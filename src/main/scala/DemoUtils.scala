package com.demo.lmax

import LMAXDemo.{ITERATIONS, NUM_THREADS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DemoUtils {

  def time[T](iterations: Long, fn: => T): T =
    val start = System.nanoTime()
    val res = fn
    val end = System.nanoTime()
    println(s"Time to process $iterations events was ${(end - start) / 1_000_000_000.0} s")
    res


  def runOnDifferentThreads(work: => Unit) = (0 until NUM_THREADS).foreach(_ => Future(work))

  def runIterationTimes(work: => Unit) = (0 until ITERATIONS).foreach(_ => work)
}
