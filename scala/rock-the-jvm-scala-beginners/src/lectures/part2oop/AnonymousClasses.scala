package lectures.part2oop

object AnonymousClasses extends App{

  abstract class Animal {
    def eat: Unit

  }

  // anonymous class
  val funnyAnimal: Animal = new Animal {
    override def eat: Unit = println("haahahahahahah")
  }
  /*
    The above code is equivalent to
    class AnonymousClasses$$anon$1 extends Animal {
      override def eat: Unit = println("ahahahahahah")

      val funnyAnimal = new AnonymousClasses$$anon$1
   */

  println(funnyAnimal.getClass)

  class Person(name: String){
    def sayHi: Unit = println(s"Hi my name is $name, how can i help")
  }

  // anonymous classes work on non-abstract classes too not just abstract classes
  val jim = new Person("Jim") {
    override def sayHi: Unit = println("I am jim")
  }


}
