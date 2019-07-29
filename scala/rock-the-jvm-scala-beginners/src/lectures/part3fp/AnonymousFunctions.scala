package lectures.part3fp

object AnonymousFunctions extends App{

  val doubler: Int => Int = x => x * 2

  // multiple parameters
  val adder: (Int, Int) => Int = (x: Int, y: Int) => x +y

  // no params
  val doSomething: () => Int = () => 3

  println(doSomething) // function
  println(doSomething()) // call (lambda functions must be called with ()

  // curly braces with lambdas

  val stringToInt = { (str: String) =>
    str.toInt
  }

  val niceIncrementer: Int => Int = _ + 1 // equivalent to x => x + 1

  val niceAdder: (Int, Int) => Int = _ + _ // same as (a, b) => a + b

  println(niceAdder(2,3))

}
