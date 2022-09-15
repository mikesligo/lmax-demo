package com.demo.lmax
package queue

import model.{MyEvent}

import java.util.concurrent.{ConcurrentLinkedQueue, CountDownLatch}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global

object ConcurrentQueueDemo {
  @main def main(): Unit =
    val queue = new ConcurrentLinkedQueue[MyEvent]
    val iterations = 1000 * 1000 * 50
    val threads = 4

    val latch = new CountDownLatch(iterations)

    DemoUtils.time(iterations, {
      Future {
        for {
          i <- 0 until iterations
          event = MyEvent(i)
          _ = queue.add(event)
          _ = latch.countDown()
        } yield ()
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
