package com.demo.lmax
package queue

import model.MyEvent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentLinkedQueue, CountDownLatch}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global

object ConcurrentQueueDemo {
  @main def main(): Unit =
    // ArrayBlockingQueue sucks, the blocking is awful
    val queue = new ConcurrentLinkedQueue[MyEvent]()
    val iterations = 1000 * 1000 * 2
    val threads = 4

    val latch = new CountDownLatch(iterations)

    DemoUtils.time(iterations, {
      Future {
        for {
          i <- 0 until iterations
        } yield {
          val event = MyEvent(i)
          queue.add(event)
          latch.countDown()
        }
      }

      val parallelWork = for {
        _ <- 0 until threads
      } yield Future {
        queue.poll()
      }

      latch.await()

      println("Published messages to queue")

      Await.result(Future.sequence(parallelWork), Duration.Inf)
    })

}
