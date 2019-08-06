package lectures.part3concurrency

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicReference

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.immutable.ParVector

object ParallelUtils extends App {

  // parallel collections (operations on them are handled by multiple threads at the same time)
  // par will perform slower when the number of elements is not big
  val parList = List(1,2,3).par
  val aParVector = ParVector[Int](1,2,3)

  def measure[T](operation: => T): Long = {
    val time = System.currentTimeMillis()
    operation
    System.currentTimeMillis() - time
  }

  val list = (1 to 1000000).toList
  val serialTime = measure {
    list.map(_ + 1)
  }
  val parallelTime = measure {
    list.par.map(_ + 1)
  }
  println(s"serial $serialTime")
  println(s"parallel $parallelTime")

  // map, flatMap, filter and foreach are same on parallel collections
  // be careful with reduce and fold operations as they may not behave as expected
  // reduce and fold are not associative
  println(List(1,2,3).reduce(_ - _))
  println(List(1,2,3).par.reduce(_ - _))
  // the above produces different output
  // sometimes you need synchronization on the result
  var sum: Int = 0
  (1 to 10).toList.par.foreach(sum += _)
  println("sum = "+sum)
  // above sum will not always be the same value

  //configuring - control number of threads
  // use task support
  aParVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2))

  // atomic ops and references
  val atomic =  new AtomicReference[Int](2)

  val currValue = atomic.get() // thread safe

  atomic.set(4) // thread safe set

  atomic.getAndSet(5) // thread safe

  atomic.compareAndSet(38, 56) // if value is 387 set value to 56

  atomic.updateAndGet(_ + 1) // thread safe function run

  atomic.getAndUpdate(_ + 1)

  atomic.accumulateAndGet(12, _ + _)

  atomic.getAndAccumulate(12, _ + _)

}
