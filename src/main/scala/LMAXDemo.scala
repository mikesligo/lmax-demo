package com.demo.lmax

import com.lmax.disruptor.{BlockingWaitStrategy, BusySpinWaitStrategy, EventFactory, EventHandler, EventTranslator, RingBuffer}
import com.lmax.disruptor.dsl.{Disruptor, ProducerType}

import java.util.concurrent.{CountDownLatch, CyclicBarrier, ExecutorService, Executors, ThreadFactory, ThreadPoolExecutor}
import com.lmax.disruptor.util.DaemonThreadFactory
import sun.misc.Unsafe

import java.lang
import java.util.concurrent.locks.LockSupport
import scala.language.postfixOps

case class MyEvent(var i: Long = 0)

object LMAXDemo
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

    disruptor.start()

    val latch = new CountDownLatch(iterations)
    val translator = new DepopTranslator(latch)

    val start = System.nanoTime()

    for {
      i <- 0 until iterations
    } yield disruptor.publishEvent(translator)

    latch.await()
    val ringBuffer = disruptor.getRingBuffer
    while (ringBuffer.getCursor < (iterations - 1)) {
      LockSupport.parkNanos(100)
    }

    val end = System.nanoTime()
    println(s"Time to process $iterations events was ${(end - start) / 1_000_000_000.0} s")


  def setupEventHandler(disruptor: Disruptor[MyEvent], threads: Int) =
    for {
      i <- 0 until threads
      handler = new DemoEventHandler
    } disruptor.handleEventsWith(handler)

class DemoEventHandler extends EventHandler[MyEvent] {
  override def onEvent(event: MyEvent, sequence: Long, endOfBatch: Boolean): Unit = {}
}

class DepopTranslator(latch: CountDownLatch) extends EventTranslator[MyEvent] {

  override def translateTo(event: MyEvent, sequence: Long): Unit = {
    event.i = sequence
    latch.countDown()
  }
}