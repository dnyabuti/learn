package lectures.part3fp

object MapFlatmapFilterFor extends App {

  val list = List(1,2,3)
  println(list)

  println(list.head)
  println(list.tail)

  println(list.map(_ + 1))

  println(s"Even numbers ${list.filter(_ % 2 == 0)}")

  val toPair = (x: Int) => List(x, x+1)

  println(list.map(toPair))
  println(list.flatMap(toPair))

  val numbers = List(1,2,3,4)
  val chars = List('a', 'b', 'c', 'd')
  val colors = List("green", "blue")


  val combinations = numbers.flatMap(n => chars.flatMap(c => colors.map(cl => "" + c + n + cl)))
  println(combinations)

  // foreach similar to map but takes a function returning Unit
  list.foreach(println)

  // for-comprehension
  val forCombinations = for {
    n <- numbers if n % 2 == 0 //guards (a filter)
    c <- chars
    color <- colors
  } yield "" + c + n + color

  println(forCombinations)

  for {
    n <- numbers
  } println(n)



}
