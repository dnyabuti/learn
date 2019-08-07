package part3_clustering

import akka.actor.{Actor, ActorLogging, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberJoined, MemberRemoved, MemberUp, UnreachableMember}
import com.typesafe.config.ConfigFactory
import part3_clustering.ClusteringBasics.ClusterSubscriber

object ClusteringBasics extends App {

  class ClusterSubscriber extends Actor with ActorLogging {
    val cluster = Cluster(context.system)

    override def preStart(): Unit = {
      cluster.subscribe(
        self,
        initialStateMode = InitialStateAsEvents,
        classOf[MemberEvent],
        classOf[UnreachableMember]
      )
    }

    override def postStop(): Unit = cluster.unsubscribe(self)

    override def receive: Receive = {
      case MemberJoined(member) => log.info(s"New member joined the cluster: ${member.address}")
      case MemberUp(member) if member.hasRole("numberCruncher") =>log.info(s"Newest member - a number cruncher: ${member.address}")
      case MemberUp(member) =>log.info(s"Newest member: ${member.address}")
      case MemberRemoved(member, previousStatus) => log.info(s"Member with address ${member.address} was removed from $previousStatus ")
      case UnreachableMember(member) => log.info(s"${member.address} is unreachable")
      case m: MemberEvent => log.info(s"Event received $m")
    }
  }
  def startCluster(ports: List[Int]): Unit = ports.foreach { port =>
    val config = ConfigFactory.parseString(
      s"""
         |akka.remote.artery.canonical.port = $port
         |""".stripMargin
    ).withFallback(ConfigFactory.load("part3_clustering/clusteringBasics.conf"))

    val system = ActorSystem("DavisCluster", config) // all actor systems in a cluster must have the same name
    system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")
  }

  startCluster(List(2551,2552,0))

}

object ClusteringBasics_ManualRegistration extends App {
  val system = ActorSystem(
    "DavisCluster",
    ConfigFactory
      .load("part3_clustering/clusteringBasics.conf")
      .getConfig("manualRegistration"))
  val cluster = Cluster(system)
  def joinExistingCluster = cluster.joinSeedNodes(List(
    Address("akka", "DavisCluster", "localhost", 2551),
    Address("akka", "DavisCluster", "localhost", 2552) // or AddressFromURIString("akka://DavisCluster...)
  ))

  def joinExistingNode = cluster.joinSeedNodes(List(
    Address("akka", "DavisCluster", "localhost", 45361)))

  def joinMyself = cluster.joinSeedNodes(List(
    Address("akka", "DavisCluster", "localhost", 2555)))

  joinExistingCluster

  system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")

}
