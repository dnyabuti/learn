package lectures.part3concurrency

import java.util.Random

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object FuturesAndPromises extends App {

  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateMeaningOfLife
  } // implicit global is passed by the compiler

  println(aFuture.value) // returns Option[Try[Int]] -- future might have failed with exception or might not have completed
  println("waiting on future to complete")
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"The meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I have failed with $e")
  }

  Thread.sleep(3000) // wait long enough to future to complete

  // social network
  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile) = {
      println(s"${this.name} poking ${anotherProfile.name}")
    }
  }

  object SocialNetwork {
    //database
    val names = Map(
      "fb-id-1-zuck" -> "Mark",
      "fb-id-2-bill" -> "Bill",
      "fb-id-0-dummy" -> "Dummy"
    )
    val friends = Map(
      "fb-id-1-zuck" -> "fb-id-2-bill"
    )
    val random = new Random()

    // API
    def fetchProfile(id: String): Future[Profile] = Future {
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(300))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }
  val mark = SocialNetwork.fetchProfile("fb-id-1-zuck")
//  mark.onComplete {
//    case Success(markProfile) => {
//      val bill = SocialNetwork.fetchBestFriend(markProfile)
//      bill.onComplete {
//        case Success(billProfile) => markProfile.poke(billProfile)
//        case Failure(e) => e.printStackTrace()
//      }
//    }
//    case Failure(e) => e.printStackTrace()
//  }

  // functional composition of futures
  // map flatmap and filter
  val nameOnWall = mark.map(profile => profile.name)
  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val zucksBffRestricted = marksBestFriend.filter(profile => profile.name.startsWith("z"))

  for {
    mark <- SocialNetwork.fetchProfile("fb-id-1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } yield mark.poke(bill)

  Thread.sleep(1000)

  //fallback
  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown-id").recover {
    case e: Throwable => Profile("fb-id-0-dummy", "Nether")
  }

  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb-id-0-dummy")
  }

  val fallbackResult = SocialNetwork.fetchProfile("fb-id-0-dummy").fallbackTo(SocialNetwork.fetchProfile("fb-id-0-dummy"))


  // banking app
  case class User(name: String)

  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Rock the JVM banking"

    def fetchUser(name: String): Future[User] = Future {
      // database
      Thread.sleep(300)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      // fetch user
      // create transaction
      // wait for transaction to finish
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        transaction <-createTransaction(user, merchantName,cost)
      } yield transaction.status

      Await.result(transactionStatusFuture, 2.seconds) // implicit conversions (pimp my library)
    }
  }
  println(BankingApp.purchase("davis", "jeep", "dealership-12", 65000))

  // Promises
  val promise = Promise[Int]()
  val future = promise.future

  // consumer (thread 1)
  future.onComplete {
    case Success(r) => println(s"[consumer] i have received the value $r")
  }

  //thread 2 producer
  val producer = new Thread(() => {
    println("[producer]' crunching numbers")
    Thread.sleep(1000)
    promise.success(42)
    println("[producer] done")
  })
  producer.start()
  Await.result(future, 2.seconds)
}