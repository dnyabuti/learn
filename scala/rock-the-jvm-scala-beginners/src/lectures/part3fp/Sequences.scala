package lectures.part3fp

import scala.util.Random

object Sequences extends App {

  // seq
  val aSeq = Seq(5,1,2,3,4)
  println(aSeq)
  println(aSeq.reverse)
  println(aSeq(3))
  println(aSeq ++ Seq(9,7,8))
  println(aSeq.sorted)

  //range
  val aRange: Seq[Int] = 1 until 10
  aRange.foreach(println)

  (1 to 10).foreach(println)

  //list
  val aList = List(43)
  val prepend = 42 +: aList :+ 44 // :: or +: means prepend
  println(prepend)

  val apples4 = List.fill(4)("apple")
  println(apples4)

  println(prepend.mkString("-"))

  // array
  val numbers = Array(1,2,3,4)
  val threeElements = Array.ofDim[Int](3)
  println(threeElements)
  threeElements.foreach(println)

  // mutation
  numbers(2) = 3 // syntax sugar for numbers.update(2, 3)

  // arrays and seq
  val numbersSeq: Seq[Int] = numbers // implicit conversion
  println(numbersSeq)

  // vector
  val vector: Vector[Int] = Vector(1,2,3)
  println(vector)

  // perf test
  val maxRuns = 1000
  val maxCapacity = 1000000
  def getWriteTime(collection: Seq[Int]): Double = {
    val r = new Random
    val times = for {
      it <- 1 to maxRuns
    } yield {
      val currentTime = System.nanoTime()
      // updating a random index with a random value
      collection.updated(r.nextInt(maxCapacity), r.nextInt())
      System.nanoTime() - currentTime
    }
    times.sum * 1/maxRuns
  }

  val numbersList = (1 to maxCapacity).toList
  val numbersVector = (1 to maxCapacity).toVector

  // updating elements in the middle of the list is not very efficient
  println(getWriteTime(numbersList))
  // depth of the tree is small. so updating element in the middle takes a short time
  println(getWriteTime(numbersVector))
}
