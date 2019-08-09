package part3_clustering

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.pipe
import com.typesafe.config.ConfigFactory

import scala.util.Random

object ClusteringExampleDomain {
  case class ProcessFile(filename: String)
  case class ProcessLine(line: String)
  case class ProcessLineResult(count: Int)
}

class Master extends Actor with ActorLogging {
  import ClusteringExampleDomain._
  import context.dispatcher
  implicit val timeout = Timeout(3 seconds)

  val cluster = Cluster(context.system)

  var workers: Map[Address, ActorRef] = Map() // use become to avoid using var
  var pendingRemoval: Map[Address, ActorRef] = Map()

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive = handleClusterEvents.orElse(handleWorkerRegistration)

  def handleClusterEvents: Receive = {
    case MemberUp(member) if member.hasRole("worker") =>
      log.info(s"member is up: ${member.address}")

      if(pendingRemoval.contains(member.address)) {
        pendingRemoval = pendingRemoval - member.address
      }
      val workerSelection = context.actorSelection(s"${member.address}/users/worker")
      workerSelection.resolveOne().map(ref => (member.address, ref)).pipeTo(self)
    case UnreachableMember(member) =>
      log.info(s"member detected as unreachable: ${member.address}")
      val workerOption = workers.get(member.address)
      workerOption.foreach { ref =>
        pendingRemoval = pendingRemoval + (member.address -> ref)
      }
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member ${member.address} removed after $previousStatus")
      workers = workers - member.address

    case m: MemberEvent => log.info(s"A member event $m occurred")
  }

  def handleWorkerRegistration: Receive = {
    case pair: (Address, ActorRef) =>
      log.info("Registering worker: {}",pair)
      workers = workers + pair
  }

  def handleJob: Receive = {
    case ProcessFile(filename) =>
      scala.io.Source.fromFile(filename).getLines().foreach { line =>
        val workerIndex = Random.nextInt((workers -- pendingRemoval.keys).size)
        val worker: ActorRef = ((workers -- pendingRemoval.keys)).values.toSeq(workerIndex)

        worker ! ProcessLine(line)
      }
  }
}

class Aggregator extends Actor with ActorLogging {
  import ClusteringExampleDomain._

  context.setReceiveTimeout(3 seconds)
  override def receive: Receive = ???

  def online(totalCount: Int): Receive = {
    case ProcessLineResult(count) =>
      context.become(online(totalCount + count))
  }
}

class Worker extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ => // TODO
  }
}

object SeedNodes extends App {
  def createNode(port: Int, role: String, props: Props, actorName: String) = {
    val config = ConfigFactory.parseString(
      s"""
         |akka.cluster.roles = ["$role"]
         |akka.remote.artery.canonical.port = $port
       """.stripMargin
    ).withFallback(ConfigFactory.load("part3_clustering/clusteringExample.conf"))

    val system = ActorSystem("DavisCluster", config)
    system.actorOf(props, actorName)
  }
  createNode(2551, "master", Props[Master], "master")
  createNode(2552, "worker", Props[Worker], "worker")
  createNode(2553, "worker", Props[Worker], "worker")

}
