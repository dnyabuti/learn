package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App {

  implicit val system = ActorSystem("OperatorFusion")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val simpleSource = Source(1 to 1000)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach[Int](println)

  // runs on the same actor .. operator/component fusion
  // simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()

  val complexFlow = Flow[Int].map { x =>
    Thread.sleep(1000)
    x + 1
  }
  val complexFlow2 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x * 10
  }

  // 2 seconds between each element being generated (one actor does all the work)
  // simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run()

  // async boundary (break operator fusion)
//  simpleSource.via(complexFlow).async // one actor
//    .via(complexFlow2).async // second actor
//    .to(simpleSink) // third actor
//    .run()

  // ordering guarantees
  // guaranteed order
//  Source(1 to 3)
//    .map(element => { println(s"Flow A: $element"); element })
//    .map(element => { println(s"Flow B: $element"); element })
//    .map(element => { println(s"Flow C: $element"); element })
//    .runWith(Sink.ignore)
  // no ordering guarantee
  Source(1 to 3)
    .map(element => { println(s"Flow A: $element"); element }).async
    .map(element => { println(s"Flow B: $element"); element }).async
    .map(element => { println(s"Flow C: $element"); element }).async
    .runWith(Sink.ignore)
}
