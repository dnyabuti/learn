package lectures.part2oop

object OOBasics extends App{

  val person = new Person("Davis", 26)
  println(person.age)
  person.greet("Daniel")

  val counter = new Counter(0)
  counter.inc.inc.print
}
// constructor
//name is a class parameter and age is a field
//class parameters be converted to fields by prepending val like age below. age can be accessed externally but name cannot
class Person(name: String, val age: Int = 0) {
  // body
  println(1+1) // will be evaluated on instantiation

  def greet(name: String): Unit = println(s"${this.name} says: Hi, $name")

  // overloading
  def greet(): Unit = println(s"Hi, I am $name")

  // multiple constructors
  def this(name: String) = this(name, 0) // auxiliary constructor. they have to call another constructor. better to have default parameters
  def this() = this("John Doe")
}

class Counter(val count: Int = 0){
  def inc = new Counter(count + 1) // immutability
  def dec = new Counter(count -1)

  def inc(n: Int): Counter = {
    if(n <= 0) this
    else inc.inc(n-1)
  }
  def dec(n: Int): Counter = {
    if(n <= 0) this
    else dec.dec(n-1)
  }

  def print = println(count)
}

// class parameters are not fields
// to convert parameters to fields add val