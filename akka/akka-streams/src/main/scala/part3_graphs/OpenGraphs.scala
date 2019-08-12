package part3_graphs

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}

object OpenGraphs extends App {

  implicit val system = ActorSystem("OpenGraphs")
  implicit val materializer = ActorMaterializer()

  /*
      A composite source that connects 2 sources
      1. Emit all elements from the first source
      2. Then emit all elements from the second source

      i.e concat the sources
   */

  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  val sourceGraph = Source.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      // declare components
      val concat = builder.add(Concat[Int](2))
      // connect components
      firstSource ~> concat
      secondSource ~> concat
      SourceShape(concat.out)
    }
  )
  sourceGraph.runForeach(println)

  // complex sink
  val sink1 = Sink.foreach[Int](x => println(s"Meaningful thing 1 $x"))
  val sink2 = Sink.foreach[Int](x => println(s"Meaningful thing 2 $x"))

  val sinkGraph = Sink.fromGraph(
    GraphDSL.create() {implicit builder =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Int](2))

      broadcast ~> sink1
      broadcast ~> sink2

      SinkShape(broadcast.in)
    }
  )

  firstSource.to(sinkGraph).run()

  // flow composed of two other flows
  val incrementer = Flow[Int].map(_ + 10)
  val multiplier = Flow[Int].map(_ * 10)

  val flowGraph = Flow.fromGraph(
    GraphDSL.create() {implicit builder =>
      import GraphDSL.Implicits._

      // components
      val incrememterShape = builder.add(incrementer)
      val multiplierShape = builder.add(multiplier)

      // connect components
      incrememterShape ~> multiplierShape

      FlowShape(incrememterShape.in, multiplierShape.out)
    }
  )
  firstSource.via(flowGraph).to(Sink.foreach(println)).run()

  // flow from sink to souce

  def fromSInkAndSource[A, B](sink: Sink[A, _], source: Source[B,_]): Flow[A, B, _] =
    Flow.fromGraph(
      GraphDSL.create() {implicit builder =>
        val sourceShape = builder.add(source)
        val sinkShape = builder.add(sink)

        FlowShape(sinkShape.in, sourceShape.out)
      }
    )

  // same as
  val f = Flow.fromSinkAndSourceCoupled(Sink.foreach[String](println), Source(1 to 10))
}
