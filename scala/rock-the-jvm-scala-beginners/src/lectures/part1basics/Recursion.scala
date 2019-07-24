package lectures.part1basics

import scala.annotation.tailrec

object Recursion extends App{

  def factorial(n: Int): Int = {
    if(n < 1 ) 1
    else {
      println("computing factorial of " + n + " - I first need factorial of " + (n-1))
      val result = n * factorial(n-1)
      println("Computed factorial of "+ n)
      result
    }
  }

//  print(factorial(5000))

  def anotherFactorial(n: Int): BigInt = {
    @tailrec
    def factHelper(x: Int, accumulator: BigInt): BigInt = {
      if (x <= 1) accumulator
      else factHelper(x - 1, x * accumulator) // TAIL RECURSION -- use recursive call asl the last expression
    }

    factHelper(n,1)
  }

  println(anotherFactorial(20000))

  // when you need loops use tail recursion
  @tailrec
  def concatenateTailRec(aString: String, n: Int, accumulator: String): String = {
      if( n <= 0) accumulator
      else concatenateTailRec(aString, n - 1, aString + accumulator)
  }

  println(concatenateTailRec("davis",3,""))

  def isPrime(n: Int): Boolean = {
    @tailrec
    def testIsPrimeUntil(t: Int, isStillPrime: Boolean): Boolean =
      if (!isStillPrime) false
      else if (t <= 1) true
      else testIsPrimeUntil(t-1, isStillPrime && n % t != 0)

    testIsPrimeUntil(n / 2, true)
  }

  println(isPrime(2009))

  def aFibonacci(n: Int, accumulator: Int): Int = {
    @tailrec
    def fiboTailRec(i: Int, last: Int, nextToLast: Int): Int = {
      if (i >= n) last
      else fiboTailRec(i + 1, last + nextToLast, last)
    }
    if(n <= 2) 1
    else fiboTailRec(2,1,1)
  }

  println(aFibonacci(8,0))
}

