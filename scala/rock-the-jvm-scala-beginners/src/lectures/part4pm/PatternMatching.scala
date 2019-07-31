package lectures.part4pm

import scala.util.Random

object PatternMatching extends App {

  val random = new Random
  val x = random.nextInt(10)

  val description = x match {
    case 1 => "the one"
    case 2 => "doubles"
    case 3 => "third"
    case _ => "hr"
  }

  println(x)
  println(description)
}
