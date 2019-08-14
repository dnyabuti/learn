package part4_techniques

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout

import scala.concurrent.duration._

object IntegratingWithActors extends App {

  implicit val system = ActorSystem("IntegratingWithActors")
  implicit val materializer = ActorMaterializer()

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case s: String =>
        log.info(s"Received a string $s")
        sender() ! s"$s$s"
      case n: Int =>
        log.info(s"Received an integer: $n")
        sender() ! (n * 2)
      case _ =>
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  val numberSource = Source(1 to 10)

  // use actor as a flow
  implicit val timeout = Timeout(2 seconds
  )
  val actorBasedFlow = Flow[Int].ask[Int](parallelism = 4)(simpleActor)

//  numberSource.via(actorBasedFlow).async.to(Sink.ignore)
  // better... ask directly
//  numberSource.ask[Int](parallelism = 4)(simpleActor)

  // using actor as source
  val actorPoweredSource = Source.actorRef[Int](bufferSize = 10, overflowStrategy = OverflowStrategy.dropHead)
  val materializedActorRef = actorPoweredSource.to(Sink.foreach[Int](number => println(s"Actor powered flow got number: $number"))).run()
  materializedActorRef ! 10
  // terminate stream
  materializedActorRef ! akka.actor.Status.Success("complete")

  // Actor as Sink
  /*
      1. an init message
      2. an ack message to confirm receipt
      3. complete message
      4. a function to generate a message in case the stream throws an exception
   */

  case object StreamInit
  case object StreamAck
  case object StreamComplete
  case class StreamFail(ex: Throwable)

  class DestinationActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case StreamInit =>
        log.info("Stream initialized")
        sender() ! StreamAck
      case StreamComplete =>
        log.info("Stream complete")
        context.stop(self)
      case StreamFail(ex) =>
        log.warning(s"Stream failed: $ex")
      case message =>
        log.info(s"Message: $message has been received")
        sender() ! StreamAck
    }
  }

  val destinationActor = system.actorOf(Props[DestinationActor], "destinationActor")
  val actorPoweredSink = Sink.actorRefWithAck[Int](
    destinationActor,
    onInitMessage = StreamInit,
    onCompleteMessage = StreamComplete,
    ackMessage = StreamAck,
    onFailureMessage = throwable => StreamFail(throwable) // optional
  )

  Source(1 to 10).to(actorPoweredSink).run()
}
