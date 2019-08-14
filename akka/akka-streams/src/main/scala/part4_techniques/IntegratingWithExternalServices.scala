package part4_techniques

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import scala.concurrent.Future

object IntegratingWithExternalServices extends App {

  implicit val system = ActorSystem("IntegratingWithExternalServices")
  implicit val materializer = ActorMaterializer()
//  import system.dispatcher // not recommended in practive for mapAsync
  implicit val dispatcher = system.dispatchers.lookup("dedicated-dispatcher")

  def genericExternalService[A, B](element: A): Future[B] = ???

  // example : PagerDuty (alerts)
  case class PagerEvent(application: String, description: String, date: Date)

  val eventSource = Source(List(
    PagerEvent("AkkaInfra", "Inra broke", new Date),
    PagerEvent("FastData", "Illegal characters", new Date),
    PagerEvent("AkkaInfra", "Service unavailable", new Date),
    PagerEvent("Frontend", "page not loading", new Date),
  ))

  object PagerService {
    private val engineers = List("Dan", "Davis", "Oso")
    private val emails = Map(
      "Dan" -> "dan@example.com",
      "Davis" -> "davis@example.com",
      "Oso" -> "oso@example.com"
    )

    def processEvent(pagerEvent: PagerEvent) = Future {
      val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond/(24*3600)) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val engineerEmail = emails(engineer)

      // page the engineer
      println(s"Sending engineer $engineerEmail a high priority notification. $pagerEvent")
      Thread.sleep(1000)

      // return email that was paged
      engineerEmail
    }
  }

  val infraEvents = eventSource.filter(_.application == "AkkaInfra")
  val pagedEngineerEmails = infraEvents.mapAsync(parallelism = 4)(event => PagerService.processEvent(event))
  // guarantees the relative order of elements... use mapAsyncUnordered if you dont care for order
  val pagedEmailSink = Sink.foreach[String](email => println(s"Successfully sent notification to $email"))

  pagedEngineerEmails.to(pagedEmailSink).run()

  class PagerActor extends Actor with ActorLogging {
    private val engineers = List("Dan", "Davis", "Oso")
    private val emails = Map(
      "Dan" -> "dan@example.com",
      "Davis" -> "davis@example.com",
      "Oso" -> "oso@example.com"
    )

    private def processEvent(pagerEvent: PagerEvent) = {
      val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond/(24*3600)) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val engineerEmail = emails(engineer)
      // page the engineer
      log.info(s"Sending engineer $engineerEmail a high priority notification. $pagerEvent")
      Thread.sleep(1000)

      // return email that was paged
      engineerEmail
    }

    override def receive: Receive = {
      case pagerEvent: PagerEvent =>
        sender() !processEvent(pagerEvent)
    }
  }
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(3 seconds)
  val pagerActor = system.actorOf(Props[PagerActor], "pagerActor")

  val alternativePagedEngineerEmails = infraEvents.mapAsync(parallelism = 4)(event => (pagerActor ? event).mapTo[String])
  alternativePagedEngineerEmails.to(pagedEmailSink).run()

  // do not confuse mapAsync with async (ASYNC boundary)
}
