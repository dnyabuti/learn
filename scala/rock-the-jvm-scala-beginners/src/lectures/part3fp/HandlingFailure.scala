package lectures.part3fp

import scala.util.{Failure, Random, Success, Try}

object HandlingFailure extends App {

  val aSuccess = Success(3)
  val aFailure = Failure(new RuntimeException("FAILURE"))

  println(aSuccess)
  println(aFailure)

  def unsafeMethod(): String = throw new RuntimeException("MEH")

  val potentialFailure = Try(unsafeMethod())
  println(potentialFailure)

  // syntax sugar
  val anotherFailure = Try {
    unsafeMethod()
  }

  println(potentialFailure.isSuccess)

  //orElse
  def backupMethod(): String = "backup success"

  val fallBackTry = Try(unsafeMethod()).orElse(Try(backupMethod()))
  fallBackTry.foreach(println)

  // if you design API and know code might throw exception, wrap computation in Try
  def betterUnsafeMethod(): Try[String] = Failure(new RuntimeException)
  def betterBackupMethod(): Try[String] = Success("valid success")
  val betterFallback = betterUnsafeMethod() orElse betterBackupMethod()

  // has map, flatMap, filter
  println(aSuccess.map(_*2))
  println(aSuccess.flatMap(x => Success(x * 10)))
  println(aSuccess.filter(_ > 10))

  // for-comprehensions
  val hostname = "localhost"
  val port = "8080"

  def renderHTML(page: String) = println(page)

  class Connection {
    val random = new Random(System.nanoTime())
    def get(url: String): String = {
      if(random.nextBoolean()) "<html>...</html>"
      else throw new RuntimeException("Server could not handle request")
    }

    def getSafe(url: String): Try[String] = Try(get(url))
  }

  object HttpService {
    val random = new Random(System.nanoTime())

    def getConnection(host: String, port: String): Connection = {
      if (random.nextBoolean()) new Connection
      else throw new RuntimeException("Port already taken")
    }

    def getSafeConnection(host: String, port: String): Try[Connection] = Try(getConnection(host, port))
  }

  val possibleConnection = HttpService.getSafeConnection(hostname, port)
  val possibleHtlm = possibleConnection.flatMap(connection => connection.getSafe("/home"))

  possibleHtlm.foreach(renderHTML)

  // shorthand
  HttpService.getSafeConnection(hostname,port)
    .flatMap(connection => connection.getSafe("/home"))
    .foreach(renderHTML)

  for {
    connection <- HttpService.getSafeConnection(hostname,port)
    html <- connection.getSafe("/home")
  } renderHTML(html)

}
