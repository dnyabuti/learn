package lectures.part3fp

import scala.util.Random

object Optionals extends App {

  val anOption: Option[Int] = Some(4)
  val noOption: Option[Int] = None

  println(anOption)

  def unsafeMethod(): String = null

  //val result = Some(unsafeMethod()) // wrong
  val result = Option(unsafeMethod())
  println(result)

  //chained methods
  def backupMethod(): String = "valid result"
  val chainedResult = Option(unsafeMethod()).orElse(Option(backupMethod()))

  // design unsafe APIs
  def betterUnsafeMethod(): Option[String] = None
  def betterBackupMethod(): Option[String] = Some("valid result")

  val betterChainedResult = betterUnsafeMethod() orElse betterBackupMethod()

  //functions
  println(anOption.isEmpty)
  println(anOption.get) // UNSAFE - DO not do this

  println(anOption.map(_ * 2))
  println(anOption.filter(_ > 10))
  println(anOption.flatMap(x => Option(x*10)))

  val config: Map[String, String] = Map(
    "host" -> "129.09.91.22",
    "port" -> "80"
  )

  class Connection {
    def connect = "connected"
  }

  object Connection {
    val random = new Random(System.nanoTime())
    def apply(host: String, port: String): Option[Connection] = {
      if(random.nextBoolean()) Some(new Connection)
      else None
    }
  }

  val host = config.get("host")
  val port = config.get("port")

  val connection = host.flatMap(h => port.flatMap(p => Connection.apply(h,p)))
  val connectionStatus = connection.map(c => c.connect)
  println(connectionStatus)
  connectionStatus.foreach(println)

  // chained calls
  config.get("host")
    .flatMap(h => config.get("port")
    .flatMap(p => Connection(h, p))
      .map(c => c.connect))
    .foreach(println)

  //for-comprehensions
  val forConnectionStatus = for {
    h <- config.get("host")
    p <- config.get("port")
    c <- Connection(h,p)
  } yield c.connect
  forConnectionStatus.foreach(println)

}
