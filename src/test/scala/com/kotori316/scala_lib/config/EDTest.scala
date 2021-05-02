package com.kotori316.scala_lib.config

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class EDTest {
  private def test[A](a: A)(implicit ed: ED[A]): Unit = {
    assertEquals(Some(a), ed.fromString(ed.toString(a)))
  }

  @Test
  def failString(): Unit = {
    val ed = ED[String]
    assertNotEquals("test_a", ed.fromString(ed.toString("a")))
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.scala_lib.config.EDTest#stringList"))
  def testString(string: String): Unit = {
    test(string)
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.scala_lib.config.EDTest#intList"))
  def testInt(i: Int): Unit = {
    test(i)
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.scala_lib.config.EDTest#stringList"))
  def testInt2(string: String): Unit = {
    val ed = ED[Int]
    assertEquals(None, ed.fromString(string))
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.scala_lib.config.EDTest#doubleList"))
  def testDouble(double: Double): Unit = {
    test(double)
  }
}

object EDTest {
  def stringList: java.util.stream.Stream[String] = java.util.stream.Stream.of(
    "", "apple", "APPLE", "52+eagle"
  )

  def intList: java.util.stream.IntStream = java.util.stream.IntStream.of(
    0, 1, 5, 10, Int.MaxValue - 1, Int.MaxValue, -1, -10, -50, Int.MinValue + 1, Int.MinValue
  )

  def doubleList: java.util.stream.DoubleStream = java.util.stream.DoubleStream.concat(
    intList.asDoubleStream(),
    java.util.stream.DoubleStream.of(
      1e9, 1d / 2, 1d / 4, 1d / 8, 1d / 16, 0.1, 0.01, 0.001, 1d / 3
    )).flatMap(d => java.util.stream.DoubleStream.of(d, -d)
  )
}
