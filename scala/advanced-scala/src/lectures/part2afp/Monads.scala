package lectures.part2afp

object Monads extends App {

  // a try Monad
  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
  }

  object Attempt {
    def apply[A](a: => A): Attempt[A] =
      try {
        Success(a)
      } catch {
        case e: Throwable => Fail(e)
      }
  }

  case class Success[+A](value: A) extends Attempt[A] {
    override def flatMap[B](f: A => Attempt[B]): Attempt[B] = {
      try {
        f(value)
      } catch {
        case e: Throwable => Fail(e)
      }
    }
  }

  case class Fail(e: Throwable) extends Attempt[Nothing] {
    override def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this
  }

  /*
  prove monad laws

  1. Left identity
  Attempt(x).flatMap(f) = f(x) // for success
  Success(x).flatMap(x) = f(x) -- proved

  2. Right Identity
  attempt.flatMap(unit) = attempt
  Success(x).flatMap(x => Attempt(x)) = Attempt(x) = Success(x)
  Fail(e).flatMap(...) = Fail(e)

  3. Associativity
  attempt.flatMap(f).flatMap(g) == attempt.flatMap(x => f(x).flatMap(g))
  Fail(e).flatMap(f).flatMap(g) = Fail(e)
  Fail(e).flatMap(x => f(x).flatMap(g)) = Fail(e)

  Success(f).flatMap(f).flatMap(g) = f(v).flatMap(g) OR Fail(e)
  Success(v).flatMap(x => f(x).flatMap(g)) = f(v).flatMap(g) OR Fail(e)
*/

  val attempt = Attempt {
    throw new RuntimeException("My own monad!")
  }
  println(attempt)

  // Lazy Monad

  class Lazy[+A](value: => A) {
    // call by need
    private lazy val internalValue = value
    def use: A = internalValue
    def flatMap[B](f: (=> A) => Lazy[B]): Lazy[B] = f(internalValue)
  }

  object Lazy {
    def apply[A](value: => A): Lazy[A] = new Lazy(value)
  }

  val lazyInstance = Lazy {
    println("Learn scala")
    42
  }
  val flatMappedInstance = lazyInstance.flatMap(x => Lazy {
    10 * x
  })
  val flatMappedInstance2 = lazyInstance.flatMap(x => Lazy {
    10 * x
  })
  flatMappedInstance.use
  flatMappedInstance2.use

  /*
      monad laws
      1. Left Identity
      unit.flatMap(f) = f(v)
      Lazy(v).flatMap(f) = f(v)

      2. Right Identity
      1.flatMap(unit) = 1
      Lazy(v).flatMap(x => Lazy(x)) = Lazy(v)

      3. Associativity
      Lazy(v).flatMap(f).flatMap(g) = f(v).flatMap(g)
      Lazy(v).flatMap(x => f(x).flatMap(g)) = f(v).flatMap(g)

   */

  // Map and Flatten in terms of flatMap
  // def map[B](f: T => B): Monad[B] = flatMap(x => unit(f(x)))  // Monad[B]
  // def flatten(m: Monad[Monad[T]]): Monad[T] = m.flatMap((x: Monad[T]) => x)

  // List(1,2,3).map(_ * 2) = List(1,2,3).flatMap(x => List(x *2))
  // List(List(1,2), List(3,4)).flatten = List(List(1,2), List(3,4)).flatMap(x => x) = List(1,2,3,4)
}
