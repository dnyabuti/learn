package playground

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, ReceiveTimeout}
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

case object StreamInit
case object StreamAck
case object StreamComplete
case class StreamFail(ex: Throwable)
case class SimpleTask(contents: String)
case object StartWork

class Worker extends Actor with ActorLogging {
  import context.dispatcher
  import scala.concurrent.duration._
  context.setReceiveTimeout(2 second)

  var messageSource: ActorRef = null

  override def receive: Receive = {
    case SimpleTask(id) =>
      Thread.sleep(100) // mimic long running process
      log.info(s"Message: $id has been received")
      messageSource = sender()
      sender() ! StreamAck

    case ReceiveTimeout =>
      if (messageSource != null ) {
        log.warning("Idle worker... equesting work...")
        messageSource ! StreamAck // request more work
      }
    case m: Any => log.warning(s"${self.path} could not process message: $m")
  }
}

class MasterWithRouter extends Actor with ActorLogging {

  val router = context.actorOf(FromConfig.props(Props[Worker]), "clusterAwareRouter")
  var parent: ActorRef = null

  override def receive: Receive = {
    case StartWork =>
      log.info("Starting work")
    case StreamInit =>
      log.info("Stream initialized")
      sender() ! StreamAck
      parent = sender()
    case m: SimpleTask =>
      log.info(s"Forwarding work: $m to routees")
      router.tell(m, sender())
    case StreamComplete =>
      log.info("Stream complete")
      context.stop(self)
    case StreamFail(ex) =>
      log.warning(s"Stream failed: $ex")
    case StreamAck =>
      log.info("received StreamAck from {}", sender.path)
      parent ! StreamAck
    case m: Any =>
      log.info(s"Could not process message $m from {}",sender().path)

  }
}

object RouteesApp extends App {
  def startRouteeNode(port: Int) = {
    val config = ConfigFactory.parseString(
      s"""
         |akka.remote.artery.canonical.port = $port
         |""".stripMargin
    ).withFallback(ConfigFactory.load("playground/application.conf"))

    val system = ActorSystem("DavisCluster", config)
    system.actorOf(Props[Worker], "worker")
  }

  startRouteeNode(2551)
  startRouteeNode(2552)
}

object MasterWithRouterApp extends App {
  val mainConfig = ConfigFactory.load("playground/application.conf")
  //  val config = mainConfig.getConfig("masterWithRouterApp").withFallback(mainConfig)
  val config = mainConfig.getConfig("masterWithGroupRouterApp").withFallback(mainConfig)

  implicit val system = ActorSystem("DavisCluster", config)
  implicit val materializer = ActorMaterializer()
  val masterActor = system.actorOf(Props[MasterWithRouter], "master")


  val actorPoweredSink = Sink.actorRefWithAck[SimpleTask](
    masterActor,
    onInitMessage = StreamInit,
    onCompleteMessage = StreamComplete,
    ackMessage = StreamAck,
    onFailureMessage = throwable => StreamFail(throwable) // optional
  )

  Thread.sleep(10000)
  masterActor ! StartWork
  import system.dispatcher
  Source(1 to 10000).mapAsyncUnordered(parallelism = 1)(n => Future(SimpleTask(n+""))).to(actorPoweredSink).run()
}

object MoreRoutees extends App {
  def startRouteeNode(port: Int) = {
    val config = ConfigFactory.parseString(
      s"""
         |akka.remote.artery.canonical.port = $port
         |""".stripMargin
    ).withFallback(ConfigFactory.load("playground/application.conf"))

    val system = ActorSystem("DavisCluster", config)
    system.actorOf(Props[Worker], "worker")
  }

  startRouteeNode(0)
  startRouteeNode(0)
}
