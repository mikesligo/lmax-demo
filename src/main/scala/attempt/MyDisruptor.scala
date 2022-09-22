package com.demo.lmax
package attempt

import model.MyEvent

import com.lmax.disruptor.util.Util

import java.util
import java.util.ArrayList as JArray
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.LockSupport
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class MyBuffer(data: JArray[MyEvent], var consumerCursor: Long = -1) {
  def entryFor(offset: Long) = data.get((offset % data.size).toInt)
}

case class MyConsumer(buffer: MyBuffer, finishAfter: Long) {
  var consumerSequence: Long = buffer.consumerCursor
  var iterations: Long = 0

  def consume =
    while (iterations <= finishAfter) {
      val cursor = buffer.consumerCursor
      if (cursor > consumerSequence) {
        val unsafe = Util.getUnsafe
        unsafe.loadFence()
        consumerSequence = cursor
        val entry = buffer.entryFor(consumerSequence) // got entry
        println(entry)
        consumerSequence = consumerSequence + 1
        iterations = iterations + 1
        unsafe.storeFence()
      } else LockSupport.parkNanos(1)
    }
}

case class MyProducer(buffer: MyBuffer) {
  private var producerSlot: Long = 0

  def produce(index: Int) =
    val entry = buffer.entryFor(producerSlot)
    entry.someNumber = index
    buffer.consumerCursor = producerSlot
    producerSlot = producerSlot + 1
}

object MyDisruptor {
  @main def main: Unit =
    val size = 10
    val iterations = 1000000
    val numThreads = 4
    val inputArray = new JArray[MyEvent](size)
    for {
      _ <- 0 until size
    } yield inputArray.add(MyEvent())

    val ringBuffer = MyBuffer(inputArray)


    val producer = MyProducer(ringBuffer)

    val latch = new CountDownLatch(iterations)

    DemoUtils.time(iterations, {
      Future {
        for {
          i <- 0 until iterations
        } yield {
          producer.produce(i)
          latch.countDown()
        }
      }

      val consumers = for {
        _ <- 0 until numThreads
      } yield Future {
        val consumer = MyConsumer(ringBuffer, iterations / numThreads)
        consumer.consume
        consumer
      }

      latch.await()

      println("Published messages to queue")

      Await.result(Future.sequence(consumers), Duration.Inf)
    })
    Thread.sleep(2000)


}
