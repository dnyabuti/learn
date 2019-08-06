package lectures.part4implicits

import lectures.part4implicits.OrganizingImplicits.Person

object OrganizingImplicits extends App {

  println(List(1,3,4,2).sorted) // sorted takes an implicit parameter ...  scala.Predef package has the defaults

  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _) // override default ordering

  // implicits
  /*
      - val/var
      - object
      - accessor methods = defs with no parentheses
   */

  case class Person(name: String, age: Int)

  val persons = List (
    Person("Steve", 30),
    Person("Jack", 89),
    Person("Amy", 21)
  )
  object Person {
    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  object AlphabeticNameOrdering {
    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  }
  import AgeOrdering._
  println(persons.sorted)

  // implicit scope
  /*
     1. Local scope (normal scope)
     2. Imported Scope
     3. Companion objects of all types involved in method signature
   */

  /*
      ordering by use
      totalPrice - 50%
      by unit count - 25%
      by unit price 25%
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits * a.unitPrice < b.nUnits * b.unitPrice)
  }
  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits < b.nUnits)
  }
  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.unitPrice < _.unitPrice)
  }
  }
