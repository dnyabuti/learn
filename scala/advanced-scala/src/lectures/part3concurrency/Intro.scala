package lectures.part3concurrency

import java.util.concurrent.Executors

object Intro extends App {

  // JVM threads
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("running in parallel")
  })

  // create a jvm thread
  // give jvm the signal to start a JVM thread
  aThread.start()

  aThread.join() // blocks until aThread finishes running

  val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
  val threadBye = new Thread(() => (1 to 5).foreach(_ => println("bye")))

  // different runs produce different results based on thread scheduling
  threadHello.start()
  threadBye.start()

  // executors (reuse threads by creating a pool)
  val pool = Executors.newFixedThreadPool(10)
  pool.execute(() => println("something in the thread pool"))

  pool.execute(() => {
    Thread.sleep(1000)
    println("done after 1 second")
  })

  pool.execute(() => {
    Thread.sleep(1000)
    println("almost done")
    Thread.sleep(1000)
    println("done after two seconds")
  })

  // no more actions can be submitted
  pool.shutdown() // pool will not accept any new executions... currently running threads will continue
//  pool.execute(() => println("should not appear")) // throws an exception in the calling thread
  println(pool.isShutdown)

  def runInParallel: Unit = {
    var x = 0

    val thread1 = new Thread(() => {
      x = 1
    })

    val thread2 = new Thread(() => {
      x = 2
    })
    thread1.start()
    thread2.start()
    println(x)

    for(_ <- 1 to 10000) runInParallel
    // race condition
  }

  class BankAccount(var amount: Int) {
    override def toString: String = "" + amount
  }

  def buy(account: BankAccount, thing: String, price: Int) = {
    account.amount -= price
    println(s"I've bought $thing")
    println(s"my account is now $account")
  }

  for (_ <- 1 to 10) {
    val account = new BankAccount(50000)
    val thread1 = new Thread(() => buy(account, "shoes",3000))
    val thread2 = new Thread(() => buy(account, "phone",4000))

    thread1.start()
    thread2.start()
    Thread.sleep(100)
    if(account.amount != 43000) println(s"aha ${account.amount}")
    println()
  }

  // use synchronized
  def buySafe(account: BankAccount, thing: String, price: Int) = {
    account.synchronized {
      // no 2 threads can evaluate this at the same time
      account.amount -= price
      println(s"I've bought $thing")
      println(s"my account is now $account")
    }
  }

  // or use @volatile on the BankAccount amount field

  var x = 0
  val threads = (1 to 100).map(_ => new Thread(() => x +=1))
  threads.foreach(_.start())
  threads.foreach(_.join())
  println(x)

  // sleep fallacy
  var message = ""
  val awesomeThread = new Thread(() => {
    Thread.sleep(1000)
    println("GOT was awesome!")  //
  })

  message = "GOT ended"
  awesomeThread.start()
  Thread.sleep(200)
  println(message) // almost always GOT ended but there is a chance GOT was awesome would be the final value

  // inception thread
  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread =
    new Thread(() => {
      if (i < maxThreads){
        val newThread = inceptionThreads(maxThreads, i + 1)
        newThread.start()
        newThread.join()
      }
      println(s"Hello from thread $i")
    })

  inceptionThreads(50).start()
}
