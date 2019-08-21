package playground

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, RunnableGraph, Source}

import scala.concurrent.duration._


object MultipleConsumers extends App {
  implicit val system = ActorSystem("AdvancedBackpressure")
  implicit val materializer = ActorMaterializer()
  // A simple producer that publishes a new "message" every second
  val producer = Source(Stream.from(1))

  // Attach a BroadcastHub Sink to the producer. This will materialize to a
  // corresponding Source.
  // (We need to use toMat and Keep.right since by default the materialized
  // value to the left is used)
  val runnableGraph: RunnableGraph[Source[Int, NotUsed]] =
  producer.toMat(BroadcastHub.sink(bufferSize = 256))(Keep.right)

  // By running/materializing the producer, we get back a Source, which
  // gives us access to the elements published by the producer.
  val fromProducer: Source[Int, NotUsed] = runnableGraph.run()

  // Print out messages from the producer in two independent consumers
  fromProducer.runForeach(msg => {
    Thread.sleep(1000)
    println("consumer1: " + msg)
  })

  fromProducer.runForeach(msg => {
    Thread.sleep(1000)
    println("consumer2: " + msg)
  })
}
