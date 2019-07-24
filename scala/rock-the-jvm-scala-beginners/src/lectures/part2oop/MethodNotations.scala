package lectures.part2oop

object MethodNotations extends App{

  class Person(val name: String, favoriteMovie: String, val age: Int = 0){
    def likes(movie: String): Boolean = movie == favoriteMovie
    def hangsOutWith(person: Person): String = s"${this.name} is hanging out with ${person.name}"
    def +(person: Person): String = s"${this.name} is hanging out with ${person.name}"
    def +(nickname: String): Person = new Person(s"$name ($nickname)",favoriteMovie)
    def unary_! : String = s"$name what the heck"
    def unary_+ : Person = new Person(name, favoriteMovie, age+1)
    def isAlive: Boolean = true
    def learns(lang: String): String =  s"$name learns $lang"
    def learnsScala: String = learns("Scala")
    def apply(): String = s"Hi, my name is $name and I like $favoriteMovie"
    def apply(count: Int): String = s"$name watched $favoriteMovie $count times"
  }

  val mary = new Person(name = "Mary", "Inception")
  println(mary.likes("Inception"))

  // infix notation = operator notation... works on methods with one parameter
  // mathods can take any name
  println(mary likes "Inception") // infix notation

  val tom = new Person("Tom", "Fight Club")
  println(mary hangsOutWith tom)

  // operators" in scala
  println(mary + tom)

  // Actors have ! for tell and ? for ask

  // prefix notation
  val x = -1 // equivalent to 1.unary_-
  val y = 1.unary_-

  //unary prefix only works with - + ~ !

  println(!mary)
  println(mary.unary_!) // both are equivalent

  // postfix notation
  println(mary.isAlive)
  println(mary isAlive) // both are the same. only available to methods without parameters

  // apply
  println(mary.apply())
  println(mary()) // calling mary as a function calls the apply function

  println(mary learnsScala)
  println((mary + "the rockstar").apply())
  println((+mary).age)
  println(mary(10))
}
