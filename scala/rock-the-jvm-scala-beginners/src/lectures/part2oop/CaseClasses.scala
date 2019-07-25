package lectures.part2oop

object CaseClasses extends App {
  // shorthand for defining class and object in one go
  // comes with equals, hashCode, toString

  case class Person(name: String, age: Int)

  // class parameters are promoted to fields
  val davis = new Person("Davis", 102)
  println(davis.name)

  // to string
  println(davis.toString)

  // equals and hashcode are implemented by default

  val davis2 = new Person("Davis", 102)

  // true even though they are different instances
  println(davis == davis2)

  // copy methods
  val davis3 = davis.copy()

  // copy can take named arguments
  val davis4 = davis.copy(age = 30)
  println(davis4)

  // companion objects
  val thePerson = Person

  // companion object factory methods
  // usig companion object apply method to instantiate a new Person
  val alex = Person("Alex", 23)

  // case classes are serializable
  // esp in Akka

  // case classes have extractor patterns (Pattern Matching)

  // case objects act like case classes. They don't get companion objects because they already are
  case object UnitedKingdom {
    def name: String = "The UK of GB and NI"
  }
}
