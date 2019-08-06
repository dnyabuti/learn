package lectures.part2afp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1

  val aFussyFunction = (x: Int) => {
    if  (x == 1) 42
    else if ( x == 2) 56
    else if ( x == 3) 999
    else throw new FunctionNotApplicableException
  }

  class FunctionNotApplicableException extends RuntimeException

  // less clunky
  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }
  // this is a partial function that accepts only part of the int domain. it is of type
  // {1, 2, 5} => Int ... since 1, 2, 5 is a subset of the int domain this function is a partial function

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  } // equivalent to the function above

  println(aPartialFunction(2))
  // println(aPartialFunction(67)) // will fail because partial functions use pattern matching and 67 is not handled

  // partial function utilities
  println(aPartialFunction.isDefinedAt(67))

  // lift
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(2))
  println(lifted(67)) // instead of throwing an exception this call print None

  // chain multiple partial function
  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 45 => 67
  }
  println(pfChain(2))
  println(pfChain(45)) // calls the second partial function

  // partial functions extend normal functions
  // partial functions are a subset of total functions
  val aTotalFunction: Int => Int = {
    case 1 => 99
  }

  // HOF accept partial functions
  val aMappedList = List(1,2,3).map {
    case 1 => 42
    case 2 => 78
    case 3 => 1000
  }
  println(aMappedList)

  // partial functions can only accept one parameter type
  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def apply(x: Int ): Int = x match {
      case 1 => 42
      case 2 => 65
      case 5 => 999
    }
    override def isDefinedAt(x: Int): Boolean =
      x == 1 || x == 2 || x == 5
  }

  val chatbot: PartialFunction[String, String] = {
    case "hello" => s"Hi my name is FORTNITE000"
    case "goodbye" => "bye human"
    case "call mom" => "unable to find your phone"
  }
  scala.io.Source.stdin.getLines().map(chatbot.lift).foreach(println)
 }
