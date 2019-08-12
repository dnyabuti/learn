package part2_primer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object BackpressureBasics extends App {

  implicit val system = ActorSystem("BackpressureBasics")
  implicit val materializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int] { x =>
    Thread.sleep(1000)
    println(s"Sink: $x")
  }

  //astSource.to(slowSink).run // operator fusion... backpressure does not apply on the same thread

  // backpressure
  fastSource.async.to(slowSink).run()

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming: $x")
    x + 1
  }

  // sink sends backpressure upstream... flow buffers internally until it has enough. default is 16, then sends the signal upstream to source
  fastSource.async
    .via(simpleFlow).async
    .to(slowSink)
    .run()

  ////////////////////////////////
  // reactions to backpressure
  ////////////////////////////////
  /*
      1. try to slow down if possible
      2. buffer elements until there's more demand
      3. drop down elements from buffer it it overflows
      4. tear-down/kill the hole stream (failure)
   */

  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  // only a few elements make it to the sink
  // sink buffers the 1st 16 numbers at the sink
  // then flow buffers the next 10 elements
  // flow will start dropping at the next element (oldest element removed)
  fastSource.async
    .via(bufferedFlow).async
    .to(slowSink)
    .run()

  /////////////////////////////
  // Overflow strategies
  /////////////////////////////
  /*
      - drop head (oldest)
      - drop tail (newest)
      - drop new (the element to be added)
      - drop entire buffer
      - backpressure signal
   */

  // throttling
  import scala.concurrent.duration._
  // print 2 elements per second
  fastSource.throttle(2, 1 second).runWith(Sink.foreach(println))
}
