package lectures.part3concurrency

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object FuturesAndPromises extends App {

  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateMeaningOfLife
  } // implicit global is passed by the compiler

  println(aFuture.value) // returns Option[Try[Int]] -- future might have failed with exception or might not have completed
  println("waiting on future to complete")
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"The meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I have failed with $e")
  }

  Thread.sleep(3000) // wait long enough to future to complete
}
