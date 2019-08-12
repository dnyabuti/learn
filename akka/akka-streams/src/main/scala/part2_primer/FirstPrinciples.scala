package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {

  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materializer = ActorMaterializer()

  val source = Source(1 to 10)
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  // graph.run()

  // flows (transform elements)
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  ///////////////////
  // SAME
  ///////////////////
  //sourceWithFlow.to(sink).run()
  //source.to(flowWithSink).run()
  //source.via(flow).to(sink).run()

  // nulls are not allowed from source... use Option

  /////////////////////
  // kinds of sources
  /////////////////////
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1))
  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  /////////////////////
  // kinds of sinks
  /////////////////////
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] // Retrieves the head and then closes the stream
  val foldSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  /////////////////////
  // kinds of flows
  /////////////////////
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5)
  // flatMap is not available

  // double flow
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)

  // syntactic sugar
  val mapSource = Source(1 to 10).map(x => x * 2) //Source(1 to 10).via(Flow[Int].map(x => x * 2))

  // run streams directly
  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run()

  val names = List("JUnit", "in", "Action", "Analytics", "Beyond", "Hadoop")

  Source(names).filter(_.length > 5).take(2).runForeach(println)

}
