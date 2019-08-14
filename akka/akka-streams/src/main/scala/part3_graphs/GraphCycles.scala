package part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, MergePreferred, RunnableGraph, Sink, Source, Zip}

object GraphCycles extends App {

  implicit val system = ActorSystem("GraphCycles")
  implicit val materializer = ActorMaterializer()

  val accelarator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))
    val incrememterShape = builder.add(Flow[Int].map { x =>
      println(s"accelerating $x")
      x + 1
    })

    sourceShape ~> mergeShape ~> incrememterShape
                    mergeShape <~ incrememterShape
    ClosedShape
  }

  // cycle deadlock
  // RunnableGraph.fromGraph(accelarator).run()
  // Solution 1: > use MergePreferred
  val actualAccelerator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(MergePreferred[Int](1))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"accelerating $x")
      x + 1
    })

    sourceShape ~> mergeShape ~> incrementerShape
                   mergeShape.preferred <~ incrementerShape
    ClosedShape
  }

  //RunnableGraph.fromGraph(actualAccelerator).run()

  // Solution 2: > Use Buffers
  val bufferedAccelarator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))
    val repeaterShape = builder.add(Flow[Int].buffer(10, OverflowStrategy.dropHead).map { x =>
      println(s"accelerating $x")
      Thread.sleep(100)
      x + 1
    })

    sourceShape ~> mergeShape ~> repeaterShape
                   mergeShape <~ repeaterShape
    ClosedShape
  }

//  RunnableGraph.fromGraph(bufferedAccelarator).run()

  /*
      Cycles in Graphs
      - Risk deadlocking

      Mitigation
      - Add bounds to the number of elements in the cycle
   */

  /*
      Fan In Shape that:
      - Takes 2 inputs
      - Output will emit INFINITE FIBONACCI SEQUENCE based off those 2 numbers
   */

  val fibonacciGenerator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zip = builder.add(Zip[BigInt, BigInt])
    val mergePreferred = builder.add(MergePreferred[(BigInt, BigInt)](1))
    val fiboLogic = builder.add(Flow[(BigInt, BigInt)].map{pair =>
      val last = pair._1
      val previous = pair._2
      Thread.sleep(100)
      (last + previous, last)
    })

    val broadcast = builder.add(Broadcast[(BigInt, BigInt)](2))
    val extractLast = builder.add(Flow[(BigInt, BigInt)].map(_._1))

    zip.out ~> mergePreferred ~> fiboLogic ~>  broadcast ~> extractLast
              mergePreferred.preferred    <~     broadcast

    UniformFanInShape(extractLast.out, zip.in0, zip.in1)
  }

  val fiboGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val source1 = builder.add(Source.single[BigInt](1))
      val source2 = builder.add(Source.single[BigInt](1))
      val sink = builder.add(Sink.foreach[BigInt](println))
      val fibo = builder.add(fibonacciGenerator)

      source1 ~> fibo.in(0)
      source2 ~>  fibo.in(1)
      fibo.out ~> sink

      ClosedShape
    }
  )
  fiboGraph.run()
}

