package lectures.part1basics

object ValuesVariablesTypes extends App{

  // vals

  val x: Int = 42
  println(x)

  val aString: String = "hello"
  val anotherString = "goodbye"

  val aBoolean: Boolean = false
  val aChar: Char = 'a'
  val anInt: Int = x
  val aShort: Short =9000
  val aLong: Long = 23232334534343L
  val aFloat: Float = 2.0f
  val aDouble: Double = 3.14

  // variables

  var aVariable: Int = 5

  aVariable = 6; // side effects (can be updated)
}
