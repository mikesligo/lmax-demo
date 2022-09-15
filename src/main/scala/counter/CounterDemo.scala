package com.demo.lmax
package counter

import DemoUtils.*

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future

object CounterDemo {
  @main def main: Unit =
    val iterations = 1_000_000_000
    var counter = 0

    // remove counter increment to demonstrate speedup
    // use AtomicLong
    def countToHalf = (0 until iterations / 2).foreach { _ => counter = counter + 1 }

    val t1 = new Thread(() => countToHalf)
    val t2 = new Thread(() => countToHalf)

    time(iterations, {
      t1.start()
      t2.start()

      t1.join()
      t2.join()
    })

    println("Count is " + counter)
}
