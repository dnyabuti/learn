package lectures.part3concurrency

import scala.collection.mutable
import scala.util.Random

object ThreadCommunication extends App {

  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean  = value == 0
    def set(newValue: Int) = value = newValue
    def get = {
      val result  = value
      value = 0
      result
    }
  }

  def naiveProdCons(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("consumer waiting")
      while(container.isEmpty) {
        println("consumer actively waiting...")
      }
      println("[consumer] i have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println("[producer]  produced the value " + value)
      container.set(value)
    })
    consumer.start()
    producer.start()
  }
  naiveProdCons()

  def smartProdCons(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      container.synchronized {
        container.wait()
      }
      println("[consumer] i have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] working...")
      Thread.sleep(2000)
      val value = 42
      container.synchronized {
        println("[producer] producing " + value)
        container.set(value)
        container.notify()
      }
    })
    consumer.start()
    producer.start()
  }
  smartProdCons()

  def prodConsLargeBuffer(): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    val consumer = new Thread(() => {
      val random = new Random()
      while(true) {
        buffer.synchronized {
          if(buffer.isEmpty) {
            println("[comsumer] buffer empty. waiting...")
            buffer.wait
          }
          // there must be at least one value in the buffer
          val x = buffer.dequeue()
          println("[consumer] consumed " + x)
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random()
      var i =0

      while(true) {
        buffer.synchronized {
          if(buffer.size == capacity){
            println("[producer] buffer is full, waiting...")
            buffer.wait()
          }
          // there must be at least one empty space in buffer
          println("[producer] producing "+ i)
          buffer.enqueue(i)
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    })
    consumer.start()
    producer.start()
  }
  prodConsLargeBuffer()

  // multiple producers and multiple consumers acting on the same buffer
  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      while(true) {
        buffer.synchronized {
          while(buffer.isEmpty) {
            println(s"[comsumer-$id] buffer empty. waiting...")
            buffer.wait
          }
          // there must be at least one value in the buffer
          val x = buffer.dequeue()
          println(s"[consumer-$id] consumed " + x)
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i =0

      while(true) {
        buffer.synchronized {
          while(buffer.size == capacity){
            println(s"[producer-$id] buffer is full, waiting...")
            buffer.wait()
          }
          // there must be at least one empty space in buffer
          println(s"[producer-$id] producing $i")
          buffer.enqueue(i)
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity =3

    (1 to nConsumers).foreach(i => new Consumer(i, buffer).start())
    (1 to nProducers).foreach(i => new Producer(i, buffer, capacity).start())
  }

  multiProdCons(3, 3)

  def testNotifyAll(): Unit = {
    val bell = new Object
    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized {
        println(s"[thread-$i waiting...")
        bell.wait()
        println(s"[thread-$i] working")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println(s"[announcer] rock")
      bell.synchronized {
        bell.notify()
      }
    }).start()
  }

  testNotifyAll()

  // deadlock
  case class Friend(name: String) {
    def bow(other: Friend) = {
      this.synchronized {
        println(s"$this: I am bowing to my friend $other")
        other.rise(this)
        println(s"$this: my friend $other has risen")
      }
    }

    def rise(other: Friend): Unit = {
      this.synchronized {
        println(s"$this: i am rising to my friend $other")
      }
    }
  }
  val sam = Friend("sam")
  val peter = Friend("peter")

  new Thread(() => sam.bow(peter)).start()
  new Thread(() => peter.bow(sam)).start()

  // livelock
}
