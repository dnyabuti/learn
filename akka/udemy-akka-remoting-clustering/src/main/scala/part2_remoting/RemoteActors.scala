package part2_remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSystem, Identify, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

object RemoteActors extends App {

  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("part2_remoting/remoteActors.conf"))
  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "simple-local-actor")
  localSimpleActor ! "hello, local actor!"

  // send message to remote simple actor
  // 1. Actor Selection
  val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remote-actor")
  remoteActorSelection ! "hello from the \"local\" JVM"

  // 2. Resolve actor selection to ActorRef
  import localSystem.dispatcher
  import scala.concurrent.duration._
  implicit val timeout = Timeout(3 seconds)
  val remoteActorRefFuture = remoteActorSelection.resolveOne()
  remoteActorRefFuture.onComplete {
    case Success(actorRef) => actorRef ! "I've resolved you in a Future"
    case Failure(exception) => println(s"I have failed to resolve the remote actor because: $exception")
  }

  // 3. Actor identification via messages
  class ActorResolver extends Actor with ActorLogging {
    override def receive: Receive = {
      case ActorIdentity(42, Some(actorRef)) => actorRef ! "Thank you for identifying yourself"
    }

    override def preStart(): Unit = {
      val selection = context.actorSelection("akka://RemoteSystem@localhost:2552/user/remote-actor")
      selection ! Identify(42)
    }
  }

  localSystem.actorOf(Props[ActorResolver], "localActorResolver")
}

object RemoteActors_Remote extends App {
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("part2_remoting/remoteActors.conf")
    .getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remote-actor")
  remoteSimpleActor ! "hello, remote actor!"
}
