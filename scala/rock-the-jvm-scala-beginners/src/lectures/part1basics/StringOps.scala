package lectures.part1basics

object StringOps extends App{
  val str: String = "Hello, I am learning Scala"

  println(str.charAt(2))
  println(str.substring(1,11))
  println(str.split(" ").toList)
  println(str.startsWith("Hello"))
  println(str.replace(" ", "-"))
  println(str.toLowerCase())
  println(str.length)

  //scala specific
  val aNumberString = "2"
  val aNumber = aNumberString.toInt
  println('a' +: aNumberString :+ 'z') // prepend and append operations
  println(aNumberString.reverse)
  println(str.take(2))

  // String interpolations

  // s-interpolators
  val name = "Davis"
  val age = 12
  val greeting = s"Hello, my name is $name and i am $age years old"
  val anotherGreeting = s"Hello, my name is $name and i will be turning  ${age + 1}"
  println(anotherGreeting)

  // F-interpolators
  val speed = 1.2f
  val myth = f"$name can eat $speed%2.2f burgers per minute"
  println(myth)

  // raw-interpolator
  println(raw"this is a \n new line")
  val escaped = "this is a \n new line"
  println(raw"$escaped") // injected values will be escaped

}
