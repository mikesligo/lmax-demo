package com.demo.lmax
package queue

import DemoUtils.*
import model.MyEvent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentLinkedQueue, CountDownLatch}
import scala.concurrent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ConcurrentQueueDemo {

  import LMAXDemo.*

  @main def main(): Unit =
    val queue = new ArrayBlockingQueue[MyEvent](BUFFER_SIZE)

    val latch = new CountDownLatch(ITERATIONS)

    time(ITERATIONS,
      {
        Future {
          runIterationTimes {
            val event = MyEvent()
            queue.offer(event)
            latch.countDown()
          }
        }

        runOnDifferentThreads {
          while(true) queue.poll()
        }

        latch.await()

        println("Published messages to queue")

        while(queue.size() != 0) {
          LockSupport.parkNanos(100)
        }
      })

}
