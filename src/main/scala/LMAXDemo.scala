package com.demo.lmax

import DemoUtils.*
import model.MyEvent

import com.lmax.disruptor.*
import com.lmax.disruptor.dsl.{Disruptor, ProducerType}
import com.lmax.disruptor.util.DaemonThreadFactory
import sun.misc.Unsafe

import java.lang
import java.util.concurrent.*
import java.util.concurrent.locks.LockSupport
import scala.language.postfixOps

object LMAXDemo {
  val BUFFER_SIZE = 8192
  val NUM_THREADS = 4
  val ITERATIONS = 100_000_000

  @main def main: Unit =
    val disruptor =
      new Disruptor[MyEvent](
        () => MyEvent(),
        BUFFER_SIZE,
        DaemonThreadFactory.INSTANCE,
        ProducerType.SINGLE,
        new BusySpinWaitStrategy())

    setupEventHandler(disruptor, NUM_THREADS)

    time(ITERATIONS,
      {
        disruptor.start()

        val latch = new CountDownLatch(ITERATIONS)
        val translator = new DepopTranslator(latch)

        runIterationTimes {
          disruptor.publishEvent(translator)
        }

        latch.await()

        val ringBuffer = disruptor.getRingBuffer
        while (ringBuffer.getCursor < (ITERATIONS - 1)) {
          LockSupport.parkNanos(100)
        }
      })

  def setupEventHandler(disruptor: Disruptor[MyEvent], threads: Int) =
    (0 until threads) foreach { _ =>
      val handler = new DemoEventHandler
      disruptor.handleEventsWith(handler)
    }
}

class DemoEventHandler extends EventHandler[MyEvent] {
  override def onEvent(event: MyEvent, sequence: Long, endOfBatch: Boolean): Unit = {}
}

class DepopTranslator(latch: CountDownLatch) extends EventTranslator[MyEvent] {

  override def translateTo(event: MyEvent, sequence: Long): Unit = {
    event.someNumber = sequence
    latch.countDown()
  }
}