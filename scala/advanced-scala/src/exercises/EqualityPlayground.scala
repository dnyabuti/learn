package exercises

import lectures.part4implicits.TypeClasses.{HTMLSerializer, User}

object EqualityPlayground extends App {

  trait Equal[T] {
    def apply(a: T, b: T): Boolean
  }

  implicit object NameEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name
  }

  object FullEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }


  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean = equalizer.apply(a, b)
  }

  val john = User("John", 32, "email@example.com")

  val anotherJohn = User("John", 41, "email2@example.com")
  println(Equal(john, anotherJohn)) // AD-HOC polymorphism -- compiler picks the correct type class instance

  //better because we have access to the entire type class interface
  println(HTMLSerializer[User].serialize(john))

  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)( implicit equalizer: Equal[T]): Boolean = equalizer(value,other)
    def !==(other: T)( implicit equalizer: Equal[T]): Boolean = !equalizer(value, other)
  }

  println(john === anotherJohn)
  println(john !== anotherJohn)

  //TYPE SAFE
  // john === 43 will not compoile
}
