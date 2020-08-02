package com.kotori316.scala_lib

import java.util.concurrent.atomic.AtomicBoolean

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ScalaEventAcceptorTest {
  val runnable: Runnable = () => ()
  val event = () => "EVENT"

  @Test
  def dummy(): Unit = {
    assertEquals(Right("EVENT"), ScalaEventAcceptor.dummy[String]().c(event.apply()))
  }

  @Test
  def dummy2(): Unit = {
    val builder = ScalaEventAcceptor.dummy[String]().andThen(_ => ())
    assertEquals(Right("EVENT"), builder.c(event.apply()))
  }

  @Test
  def throw1(): Unit = {
    val e = new RuntimeException("A")
    val builder = ScalaEventAcceptor.dummy[String]().andThen(_ => throw e)
    assertEquals(Left(e), builder.c(event.apply()))
    assertThrows(classOf[RuntimeException], () => builder.build(runnable).accept("EXCEPTION"))
  }

  @Test
  def throw2(): Unit = {
    val e = new Exception("A")
    val builder = ScalaEventAcceptor.dummy[String]().andThen(_ => throw e)
    val value = builder.c(event.apply())
    assertEquals(Left(e), value)
    assertThrows(classOf[RuntimeException], () => builder.build(runnable).accept("EXCEPTION"))
  }

  @Test
  def throw3(): Unit = {
    val e = new RuntimeException("A")
    val builder = ScalaEventAcceptor.dummy[String]().andThen(_ => throw e)
    val bool = new AtomicBoolean(false)
    assertFalse(bool.get())
    val c = builder.build(() => bool.set(true))
    assertThrows(classOf[RuntimeException], () => c.accept("CHANGE_STATE"))
    assertTrue(bool.get())
  }

  @Test
  def throw4(): Unit = {
    val bool = new AtomicBoolean(false)
    val e = new RuntimeException("A")
    val builder = ScalaEventAcceptor.dummy[String]().andThen(_ => throw e).andThen(_ => bool.set(true))

    assertFalse(bool.get(), "Boolean holder not touched.")
    val c = builder.build(runnable)
    assertThrows(classOf[RuntimeException], () => c.accept("CHANGE_STATE"))
    assertFalse(bool.get(), "Accessing to bool holder was canceled.")
  }
}
