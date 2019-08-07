package part2_remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, ActorSystem, Identify, PoisonPill, Props}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

class RemoteActorsExercise {

}

object WordCountDomain {
  case class Initialize(nWorkers: Int)
  case class WordCountTask(text: String)
  case class WordCountResult(count: Int)
  case object EndWordCount
}

class WordCountWorker extends Actor with ActorLogging {

  import WordCountDomain._

  override def receive: Receive = {
    case WordCountTask(text) =>
      log.info(s"I am processing $text")
      sender() ! WordCountResult(text.split(" ").length)
  }
}

class WordCountMaster extends Actor with ActorLogging {
  import WordCountDomain._

  val workerRouter = context.actorOf(FromConfig.props(Props[WordCountWorker]), "workerRouter")
  override def receive: Receive = onlineWithRouter(0,0)

  def onlineWithRouter(remainingTasks: Int, totalCount: Int): Receive = {
    case text: String =>
      val sentences = text.split("\\. ")
      // send sentences to worker in turn
      sentences.foreach(sentence => workerRouter ! WordCountTask(sentence))
      context.become(onlineWithRouter(remainingTasks + sentences.length, totalCount))
    case WordCountResult(count) =>
      if (remainingTasks == 1) {
        log.info(s"TOTAL RESULT: ${totalCount + count}")
        context.stop(self)
      }else {
        context.become(onlineWithRouter(remainingTasks -1, totalCount + count))
      }
  }

  def waitingToInitializeWorkers(nWorkers: Int): Receive = {
    case Initialize(nWorkers) =>
      val workers = (1 to nWorkers).map(id =>
        context.actorOf(Props[WordCountWorker],s"wordCountWorker$id"))
      context.become(online(workers.toList, 0 , 0))
//      log.info("Master initializing..")
//      identifyWorkers(nWorkers)

  }

  def identifyWorkers(nWorkers: Int): Unit = {
    /*
         1. Create actor selection for every worker from 1 to nWorkers
         2. Send identity message to actor selections
         3. Get into an initialization state while you are receiving Actor Identities
     */
    (1 to nWorkers).foreach { i =>
      val selection = context.actorSelection(s"akka://WorkersSystem@localhost:2552/user/wordCountWorker$i")
      selection ! Identify(42)
    }
    context.become(initialization(List[ActorRef](), nWorkers))
  }

  def initialization(workers: List[ActorRef] = List(), remainingWorkers: Int): Receive = {
    case ActorIdentity(42, Some(actorRef)) =>
      log.info(s"Thank you for identifying yourself")

      if (remainingWorkers == 1) {
        log.info("All workers have been identified")
        context.become(online(workers, 0, 0))
      } else context.become(initialization(actorRef :: workers, remainingWorkers - 1))
  }

  def online(workers: List[ActorRef], remainingTasks: Int, totalCount: Int): Receive = {
    case text: String =>
      val sentences = text.split("\\. ")
      // send sentences to worker in turn
      Iterator.continually(workers).flatten.zip(sentences.iterator).foreach { pair =>
        val (worker, sentence) = pair
        worker ! WordCountTask(sentence)
      }
      context.become(online(workers, remainingTasks + sentences.length, totalCount))
    case WordCountResult(count) =>
      if (remainingTasks == 1) {
        log.info(s"TOTAL RESULT: ${totalCount + count}")
        workers.foreach(_ ! PoisonPill)
        context.stop(self)
      }else {
        context.become(online(workers, remainingTasks -1, totalCount + count))
      }
  }
}

object MasterApp extends App {
  import WordCountDomain._
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
      |""".stripMargin
  ).withFallback(ConfigFactory.load("part2_remoting/remoteActorsExercise.conf"))

  val system = ActorSystem("MasterSystem", config)

  val master = system.actorOf(Props[WordCountMaster], "wordCountMaster")

  master ! Initialize(5)
  Thread.sleep(1000)

  scala.io.Source.fromFile("src/main/resources/txt/lipsum.txt").getLines().foreach { line =>
    master ! line
  }
}

object WorkersApp extends App {
//  import WordCountDomain._
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
      |""".stripMargin
  ).withFallback(ConfigFactory.load("part2_remoting/remoteActorsExercise.conf"))

  val system = ActorSystem("WorkersSystem", config)

//  (1 to 5).map(i => system.actorOf(Props[WordCountWorker], s"wordCountWorker$i")) // deploying remotely instead of this
}
