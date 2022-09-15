package com.demo.lmax

object DemoUtils {

  def time[T](iterations: Long, fn: => T): T =
    val start = System.nanoTime()
    val res = fn
    val end = System.nanoTime()
    println(s"Time to process $iterations events was ${(end - start) / 1_000_000_000.0} s")
    res
}
