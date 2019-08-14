package part4_techniques

import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{RestartSource, Sink, Source}

import scala.concurrent.duration._
import scala.util.Random

object FaultTolerance extends App {
  implicit val system = ActorSystem("FaultTolerance")
  implicit val materializer = ActorMaterializer()

  val faultySource = Source(1 to 10).map(e => if (e == 6) throw new RuntimeException else e)
  //faultySource.log("trackingElements").to(Sink.ignore).run()
  //exception terminates stream

  // 1. Gracefully terminating a stream
  faultySource.recover {
    case _: RuntimeException => Int.MinValue
  }.log("gracefulSource")
    .to(Sink.ignore)
    //.run()

  // 2. Recover with another stream
  faultySource.recoverWithRetries(3, {
    case _: RuntimeException => Source(90 to 99)
  }).log("recoverWithRetries")
    .to(Sink.ignore)
    //.run()

  // 3. Backoff supervision
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 1 second,
    maxBackoff = 30 seconds,
    randomFactor = 0.2,
  )(() => {
    val randomNumber = new Random().nextInt(20)
    Source(1 to 10).map(elem => if (elem == randomNumber) throw new RuntimeException else elem)
  })

  restartSource
    .log("restartBackoff")
    .to(Sink.ignore)
    //.run()

  // 4. Supervision strategy
  val numbers = Source(1 to 20).map(elem => if (elem == 13) throw new RuntimeException("bad luck") else elem).log("supervision")
  val supervisedNumbers =numbers.withAttributes(ActorAttributes.supervisionStrategy {
    // RESUME = skip faulty element, STOP = stop stream, RESTART = resume + clear internal state
    case _: RuntimeException => Resume
    case _ => Stop
  })

  supervisedNumbers.to(Sink.ignore).run()
}
