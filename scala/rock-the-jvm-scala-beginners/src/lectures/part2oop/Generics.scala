package lectures.part2oop

object Generics extends App{

  class MyList[+A] {
    // if B is a Supertype Of A
    def add[B >: A](element: B): MyList[B] = ???
    /*
      A = Cat
      B = Animal
      i.e if I add B to a list of Cats then the new list becomes B
     */
  }
  val listOfIntegers = new MyList[Int]
  val listOfStrings = new MyList[String]

  // objects cannot be type parameterized
  object MyList {
    def empty[A]: MyList[A] = ???
  }

  val emptyListOfIntegers = MyList.empty[Int]

  // variance problem

  class Animal
  class Cat extends Animal
  class Dog extends Animal

  // 1. Yes, List[Cat] extends List[Animal] = COVARIANCE
  class CovariantList[+A] // +A means this is a covariant list
  val animal: Animal = new Cat
  val animalList : CovariantList[Animal] = new CovariantList[Cat]

  // animalList.add(new Dog) ??? Hard question... the list is instance of Cat list

  // 2. NO INVARIANCE
  class InvariantList[A]
  val invariantAnimalList: InvariantList[Animal] = new InvariantList[Animal] //left and right have to have same type

  // 3. Hell no. CONTRAVARIANCE
  class ContravariantList[-A]
  val contravariantList: ContravariantList[Cat] = new ContravariantList[Animal] // replacing a list of cats with animals

  // 3.  CONTRAVARIANCE
  class Trainer[-A]
  val trainer: Trainer[Cat] = new Trainer[Animal] // a trainer of cats can also be a trainer of animals

  // bounded types
  class Cage[A <: Animal](animal: A) // cage accepts only sub types A which are subclasses of Animal
  val cage = new Cage(new Dog)

  class Car
  // needs proper bounded type
//  val newCage = new Cage(new Car) // will fail at runtime. Car is not subtype of Animal
}
