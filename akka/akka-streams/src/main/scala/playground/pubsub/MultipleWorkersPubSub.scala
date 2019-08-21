package playground.pubsub

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, ReceiveTimeout}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

case object StreamInit
case object StreamAck
case object StreamComplete
case class StreamFail(ex: Throwable)
case class SimpleTask(contents: String)
case object StartWork

class Worker extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSubMediator.Put

  import scala.concurrent.duration._
  val mediator = DistributedPubSub(context.system).mediator
  // register to the path
  mediator ! Put(self)

  context.setReceiveTimeout(1 minute)

  override def preStart(): Unit = sendAck // request data on startup

  override def receive: Receive = {
    case SimpleTask(id) =>
      Thread.sleep(100) // mimic long running process
      log.info(s"Message: $id has been received")
      sendAck

    case ReceiveTimeout =>
        log.warning("Idle worker... requesting work...")
        context.setReceiveTimeout(context.receiveTimeout * 2)
        sendAck
    case m: Any => log.warning(s"${self.path} could not process message: $m")
  }

  def sendAck: Unit = mediator ! Send(path = "/user/master", msg = StreamAck, localAffinity = false)
}

class MasterWithRouter extends Actor with ActorLogging {

  var parent: ActorRef = null

  import akka.cluster.pubsub.DistributedPubSubMediator.Put
  val mediator = DistributedPubSub(context.system).mediator
  // register to the path
  mediator ! Put(self)

  override def receive: Receive = {
    case StartWork =>
      log.info("Starting work")
    case StreamInit =>
      log.info("Stream initialized")
      sender() ! StreamAck
      parent = sender()
    case m: SimpleTask =>
      log.info(s"Forwarding work: $m to routees")
      mediator ! Send(path = "/user/worker", msg = m, localAffinity = false)
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

object StartWorkerNodes extends App {
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

object Source1 extends App {
  val mainConfig = ConfigFactory.load("playground/clusterPubSub.conf")
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

object StartWorkerNodes2 extends App {
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

object Source2 extends App {
  val mainConfig = ConfigFactory.load("playground/clusterPubSub.conf")
  //  val config = mainConfig.getConfig("masterWithRouterApp").withFallback(mainConfig)

  implicit val system = ActorSystem("DavisCluster", mainConfig)
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
  Source(-10000 to 0).mapAsyncUnordered(parallelism = 1)(n => Future(SimpleTask(n+""))).to(actorPoweredSink).run()
}
