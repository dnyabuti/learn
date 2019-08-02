package lectures.part2afp

object CurriesPAF extends App {
  // curried function
  val supperAdder: Int => Int => Int =
    x => y => x + y

  val add3 = supperAdder(3)
  println(add3(5))
  println(supperAdder(3)(5))

  // METOHD
  def curriedAdder(x: Int)(y: Int): Int = x + y // curried method

  // LIFTING = METHOD converted to function value. function values are used in HOF
  // LIFTING = ETA-EXPANSION = converting method to function value
  val add4: Int => Int = curriedAdder(4) // if type annotation is remove function does not work

  def inc(x: Int) = x + 1
  List(1,2,3).map(x => inc(x)) // ETA-expansion is performed automatically

  // partial function applications
  val add5 = curriedAdder(5) _ // underscore tells compiler to perform eta-expansion after applying the value 5. ie val is Int => Int

  val simpleAddFunction = (x: Int, y: Int) => x +y
  def simpleAddMethod(x: Int, y: Int) = x +y
  def curriedAddMethod(x: Int)(y: Int) = x + y

  val add7 = (x: Int) => simpleAddFunction(7, x)
  println(add7(1))

  val add7_2 = simpleAddFunction.curried(7)
  val add7_3 = curriedAddMethod(7) _ // PAF (partially applied function.. .method lifted to function value
  val add7_4 = curriedAddMethod(7)(_) // PAF alternative syntax
  val add7_5 = simpleAddMethod(7,_: Int) // alternative syntax for turning methods into function values
  val add7_6 = simpleAddFunction(7,_: Int) // _ forces compiler to do ETA-Expansion

  def concatenator(a: String, b: String, c: String) = a + b + c
  val insertName = concatenator("Hello, I am ", _: String, ", how are you")
  println(insertName("Drogo"))

  val fillInTheBlanks = concatenator("Hello ", _:String, _: String)
  println(fillInTheBlanks("from ", " mars"))

  // curried formatter
  def curriedFormatter(s: String)(number: Double): String = s.format(number)
  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.3e-2)

  val simpleFormat = curriedFormatter("%4.2f") _ // lift
  val seriousFormat = curriedFormatter("%8.6f") _
  val preciseFormat = curriedFormatter("%14.12f") _

  println(numbers.map(preciseFormat))
  println(numbers.map(curriedFormatter("%12.12f"))) // compiler does eta-expansion ... no need for _

  def byName(n: => Int) = n + 1
  def byFunction(f: () => Int) = f() + 1

  def method: Int = 42
  def parenMethod(): Int = 42

  byName(42)
  byName(method)
  byName(parenMethod())
  byName(parenMethod) // ok but is equivalent to byName(parenMethod()) <--- not HOF
  // byName(() => 42) not ok
  byName((() => 42)()) // ok declaring a function and then calling it
  // byName(parenMethod _// not ok

  // byFunction(4) not ok
  // byFunction(method) // not ok -- no eta-expansion
  byFunction(parenMethod) // compiler does eta-expansion
  byFunction(() => 42)
  byFunction(parenMethod _) // works but _ us unnecessary

}
