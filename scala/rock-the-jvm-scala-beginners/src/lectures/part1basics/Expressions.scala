package lectures.part1basics

object Expressions extends App{

  val x = 1 + 2 // Compiler knows value of x because it can be infered from the expression
  println(x)

  // + - * / & | ^ << >> >>> (right shift with zero extension)

  // if expression -- if is an expression in scala
  val aCondition: Boolean = true
  val aConditionValue = if (aCondition) 7 else 3
  println(aConditionValue)

  println( if (aCondition) 7 else 3)

  //while loop (never write this again) <--- they are for imparative programming
  var i = 0
  while(i < 10 ){
    println(i)
    i += 1
  }

  var aVariable = 5

  // everything in scala is an expression
  val aWeirdValue = (aVariable = 3) // aWeirdValue is of type Unit, Unit === void

  // side effects: println(), whiles, reassigning ===== they return Unit

  // code blocks
  // value of block is the value of the last expression
  val aCodeBlock = {
    val y = 2
    val z = y +1

    if (z > 2) "hello" else "goodbye"
  }

  // scope of z is within the code block
}

