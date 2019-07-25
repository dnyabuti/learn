package lectures.part2oop

object Exceptions extends App {

  val x: String = null

  // NPE
  // println(x.length)

  // expression to throw an exception
  // throw new NullPointerException

  // val aWeirdValue: String  = throw new NullPointerException

  // catch exceptions
  def getInt(withExceptions: Boolean): Int =
    if (withExceptions) throw new RuntimeException("No int for you")
    else 42

  try {
    // code that might fail
    getInt(true)
  } catch {
    case e: RuntimeException => println("Caught a Runtime exception")
  } finally {
    println("Finally")
  }

  // try catch is an expression of type AnyVal
  val tryCatch = try {
    getInt(true)
  } catch {
    case e: RuntimeException => println("Exception")
  } finally {
    println("Finally")
  }

  // custom exceptions (exceptions are instances of classes)

  class MyException extends Exception

  val exception = new MyException

  // throw exception

  // crash with OOM
  // val array = Array.ofDim(Int.MaxValue)

  // stack overflow error
  // def infinite: Int = 1 + infinite
  // val noLimit = infinite

  object PocketCalculator {

    def add(x: Int, y: Int): Int = {
      val result = x + y
      if (x > 0 && y > 0 && result < 0) throw new OverflowException
      else if (x < 0 && y < 0 && result > 0) throw new UnderflowException
      else result
    }

    def subtract(x: Int, y: Int): Int = {
      val result = x - y
      if (x > 0 && y < 0 && result < 0) throw new OverflowException
      else if (x < 0 && y > 0 && result > 0) throw new UnderflowException
      else result
    }

    def multiply(x: Int, y: Int): Int = {
      val result = x * y
      if (x > 0 && y > 0 && result < 0) throw new OverflowException
      else if (x < 0 && y < 0 && result < 0) throw new OverflowException
      else if (x > 0 && y < 0 && result > 0) throw new UnderflowException
      else if (x < 0 && y > 0 && result > 0) throw new UnderflowException
      else result
    }

    def div(x: Int, y: Int): Int = {
      if (y == 0) throw new MathCalculationException
      else x / y
    }
  }

  class OverflowException extends RuntimeException

  class UnderflowException extends RuntimeException

  class MathCalculationException extends RuntimeException("Division by 0")

  println(PocketCalculator.add(10,1))

}
