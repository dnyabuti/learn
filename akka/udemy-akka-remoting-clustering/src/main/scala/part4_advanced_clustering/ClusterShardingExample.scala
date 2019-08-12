package part4_advanced_clustering

import java.util.{Date, UUID}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion.Passivate
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.Random

case class OysterCard(id: String, amount: Double)
case class EntryAttempt(oysterCard: OysterCard, date: Date)
case object EntryAccepted
case class EntryRejected(reason: String)
case object TerminateValidator

////////////////////////////////
// Actors
////////////////////////////////

class Turnstile(validator: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case o: OysterCard => validator ! EntryAttempt(o, new Date())
    case EntryAccepted => log.info("GREEN: Accepted")
    case EntryRejected(reason) => log.info(s"RED: $reason")
  }
}

object Turnstile {
  def props(validator: ActorRef) = Props(new Turnstile(validator))
}

class OysterClassValidator extends Actor with ActorLogging {
  // assume this actor stores a lot of data (needs sharding)
  override def preStart(): Unit = {
    super.preStart()
    log.info(s"Validator starting")
    context.setReceiveTimeout(10 seconds)
  }

  override def receive: Receive = {
    case EntryAttempt(card @ OysterCard(id, amount),_) =>
      if (amount > 2.5) sender() ! EntryAccepted
      else sender() ! EntryRejected(s"[$id] not enough funds. please top up")
    case ReceiveTimeout =>
      context.parent ! Passivate(TerminateValidator)
    case TerminateValidator => // no more messages will be received
    context.stop(self)
  }
}

////////////////////////////////
// Sharding Settings
////////////////////////////////

object TurnstileSettings {
  val numberOfShards = 10 // use 10x number of nodes in cluster
  val numberOfEntities = 100 // 10x number of shards

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case attempt @ EntryAttempt(OysterCard(cardId, _), _) =>
      val entiryId = cardId.hashCode.abs  % numberOfEntities
      (entiryId.toString, attempt)
  }

 val extractShardId: ShardRegion.ExtractShardId = {
   case attempt @ EntryAttempt(OysterCard(cardId, _), _) =>
     val shardId = cardId.hashCode.abs  % numberOfShards
     shardId.toString
   case ShardRegion.StartEntity(entityId) =>
     (entityId.toLong % numberOfShards).toString
 }
}

////////////////////////////////
// Cluster Nodes
////////////////////////////////

class TubeStation(port: Int, numberOfTurnstiles: Int) extends App {
  val config = ConfigFactory.parseString(
    s"""
       |akka.remote.artery.canonical.port = $port
       |""".stripMargin
  ).withFallback(ConfigFactory.load("part4_advanced_clustering/clusterShardingExample.conf"))
  val system = ActorSystem("DavisCluster", config)

  // setup cluster sharding
  val validatorShardRegionRef = ClusterSharding(system).start(
    typeName = "OysterCardValidator",
    entityProps = Props[OysterClassValidator],
    settings = ClusterShardingSettings(system).withRememberEntities(true),
    extractEntityId = TurnstileSettings.extractEntityId,
    extractShardId = TurnstileSettings.extractShardId
  )

  val turnstiles = (1 to numberOfTurnstiles).map(_ => system.actorOf(Turnstile.props(validatorShardRegionRef)))

  Thread.sleep(10000)
  for (_ <- 1 to 1000) {
    val randomTurnstileIndex = Random.nextInt(numberOfTurnstiles)
    val randomTurnstile = turnstiles(randomTurnstileIndex)
    randomTurnstile ! OysterCard(UUID.randomUUID().toString, Random.nextDouble() * 10)
    Thread.sleep(200)
  }
}

object PiccadillyCircus extends TubeStation(2551, 10)
object Westminster extends TubeStation(2561, 10)
object CharlingCross extends TubeStation(2571, 15)
