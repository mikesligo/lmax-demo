package com.demo.lmax

import model.{MyEvent}

import com.lmax.disruptor.*
import com.lmax.disruptor.dsl.{Disruptor, ProducerType}
import com.lmax.disruptor.util.DaemonThreadFactory
import sun.misc.Unsafe

import java.lang
import java.util.concurrent.*
import java.util.concurrent.locks.LockSupport
import scala.language.postfixOps

object LMAXDemo {
  @main def main: Unit =
    val ringBufferSize = 1024
    val threads = 4
    val iterations = 1000 * 1000 * 100

    val disruptor =
      new Disruptor[MyEvent](
        () => MyEvent(),
        ringBufferSize,
        DaemonThreadFactory.INSTANCE,
        ProducerType.SINGLE,
        new BusySpinWaitStrategy())

    setupEventHandler(disruptor, threads)

    DemoUtils.time(iterations, {
      disruptor.start()

      val latch = new CountDownLatch(iterations)
      val translator = new DepopTranslator(latch)

      for {
        _ <- 0 until iterations
      } yield disruptor.publishEvent(translator)

      latch.await()
      val ringBuffer = disruptor.getRingBuffer
      while (ringBuffer.getCursor < (iterations - 1)) {
        LockSupport.parkNanos(100)
      }
    })

  def setupEventHandler(disruptor: Disruptor[MyEvent], threads: Int) =
    for {
      i <- 0 until threads
      handler = new DemoEventHandler
    } disruptor.handleEventsWith(handler)
}

class DemoEventHandler extends EventHandler[MyEvent] {
  override def onEvent(event: MyEvent, sequence: Long, endOfBatch: Boolean): Unit = {}
}

class DepopTranslator(latch: CountDownLatch) extends EventTranslator[MyEvent] {

  override def translateTo(event: MyEvent, sequence: Long): Unit = {
    event.i = sequence
    latch.countDown()
  }
}