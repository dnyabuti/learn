package lectures.part4implicits

object TypeClasses extends App {

  trait HTMLWritable {
    def toHtml: String
  }

  case class User(name: String, age: Int, email: String) extends  HTMLWritable {
    override def toHtml: String = s"<div>$name $age years old <a href=$email/></div>"
  }

  val john = User("John", 32, "email@example.com")

  // better way

  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  implicit object UserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>${user.name} ${user.age} years old <a href=${user.email}/></div>"
  }

  println(UserSerializer.serialize(User("John", 32, "email@example.com")))

  // why is it better....
  /*
      1. We can define serializers for other types eg. for date below
      2. We can define multiple serializers for a certain type PartialUserSerializer
   */

  import java.util.Date
  object DateSerializer extends HTMLSerializer[Date] {
    override def serialize(date: Date): String = s"<div> ${date.toString}</div>"
  }

  object PartialUserSerializer extends HTMLSerializer[User] { // second serializer for User
    override def serialize(user: User): String = s"<div>${user.name}</div>"
  }

  // HTMLSerializer is a type class.. all implementers are type class instances



  /// better

  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]) = serializer
  }

  println(HTMLSerializer.serialize(john))

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div>$value</div>"
  }

  println(HTMLSerializer.serialize(41))

  //
  implicit class HTMLEnrihment[T](value: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  println(john.toHTML) // println(new HTMLEnrichment[User](john).toHtml(UserSerializer))

  println(2.toHTML)

  println(john.toHTML(PartialUserSerializer))

  /*

      - type class -- HTMLSerializer[T]
      - type class instances (some of which are implicit) UserSerializer, IntSerializer ...
      - conversion with implicit classes HTMLEnrichment
   */

  // context bounds
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String =
    s"<html><body>${content.toHTML(serializer)}</body></html>"

  def htmlSugar[T: HTMLSerializer](content: T): String = {
    val serializer = implicitly[HTMLSerializer[T]]
    s"<html><body>${content.toHTML(serializer)}</body></html>"
  }

  // implicitly
  case class Permissions(mask: String)
  implicit val defualtPermissions: Permissions = Permissions("0744")

  // in some other parts of code
  val standardPerms = implicitly[Permissions] // surface the implicit value for permissions
  println(standardPerms)
}
