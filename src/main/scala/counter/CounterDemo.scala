package com.demo.lmax
package counter

import scala.concurrent.Future

object CounterDemo {
  @main def main: Unit =
    val iterations = 100_000
    var counter = 0

    def countToHalf = (0 until iterations / 2).foreach { _ => counter = counter + 1 }

    val t1 = new Thread(() => countToHalf)
    val t2 = new Thread(() => countToHalf)

    t1.start()
    t2.start()

    t1.join()
    t2.join()

    println("Count is " + counter)
}
