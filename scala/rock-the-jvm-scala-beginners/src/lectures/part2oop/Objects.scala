package lectures.part2oop

object Objects extends App{

  // Scala does not have class level functionality. There are no static. Instead Scala has Object
  object Person {
    val N_EYES = 2
    def canFly: Boolean = false
    // factory method
    def apply(mother: Person, father: Person): Person = new Person("Bobbie")
  }

  class Person(val name: String){
    // instance level functionality
  }
  // COMPANIONS -- classes and objects with the same name in the same scope

  println(Person.N_EYES)
  println(Person.canFly)

  // in Scala, Scala object = SINGLETON INSTANCE
  val mary = new Person("Mary")
  val john = new Person("John")

  val person1 = Person
  val person2 = Person

  // both of the above are the same
  println(person1 == person2) // true

  val bobbie = Person(mary, john) // calls the factory method apply in the object (singleton instance) which takes two person arguments

  println(bobbie.name)



}
