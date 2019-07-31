package lectures.part4pm

import exercises.{Cons, Empty, MyList}

object AllThePatterns extends App {

  val x: Any = "Scala"
  val constants = x match {
    case 1 => "A Num"
    case "Scala" => "The Scala lang"
    case true => "Truth"
    case AllThePatterns => "Singleton Object AllThePatterns"
  }

  // wildcard
  val matchAnything = x match {
    case _ =>
  }

  // variable
  val matchAVariable = x match {
    case something => s"I have found $something"
  }

  // tuples
  val aTuple = (1,2)
  val matchTuple = aTuple match {
    case (1,1) =>
      case(1,something) => s"Found tuple with _1 as 1 and _2 as $something"
  }

  val nestedTuple = (1, (2,3))
  val matchNestedTuple = nestedTuple match {
    case(_,(2,v)) =>
  }
  val aList: MyList[Int] = Cons(1,Cons(2, Empty))

  val matchList = aList match {
    case Empty =>
    case Cons(head, Cons(subhead, subtail)) =>
  }

  // list patterns
  val aStandardList = List(1,2,3,42)
  val standardListMatch = aStandardList match {
    case List(1,_,_,_) => // extractor
    case List(1,_*) => // list of any length (var args)
    case 1 :: List(_) => // infix pattern
    case List(1,2,3) :+ 24 => // infix pattern
  }

  // type specifiers
  val unknown: Any = 2
  val unknownMatch = unknown match {
    case list: List[Int] => // explicit type specifier
    case _ =>
  }

  // name binding
  val nameBindingMatch = aList match {
    case notEmptyList @ Cons(_,_) => // name binding... allows you to use the name later
    case Cons(1, rest @ Cons(2, _)) => // name binding inside a nested expression
  }

  // multi-patterns
  val multipattern = aList match {
    case Empty | Cons(0,_) => // use pipe operator to chain multiple patterns
    case _ =>
  }

  // if guards
  val secondElementSpecial = aList match {
    case Cons(_,Cons(specialElement, _)) if specialElement % 2 == 0 =>
  }

  // generics were introduced in java 5. After compiler type checking is complete generics are removed (backwards compatible java 1)
  // so List[String] is converted to List and as a result the expression below returns a list of strings
  // fix. put int first
  // this is called type erasure
  val numbers = List(1,2,3)
  val numbersMatch = numbers match {
    case listOfStrings: List[String] => "a list of strings"
    case listOfStrings: List[Int] => "a list of numbers"
    case _ => ""
  }

  println(numbersMatch)
}
