package com.kotori316.scala_lib.util

import cats.implicits._
import com.kotori316.scala_lib.util.Norm._
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class NormInstanceTest {
  @Test
  def numberTest(): Unit = {
    import com.kotori316.scala_lib.util.NormInstanceL1._
    assertEquals(5d, Norm[Int].norm(5))
    assertEquals(10d, (-10).norm)
    assertEquals(Double.box(16), Byte.box(16).norm)
  }

  @Test
  def optionTest(): Unit = {
    import com.kotori316.scala_lib.util.NormInstanceL1._
    assertEquals(0, Option.empty[Int].norm)
    assertEquals(5, Option(5).norm)
    assertEquals(5, Option(-5).norm)
  }

  @Test
  def listTest(): Unit = {
    import com.kotori316.scala_lib.util.NormInstanceL1._
    val a = 1 :: 4 :: Nil
    val b = -1 :: -4 :: Nil
    assertEquals(5, a.norm)
    assertEquals(5, b.norm)
    assertEquals(10, (a |+| b).norm)
  }

  @Test
  def pairTest(): Unit = {
    import com.kotori316.scala_lib.util.NormInstanceL1._
    val a = 1 -> 3
    val b = 4 -> 7
    assertEquals(4, a.norm)
    assertEquals(11, b.norm)
  }

  @Test
  def sqTest(): Unit = {
    assertTrue(4d === NormInstanceL2.sq(2))
  }

  @Test
  def pairNormTest(): Unit = {
    import com.kotori316.scala_lib.util.NormInstanceL2._
    val tuple: (Int, Int) = (1, 1)
    assertTrue(Math.sqrt(2) === Norm[(Int, Int)].norm(tuple))
    val tuple1: (Int, Int) = (2, 1)
    assertEquals(Math.sqrt(5), tuple1.norm)
  }

  @Test
  def foldAbleNormTest(): Unit = {
    import com.kotori316.scala_lib.util.NormInstanceL2._
    val maybeTuple: Option[(Int, Int)] = Option((1, 1))
    assertTrue(Math.sqrt(2) === maybeTuple.norm)
    val maybeDouble: Option[Double] = Option.empty[Double]
    assertTrue(0d === maybeDouble.norm)

    val ints: List[Int] = 2 :: 2 :: 5 :: 6 :: Nil
    assertTrue(Math.sqrt(4 + 4 + 25 + 36) === ints.norm)
  }
}
