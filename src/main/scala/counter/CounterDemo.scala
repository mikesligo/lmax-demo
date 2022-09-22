package com.demo.lmax
package counter

import DemoUtils.*

import sun.misc.Unsafe

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import scala.concurrent.Future

// normal (6s) (volatile doesn't really help because the opposing thread isn't blocked)
// add volatile (22s)
// add synchronised (33s)
// use AtomicLong with CAS (18s)

object CounterDemo {
  @main def main: Unit =
    val iterations = 1_000_000_000
    var counter = 0

    def countToHalf = (0 until iterations / 2).foreach { _ => {
      counter = counter + 1
    } }

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


//import java.lang.reflect.Field
//val f = classOf[Unsafe].getDeclaredField("theUnsafe")
//f.setAccessible(true)
//@volatile val unsafe = f.get(null).asInstanceOf[Unsafe]

