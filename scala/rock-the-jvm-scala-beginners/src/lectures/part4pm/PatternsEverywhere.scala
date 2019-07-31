package lectures.part4pm

object PatternsEverywhere extends App {

  // catch block is a pattern match
  try {
    // some code
  } catch {
    case e: RuntimeException => "runtime"
    case npe: NullPointerException => "npe"
    case _ => "something else"
  }

  // generators are also based on pattern matching
  val list = List(1,2,3,4)
  val evenOnes = for {
    x <- list if x % 2 == 0 //????
  } yield 10 * x

  println((evenOnes))

  val tuples = List((1,2), (3,4))
  val filterTuples = for {
    (first, second) <- tuples
  } yield first * second

  // multiple value definitions based on pattern matching
  val tuple = (1,2,3)
  val (a,b,c) = tuple

  println(b)

  val head :: tail = list
  println(head)
  println(tail)

  // partial function
  val mappedList = list.map {
    case v if v %2 == 0 => v + " is even"
    case 1 => "one"
    case _ => "something else"
  }

  println(mappedList)
}
