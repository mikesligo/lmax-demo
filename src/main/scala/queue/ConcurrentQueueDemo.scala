package com.demo.lmax
package queue

import DemoUtils.*
import model.MyEvent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentLinkedQueue, CountDownLatch}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ConcurrentQueueDemo {
  @main def main(): Unit =
    // ConcurrentLinkedQueue is better
    val iterations = 100_000_000
    val bufferSize = 8192
    val numThreads = 4
    val queue = new ArrayBlockingQueue[MyEvent](bufferSize)

    val latch = new CountDownLatch(iterations)

    time(iterations, {
      Future {
        for {
          i <- 0 until iterations
        } yield {
          val event = MyEvent(i)
          queue.offer(event)
          latch.countDown()
        }
      }

      val parallelWork = for {
        _ <- 0 until numThreads
      } yield Future {
        while(true) queue.poll()
      }

      latch.await()

      println("Published messages to queue")

      while(queue.size() != 0) {
        LockSupport.parkNanos(1000)
      }
    })

}
