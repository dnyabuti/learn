package lectures.part2oop

import java.util.Date
import java.sql.{Date => SqlDate}

import playground.{Test1, Test2}

object PackagingAndImports extends App {
  // package object
  sayHello
  println(SPEED_OF_LIGHT)

  // imports package_name.{imports separated by comma} or package_name._ to import all
  val t1 = Test1
  val t2 = Test2

  // create alias by using class/object name => newName
  val date = new Date
  val sqlDate = new SqlDate(2017,5,9)

}
