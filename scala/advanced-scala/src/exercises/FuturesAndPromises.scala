package exercises

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success, Try}

object FuturesAndPromises extends App {

  // fulfil promise immediately with a value
  def fulfillImmediately[T](value: T): Future[T] = Future(value)

  // in sequence
  def inSequence[A, B](first: Future[A], second: Future[B]): Future[B] = {
    first.flatMap(_ => second)
  }

  // first of two futures to complete
  def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]

    fa.onComplete(promise.tryComplete)
    fb.onComplete(promise.tryComplete)

    promise.future
  }

  // last out of two futures
  def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
    // promise which both futures will try to complete
    // one that fails to complete the first promise will complete the second
    val bothPromise = Promise[A]
    val lastPromise = Promise[A]

    val checkAndComplete = (result: Try[A]) =>
      if(!bothPromise.tryComplete(result))
        lastPromise.complete(result)

    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)

    lastPromise.future
  }

  val fast = Future {
    42
  }
  val slow = Future {
    Thread.sleep(200)
    65
  }
  first(fast, slow).foreach(println)
  last(fast, slow).foreach(println)
  Thread.sleep(1000)

  def retryUntil[A](action: () => Future[A], condition: A => Boolean): Future[A] =
    action()
    .filter(condition)
    .recoverWith {
      case _ => retryUntil(action,condition)
    }

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println(s"generated $nextValue")
    nextValue
  }
  retryUntil(action, (x: Int) => x < 20).foreach(result => println(s"settled at $result"))

  Thread.sleep(10000)
}
