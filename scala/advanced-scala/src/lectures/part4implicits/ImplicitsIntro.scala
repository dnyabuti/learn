package lectures.part4implicits

object ImplicitsIntro extends App {

  val pair = "Daniel" -> "555" // no arrow method on string class
  val intPair = 3 -> "three"

  case class Person(name: String) {
    def greet = s"Hi my name is $name"
  }

  implicit def fromStringToPerson(str: String): Person = Person(str)

  println("Davis".greet) // looks up for implicit classes, methods and values that have a greet method with a return type Person
  // println(fromStringToPerson("Davis").greet

  //implicit parameters
  def increment(x: Int)(implicit amount: Int) = x + amount
  implicit val defaultAmount = 10
  println(increment(2)) // 12 -- default amount implicitly passed by compiler from search scope

}
