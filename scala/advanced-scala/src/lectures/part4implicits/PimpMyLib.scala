package lectures.part4implicits

object PimpMyLib extends App {

  // extend classes we dont have access to
  implicit class RichInt(val value: Int) extends AnyVal {
    def isEven: Boolean = value %2 == 0
    def sqrt: Double = Math.sqrt(value)
    def times(function: () => Unit): Unit = {
      def timesAux(n: Int): Unit =
        if (n <= 0) ()
        else {
          function()
          timesAux(n-1)
        }
      timesAux(value)
    }

    def *[T](list: List[T]): List[T] = {
      def concatenate(n: Int): List[T] = {
        if (n <= 0) List()
        else concatenate(n-1) ++ list
      }
      concatenate(value)
    }
  }
  println(42.isEven) // new RichInt(42).isEven

  import scala.concurrent.duration._
  val x = 2.seconds

  // enrich string class with asInt and encrypt method

  implicit class RichString(val string: String) extends AnyVal {
    def asInt: Int = Integer.valueOf(string)
    def encrypt(cypherDistance: Int): String = string.map(c => (c + cypherDistance).asInstanceOf[Char])
  }

  println("34".asInt * 2)
  println("john".encrypt(2))
  3.times(() => println("GOT"))
  println(4 * List(3,4))

  // this would work but it is not best practice...
  implicit def stringToInt(string: String): Int = Integer.valueOf(string)
  println("8"/ 4) // stringToInt(8)/4

  // implicit conversions from methods are discouraged

  //danger zone
  implicit def intToBoolean(i: Int): Boolean = i == 1

  /*
      if (n) do something
      else do something else
   */
  val aConditionedValue = if (3) "OK" else "Something wrong"
  println(aConditionedValue)
  // if there is a bug in an implicit def it would be hard to debug...
  // avoid implicit defs
  // if implicit conversions are needed make them specific and avoid general types
}
