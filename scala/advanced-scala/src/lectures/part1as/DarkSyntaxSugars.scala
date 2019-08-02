package lectures.part1as

import scala.util.Try

object DarkSyntaxSugars extends App {

  // methods with single param
  def singleArgMethod(arg: Int): String = s"$arg <-arg"

  val description = singleArgMethod {
    // write some code
    42
  }

  val aTryInstance = Try {
    throw new RuntimeException
  }

  List(1,2,3,4).map { x =>
    x + 1
  }

  // instances of traits with single methods can be reduced to lambda
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = new Action {
    override def act(x: Int): Int = x + 1
  }

  // to
  val anInstance1: Action = (x: Int) => x + 1

  // runnable
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("hello runnable")
  })
  // to

  val aSweeterThread = new Thread(() => println("sweeter"))

  abstract class AnAbstractType {
    def implemented: Int = 23
    def f(a: Int): Unit
  }

  val anAbstractInstance: AnAbstractType = (a: Int) => println("sweet")

  // :: and #:: are special
  // last character determines the associativity of a method
  // ends in a colon: Right associative otherwise it is Left associative
  //
  val prependedList = 2 :: List(3, 4)
  // so the above statement is equivalent to List(3, 4).::(2)
  1 :: 2 :: 3 :: List(4, 5) // equals
  List(4, 5).::(3).::(2).::(1)

  class MyStream[T] {
    def -->:(value: T): MyStream[T] = this
  }
  val mySteam = 1-->: 2 -->: 3 -->: new MyStream[Int] // will be evaluated from right to left because it is right associative

  // multi-word method naming

  class Teen(name: String) {
    def `is playing`(game: String) = println(s"$name is playing $game")
  }

  val drogo = new Teen("Drogo")
  drogo `is playing` "fortnite"

  // infix types
  class Composite[A, B]
  val composite: Composite[Int, String] = ??? // or
  val composite2: Int Composite String = ???

  class -->[A, B]
  val towards: Int --> String = ???

  // upadate method ... special like apply
  val anArray = Array(1,2,3)
  anArray(2) = 7 // rewritten to anArray.update(2, 7). Used in mutable collections

  // setters for mutable containers
  class Mutable {
    private var internalMember: Int = 0
    def member = internalMember // getter
    def member_=(value: Int): Unit = internalMember = value // setter
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // rewritten as aMutableContainer.member_=(42

}
