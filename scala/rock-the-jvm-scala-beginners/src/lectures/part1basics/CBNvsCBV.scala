package lectures.part1basics

object CBNvsCBV extends App{

  def calledByValue(x: Long): Unit = {
    println("by value: " + x)
    println("by value: " + x)
  }
  def calledByName(x: => Long): Unit = {
    println("by name: " + x)
    println("by name: " + x)
  }

  calledByValue(System.nanoTime())
  calledByName(System.nanoTime())

  def infinite(): Int = 1 + infinite()
  def printFirst(x: Int, y: => Int) = println(x)

  // => means call by value. value of expression is evaluated every time
//  println(infinite(), 34) // will crash because infinite is called continuously
  printFirst(34, infinite()) // => in the function def means that infinite() which is variable y will be
                            // evaluated lazily. since it is not used in function, infinite is actually
                            // never evaluated. so the function call does not fail
                            // y is never used
}
