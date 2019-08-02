package lectures.part2afp

object LazyEval extends App {

//  val x: Int = throw new RuntimeException will crash
  lazy val x: Int = throw new RuntimeException // will delay evaluation until when needed

  lazy val x2: Int = {
    println("hello")
    42
  }
  println(x2) // prints hello and 42
  println(x2) // prints only the value of x

  def sideEffectCondition: Boolean = {
    println("boo")
    true
  }

  def simpleCondition: Boolean = false
  lazy val lazyCondition = sideEffectCondition
  println(if (simpleCondition && lazyCondition) "yes" else "no") // side effect is not printed because simpleCondition is evaluated first

  def byNameMethod(n: => Int): Int = n + n + n + 1
  def retrieveValue = {
    println("waiting")
    Thread.sleep((1000))
    42
  }

  def betterByNameMethod(n: => Int): Int = {
    lazy val t = n
    t + t + t + 1
  }
  println(byNameMethod(retrieveValue))
  println(betterByNameMethod(retrieveValue))

  // filter with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }
  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1, 25, 40, 5, 23)
  val lt30 = numbers.filter(lessThan30) // list with numbers less than 30
  val gt20 = lt30.filter(greaterThan20)
  println(gt20)

  val lt30Lazy = numbers.withFilter(lessThan30) // withFilter uses lazy vals
  val gt20Lazy = lt30Lazy.withFilter(greaterThan20)
  println
  println(gt20Lazy)
  println
  gt20Lazy.foreach(println) // values are checked on by need basis...

  // for-comprehensions use withFilter with guards
  for {
    a <- List(1,2,3) if a % 2 == 0
  } yield a + 1
  // same as
  List(1,2,3).withFilter(_ % 2 == 0).map(_ + 1)
}
