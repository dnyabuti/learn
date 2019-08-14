package part3_graphs

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape2, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}

object MoreOpenGraphs extends App {

  implicit val system = ActorSystem("MoreOpenGraphs")
  implicit val materializer = ActorMaterializer()

  // max 3 operator (3 inputs with one output
  val max3StaticGraph = GraphDSL.create() {implicit builder =>
    import GraphDSL.Implicits._

    val max1 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))
    val max2 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))

    max1.out ~> max2.in0

    UniformFanInShape(max2.out, max1.in0, max1.in1, max2.in1)
  }

  val source1 = Source(1 to 10)
  val source2 = Source(1 to 10).map(_ => 5)
  val source3 = Source((1 to 10).reverse)

  val maxSink = Sink.foreach[Int](x => println(s"Max is: $x"))

  val max3RunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val max3Shape = builder.add(max3StaticGraph)

      source1 ~> max3Shape.in(0)
      source2 ~> max3Shape.in(1)
      source3 ~> max3Shape.in(2)
      max3Shape.out ~> maxSink

      ClosedShape
    }
  )
//  max3RunnableGraph.run()

  // Non-Uniform Fan Out Shape
  // txns > 10,000 should go to a different flow (suspicious)

  case class Transaction(id: String, source: String, recepient: String, amount: Double, date: Date)

  val transactionSource = Source(List(
    Transaction("23233332", "Paul", "Jim", 100, new Date),
    Transaction("23233332", "Suspect", "This", 100000, new Date),
    Transaction("23233332", "Good", "One", 7000, new Date),
  ))

  val bankProcessor = Sink.foreach[Transaction](println)
  val suspiciousAnalysisService = Sink.foreach[String](txnId => println(s"Suspicious transaction ID: $txnId"))

  val suspiciousTxnStaticGraph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Transaction](2))
    val suspiciousTransactionFilter = builder.add(Flow[Transaction].filter(_.amount > 10000))
    val transactionIdExtractor = builder.add(Flow[Transaction].map[String](txn => txn.id))

    broadcast.out(0) ~> suspiciousTransactionFilter ~> transactionIdExtractor

    new FanOutShape2(broadcast.in, broadcast.out(1), transactionIdExtractor.out)
  }

  val suspiciousTxnRunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val suspiciousTxnShape = builder.add(suspiciousTxnStaticGraph)

      transactionSource ~> suspiciousTxnShape.in
      suspiciousTxnShape.out0 ~> bankProcessor
      suspiciousTxnShape.out1 ~> suspiciousAnalysisService

      ClosedShape
    }
  )

  suspiciousTxnRunnableGraph.run()
}
