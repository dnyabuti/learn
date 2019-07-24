package lectures.part2oop

object AbstractDataTypes extends App {

 // abstract (unimplemented methods/fields)
  abstract class Animal {
   val creatureType: String
   def eat: Unit
 }

  class Dog extends Animal {
    override val creatureType: String = "Canine"
    def eat: Unit = println("crunc crunch") // override is not necessary when extending an abstract class
  }

  // traits (the ultimate abstract data types
  trait Carnivore {
    def eat(animal: Animal): Unit // abstract method
    val preferredMeal: String = "fresh meat"
  }

  // trait
  trait ColdBlooded

  // unlike abstract classes, traits can be inherited along with classes
  class Crocodile extends Animal with Carnivore with ColdBlooded {
    override val creatureType: String = "croc"

    override def eat: Unit = println("nomnom")

    override def eat(animal: Animal): Unit = println(s"I'm a croc and i'm eating ${animal.creatureType}")
  }

  val dog = new Dog
  val croc = new Crocodile
  croc.eat
  croc.eat(dog)

  // traits vs abstract classes
  // 1. Traits do not have constructors
  // 2. You can only extend 1 class but you can inherit multiple traits in the same class
  // 3. traits = behavior, abstract class = thing

}
