package lectures.part2oop

object Inheritance extends App{

  class Animal{
    // private - cannot be called outslide class
    // protected - can be accessed in subclasses as wel
    val creatureType = "Wild"
    def eat = println("nomnom")
  }

  // single class inheritance
  class Cat extends Animal{
    val cat = new Cat
    cat.eat
  }

  // constructors
  class Person(name: String, age: Int){
    def this(name: String) = this(name, 0)
  }
  //  class Adult(name: String, age: Int, idCard: String) extends Person(name, age)
  //  can work too since there is a constructor with two arguments
  class Adult(name: String, age: Int, idCard: String) extends Person(name)

  // overriding
  class Dog extends Animal {
    override val creatureType: String = "Domestic"
    override def eat: Unit = println("crunch, crunch")
  }

  val dog = new Dog
  dog.eat
  println(dog.creatureType)


  // overriding fields in constructor
  class Dog2(override val creatureType: String) extends Animal {
    override def eat: Unit = println("crunch, crunch")
  }
  val dog2 = new Dog2("K9")
  dog2.eat
  println(dog2.creatureType)

  // type substitution - (Broad: Polymorphism)
  val unknownAnimal: Animal = new Dog2("k9")
  unknownAnimal.eat

  // super - when you want to reference a field or method from parent class

  // preventing overrides
  // 1. Use keyword final
  // 2. Use final on class to prevent class from being extended final class Animal {}
  // 3. sealed - can allow extending in this file only, prevent extension in other files
  //    (useful when you want to be exhaustive in your type hierarchy. eg. there can only be two animals in this world
}
