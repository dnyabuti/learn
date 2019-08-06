package lectures.part4implicits

import java.util.Date

object JsonSerialization extends App {

  /*
      Users, Post, Feed
      Serialize them to json
   */

  case class User(name: String, age: Int, emailL: String)
  case class Post(content: String, createdAt: Date)
  case class Feed(user: User, posts: List[Post])

  /*
      1. Create intermediate data types  (Int, String, List, Date
      2. Create type classes for conversion
      3. Serialize to Json
   */

  sealed trait JSONValue {
    def stringify: String
  }

  final case class JSONString(value: String) extends JSONValue {
    override def stringify: String = "\"" + value + "\""
  }

  final case class JSONNumber(value: Int) extends JSONValue {
    override def stringify: String = value.toString
  }

  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    override def stringify: String = values.map(_.stringify).mkString("[",",","]")
  }

  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    override def stringify: String = values.map {
      case (key, value) => "\"" + key + "\":" + value.stringify
    }
      .mkString("{",",","}")
  }

  val data = JSONObject(Map(
    "user" -> JSONString("Davis"),
    "posts" -> JSONArray(List(
      JSONString("Scala Rocks!"),
      JSONNumber(453)
    ))
  ))

  println(data.stringify)
  // type class
  // type class instances (implicit)
  // use type class instances (pimp)

}
