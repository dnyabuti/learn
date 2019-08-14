package part5_advanced

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, FlowShape, Inlet, Outlet, SinkShape, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}

object CustomOperators extends App {

  implicit val system = ActorSystem("CustomOperators")
  implicit val materializer = ActorMaterializer()

  class RandomNumberGenerator(max: Int) extends GraphStage[SourceShape[Int]] {
    val outPort = Outlet[Int]("randomGenerator")
    val random = new Random()

    override def shape: SourceShape[Int] = SourceShape(outPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
      // implement logic
      setHandler(outPort, new OutHandler {
        override def onPull(): Unit = {
          // emit new element
          val nextNumber = random.nextInt(max)
          // push it to the outPort
          push(outPort, nextNumber)
        }
      })
    }
  }

  val randomGeneratorSource = Source.fromGraph(new RandomNumberGenerator(100))
  //randomGeneratorSource.runWith(Sink.foreach(println))

  // 2. Custom sink that prints elements in batches (backpressure handled automatically!)
  class Batcher(batchSize: Int) extends GraphStage[SinkShape[Int]] {
    val inPort = Inlet[Int]("batcher")

    override def shape: SinkShape[Int] = SinkShape[Int](inPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

      override def preStart(): Unit = {
        pull(inPort)
      }
      // mutable state
      val batch = new mutable.Queue[Int]
      setHandler(inPort, new InHandler {
        // when upstream wants to send me an element
        override def onPush(): Unit = {
          val nextElement = grab(inPort)
          batch.enqueue(nextElement)

          if(batch.size >= batchSize) {
            println("New batch: "+ batch.dequeueAll(_ => true).mkString("[", ", ", "]"))
          }
          pull(inPort) // send demand upstream
        }

        override def onUpstreamFinish(): Unit = {
          if(batch.nonEmpty) {
            println("New batch: "+ batch.dequeueAll(_ => true).mkString("[", ", ", "]"))
            println("Stream finished.")
          }
        }
      })
    }
  }

  val batcherSink = Sink.fromGraph(new Batcher(10))
//  randomGeneratorSource.to(batcherSink).run()

  // A custom filter flow

  class SimpleFilter[T](predicate: T => Boolean) extends GraphStage[FlowShape[T, T]] {
    val inPort = Inlet[T]("filterIn")
    val outPort = Outlet[T]("filterOut")

    override def shape: FlowShape[T, T] = FlowShape(inPort, outPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
      setHandler(outPort, new OutHandler {
        override def onPull(): Unit = {
          pull(inPort)
        }
      })
      setHandler(inPort, new InHandler {
        override def onPush(): Unit = {
         try {
           val nextElement = grab(inPort)
           if (predicate(nextElement)) {
             push(outPort, nextElement) // pass it on
           } else {
             pull(inPort)
           }
         } catch {
           case e: Throwable => failStage(e)
         }
        }
      })
    }
  }

  val myFilter = Flow.fromGraph((new SimpleFilter[Int](_ > 50)))
//  randomGeneratorSource.via(myFilter).to(batcherSink).run()

  // Materialized values in graph stages
  // Flow that counts the number of elements that go through it

  class CounterFlow[T] extends GraphStageWithMaterializedValue[FlowShape[T, T], Future[Int]] {

    val inPort = Inlet[T]("counterIn")
    val outPort = Outlet[T]("counterOut")

    override val shape = FlowShape(inPort, outPort)

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Int]) = {
      val promise = Promise[Int]
      val logic = new GraphStageLogic(shape) {
        // set mutable state
        var counter = 0

        setHandler(outPort, new OutHandler {
          override def onPull(): Unit = pull(inPort)

          override def onDownstreamFinish(): Unit = {
            promise.success(counter)
            super.onDownstreamFinish()
          }
        })
        setHandler(inPort, new InHandler {
          override def onPush(): Unit = {
            val nextElement = grab(inPort)
            counter += 1
            // pass it on
            push(outPort, nextElement)
          }

          override def onUpstreamFinish(): Unit = {
            promise.success(counter)
            super.onUpstreamFinish()
          }

          override def onUpstreamFailure(ex: Throwable): Unit = {
            promise.failure(ex)
            super.onUpstreamFailure(ex)
          }
        })
      }
      (logic, promise.future)
    }
  }

  val counterFlow = Flow.fromGraph(new CounterFlow[Int])
  val counterFuture = Source(1 to 10)
    .viaMat(counterFlow)(Keep.right)
    .to(Sink.foreach[Int](println))
      .run()
  import system.dispatcher
  counterFuture.onComplete {
    case Success(value) => println(s"The number of elements processed is $value")
    case Failure(exception) => println(s"Processing failed due to: $exception")
  }
}
