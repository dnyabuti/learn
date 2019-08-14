package part4_techniques

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object AdvancedBackpressure extends App {

  implicit val system = ActorSystem("AdvancedBackpressure")
  implicit val materializer = ActorMaterializer()

  // control backpressure
  val controlledFlow = Flow[Int].map(_ * 2).buffer(10, OverflowStrategy.dropHead)

  case class PagerEvent(description: String, date: Date, nInstances: Int = 1)
  case class Notification(email: String, pagerEvent: PagerEvent)

  val events = List(
    PagerEvent("service discovery failed", new Date),
    PagerEvent("Illegal elements in pipeline", new Date),
    PagerEvent("Spike in 500 error code", new Date),
    PagerEvent("service stopped responding", new Date)
  )

  val eventSource = Source(events)

  val oncallEngineer = "dan@example.com"

  def sendEmail(notification: Notification) =
    println(s"Dear ${notification.email}, you have an event, ${notification.pagerEvent}") // send email

  val notificationSink = Flow[PagerEvent].map(event => Notification(oncallEngineer,event))
    .to(Sink.foreach[Notification](sendEmail))

//  eventSource.to(notificationSink).run()
  // if notification sink or send email is slow
  // if event source is not able to backpressure

  def sendEmailSlow(notification: Notification) = {
    Thread.sleep(1000)
    println(s"Dear ${notification.email}, you have an event, ${notification.pagerEvent}") // send email
  }
  // aggregate pager events
  val aggregateNotificationFlow = Flow[PagerEvent]
    .conflate((event1, event2) => { // acts like reduce
      val nInstances = event1.nInstances + event2.nInstances
      PagerEvent(s"You have $nInstances events that require your attention", new Date, nInstances)
  }).map(resultingEvent => Notification(oncallEngineer, resultingEvent))

  eventSource.via(aggregateNotificationFlow).async.to(Sink.foreach[Notification](sendEmailSlow)).run()

  // slow producer, fast consumer (extrapolate/expand)
  import scala.concurrent.duration._
  val slowCounter = Source(Stream.from(1)).throttle(1,1 second)
  val hungrySink = Sink.foreach[Int](println)

  val extrapolator = Flow[Int].extrapolate(element => Iterator.from(element)) // creates elements when there is unment demand
  // or
  val repeater = Flow[Int].extrapolate(element => Iterator.continually(element))

  slowCounter.via(extrapolator).to(hungrySink).run()
  // or
  //slowCounter.via(repeater).to(hungrySink).run()

  val expander = Flow[Int].expand(element => Iterator.from(element)) // creates elements at all times
}
