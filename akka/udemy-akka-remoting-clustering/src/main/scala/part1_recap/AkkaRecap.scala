package part1_recap

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, PoisonPill, Props, Stash, SupervisorStrategy}
import akka.util.Timeout

object AkkaRecap extends App {

  class SimpleActor extends Actor with ActorLogging with Stash {
    override def receive: Receive  = {
      case "createChild" => {
        val childActor = context.actorOf(Props[SimpleActor], "myChild")
        childActor ! "hello from parent actor"
      }
      case "stashThis" => stash()
      case "change" => {
        unstashAll()
        context.become(anotherHandler)
      }
      case message => log.info(s"I received $message")
    }

    override def preStart(): Unit = {
      log.info("Actor starting...")
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: RuntimeException => Restart
      case _ => Stop
    }

    def anotherHandler: Receive = {
      case "stashThis" => log.info(s"processed stashed message")
      case message => log.info(s"In anotherHandler and received: $message")
    }
  }

  val system = ActorSystem("AkkaRecap")
  val actor = system.actorOf(Props[SimpleActor], "simple-actor")
  actor ! "stashThis"
  actor ! "createChild"
  actor ! "hello"
  actor ! "change"
  actor ! "hello"

  import scala.concurrent.duration._
  import system.dispatcher
  system.scheduler.scheduleOnce(2 seconds){
    actor ! "delayed message"
  }

  // FSM ask
  import akka.pattern.ask
  val anotherActor  = system.actorOf(Props[SimpleActor], "another-simple-actor")
  implicit val timeout = Timeout(10 seconds)
  val future = actor ? "question"

  // use ask in conjunction with pipe pattern
  import akka.pattern.pipe
  future.mapTo[String].pipeTo(anotherActor)

  // stopping
//  actor ! PoisonPill
}
