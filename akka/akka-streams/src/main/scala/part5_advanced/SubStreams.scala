package part5_advanced

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.util.{Failure, Success}

object SubStreams extends App {

  implicit val system = ActorSystem("SubStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  // 1. Grouping a stream by a certain function

  val wordsSource = Source(List(
    "Akka",
    "is",
    "amazing",
    "learning",
    "substreams"
  ))

  val groups = wordsSource.groupBy(30, word => if(word.isEmpty) '\0' else word.toLowerCase().charAt(0))

  groups.to(Sink.fold(0)((count, word) => {
    val newCount = count + 1
    println(s"I just received $word, count is $newCount")
    newCount
  })).run()

  // 2. Merge Substreams back
  val textsource = Source(List(
    "I like akka streams",
    "data is amazing",
    "leaning is good for you"
  ))

  val totalCharCountFuture = textsource.groupBy(2, string => string.length % 2)
    .map(_.length) // expensive computation here
    .mergeSubstreams//WithParallelism(2)
    .toMat(Sink.reduce[Int](_ + _))(Keep.right)
    .run()

  totalCharCountFuture.onComplete {
    case Success(value) => println(s"Total Char count: $value")
    case Failure(ex) => println(s"Char computation failed: $ex")
  }

  // 3. Split stream into substreams when a condition is met

  val text = """I like akka streams
               |data is amazing
               |leaning is good for you""".stripMargin

  val anotherCharCountFuture = Source(text.toList)
    .splitWhen(c => c == '\n') // expect 3 substreams
    .filter(_ != '\n')
    .map(_ => 1)
    .mergeSubstreams
    .toMat(Sink.reduce[Int](_ + _))(Keep.right).run()

  anotherCharCountFuture.onComplete {
    case Success(value) => println(s"Total Char count: $value")
    case Failure(ex) => println(s"Char computation failed: $ex")
  }

  // Flattening -- equivalent of flatMap on streams
  /*
      Either
      - Concat
      - Merge
   */
  val simpleSource = Source(1 to 5)
  simpleSource.flatMapConcat(x => Source(x to (3 * x))).runForeach(println)

  // merge
  simpleSource.flatMapMerge(2, x => Source(x to (3 * x))).runForeach(println)
}
