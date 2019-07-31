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

  case class Person(name: String, age: Int)
  val cal = Person("Cal", 20)

  val greeting = cal match {
    case Person(n, a) if a < 21 => s"Hi $a year old $n. You may drink"
    case Person(n, a) => s"Hi $n. Are you $a?"
  }

  println(greeting)

  sealed class Animal
  case class Dog(breed: String) extends Animal
  case class Parrot(greeting: String) extends Animal

  val animal: Animal = new Dog("GS")

  // when a class is sealed compiler will notify you that the match is not exhaustive
  animal match {
    case Dog(someBreed) => println(s"Matched dog of the $someBreed breed" )
  }

  trait Expr
  case class Number(n: Int) extends Expr
  case class Sum(e1: Expr, e2: Expr) extends Expr
  case class Prod(e1: Expr, e2: Expr) extends Expr

  // return human readable format
  // Sum(Number(2), Number(3)) => 2 + 3

  def show(e: Expr): String = e match {
    case Number(n) => s"$n"
    case Sum(e1, e2) => show(e1) + " + " + show(e2)
    case Prod(e1,e2) => {
      def maybeShowParentheses(exp: Expr) = exp match {
        case Prod(_,_) => show(exp)
        case Number(_) => show(exp)
        case _ => "(" + show(exp) + ")"
      }
      maybeShowParentheses(e1) + " * " + maybeShowParentheses(e2)
    }
  }

  println(show(Sum(Number(1), Number(2))))
  println(show(Prod(Sum(Number(1),Number(2)),Number(3))))
}
