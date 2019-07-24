package lectures.part1basics

object Functions extends App{

  def aFunction(a: String, b: Int): String = {
      a + " " + b
  }

  println (aFunction("hello", 3))

  def aParameterlessFunction(): Int = 42

  println(aParameterlessFunction())
  println(aParameterlessFunction)

  def aRepeatedFunction(aString: String, n: Int): String = {
    if( n== 1) aString
    else aString + aRepeatedFunction(aString, n-1)
  }

  println(aRepeatedFunction("hello", 3))

  // when loops are needed use recursion

  def aFunctionWithSideEffects(aString: String): Unit = {
    println(aString)
  }

  def aBigFunction(n: Int): Int = {
    def aSmallerFunction(a: Int, b: Int): Int = a + b

    aSmallerFunction(n, n-1)
  }

  def aGreeting(name: String, age: Int): String = {
    "Hi, my name is "+name+" and I am "+age+" years old."
  }

  println(aGreeting("davis", 33))

  def aFactorial(n: Int): Int = {
    if(n <= 0) 1
    else n * aFactorial(n-1)
  }

  println(aFactorial(4))

  def aFibonacci(n: Int): Int = {
    if (n <= 2) 1
    else aFibonacci(n - 1) + aFibonacci(n - 2)
  }

  println(aFibonacci(20))

  def isPrime(n: Int): Boolean = {
    def testIsPrimeUntil(t: Int): Boolean =
      if (t <= 1) true
      else n % t !=0 && testIsPrimeUntil(t-1)

      testIsPrimeUntil(n / 2)

  }
}
