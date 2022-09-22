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

  import LMAXDemo.*

  @main def main(): Unit =
    val queue = new ArrayBlockingQueue[MyEvent](BUFFER_SIZE)

    val latch = new CountDownLatch(ITERATIONS)

    time(ITERATIONS,
      {
      Future {
        for {
          i <- 0 until ITERATIONS
        } yield {
          val event = MyEvent(i)
          queue.offer(event)
          latch.countDown()
        }
      }

      for {
        _ <- 0 until NUM_THREADS
      } yield Future {
        while(true) queue.poll()
      }

      latch.await()

      println("Published messages to queue")

      while(queue.size() != 0) {
        LockSupport.parkNanos(100)
      }
    })

}
