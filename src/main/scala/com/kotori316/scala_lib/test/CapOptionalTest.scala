package com.kotori316.scala_lib.test

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import cats.Eval
import cats.data.OptionT
import com.kotori316.scala_lib.util.CapConverter
import net.minecraftforge.common.util.LazyOptional
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.Test

private[test] class CapOptionalTest {

  import CapOptionalTest._

  @Test
  def instance(): Unit = {
    val a: Cap[String] = Cap.make("123456")
    val opt = Cap.asJava(a)

    assertEquals("123456", opt.orElse("NULL"))
    assertTrue(opt.isPresent)

    val none = Cap.asJava(Cap.empty[String])
    assertEquals("NULL", none.orElse("NULL"))
    assertTrue(!none.isPresent)
  }

  @Test
  def invalidate(): Unit = {
    val opt = Cap.asJava(Cap.make(123465))
    assertEquals(123465, opt.orElse(0))

    opt.invalidate()
    assertEquals(0, opt.orElse(0))
  }

  @Test
  def convertJavaToScala(): Unit = {
    val opt = LazyOptional.of(() => "45")
    val optT = opt.asScala
    val actual = optT.getOrElse("0")
    assertEquals("45", actual.value, s"Actual=${actual.value}, ${actual.getClass}")

    val empty = LazyOptional.empty[String]
    val emT = empty.asScala
    assertAll(
      () => assertTrue(emT.isEmpty.value),
      () => assertEquals("NULL", emT.getOrElse("NULL").value),
    )
  }

  @Test
  def mixinCheck1(): Unit = {
    // kotori_scala_LazyOptional_wrapper
    val field = classOf[LazyOptional[_]].getDeclaredField("kotori_scala_LazyOptional_wrapper")
    field.setAccessible(true)
    val opt1 = LazyOptional.of(() => "45")
    val opt2 = LazyOptional.of(() => Int.box(1264))
    val opt3 = LazyOptional.empty()
    assertAll(
      () => assertEquals(null, field.get(opt1)),
      () => assertEquals(null, field.get(opt2)),
      () => assertEquals(null, field.get(opt3)),
    )
  }

  @Test
  def mixinCheck2(): Unit = {
    val field = classOf[LazyOptional[_]].getDeclaredField("kotori_scala_LazyOptional_wrapper")
    field.setAccessible(true)
    val opt1 = Cap.asJava(Cap.make("45"))
    val opt2 = Cap.asJava(Cap.make(Int.box(1264)))
    val opt3 = Cap.asJava(Cap.empty)
    assertAll(
      () => assertTrue(null != field.get(opt1)),
      () => assertTrue(null != field.get(opt2)),
      () => assertTrue(null != field.get(opt3)),
    )
  }

  @Test
  def mixinCheck3(): Unit = {
    val integer = new AtomicInteger(0)
    val a = OptionT.apply(Eval.always(Option(integer.get())))
    val b = a.filter(_ > 0)
    assertAll(() => assertEquals(0, a.getOrElse(-1).value), () => assertEquals(-1, b.getOrElse(-1).value))

    val opt1 = Cap.asJava(a)
    val opt2 = Cap.asJava(b)
    assertAll(
      () => assertEquals(0, opt1.orElse(-1)),
      () => assertEquals(-1, opt2.orElse(-1)),
      () => assertTrue(!opt1.filter(_ > 0).isPresent),
      () => assertTrue(opt1.isPresent),
      () => assertTrue(!opt2.isPresent),
    )

    integer.set(100)
    assertAll(
      () => assertEquals(100, a.getOrElse(-1).value),
      () => assertEquals(100, b.getOrElse(-1).value),
      () => assertTrue(opt1.filter(_ > 0).isPresent),
      () => assertTrue(opt1.isPresent),
      () => assertTrue(!opt2.isPresent),
      () => assertEquals(100, opt1.orElse(-1)),
      () => assertEquals(-1, opt2.orElse(-1)),
    )
  }

  @Test
  def mixinCheck4(): Unit = {
    val integer = new AtomicInteger(1)
    val a = OptionT.apply(Eval.always(Option.when(integer.get() > 0)(integer.get())))
    assertTrue(a.isDefined.value)
    integer.set(-1)
    assertTrue(a.isEmpty.value)
    integer.set(100)

    locally {
      val opt = Cap.asJava(a)
      integer.set(-2)
      integer.set(200)
      assertTrue(opt.isPresent)
    }
    locally {
      val opt = Cap.asJava(a)
      val bool = new AtomicBoolean(false)
      integer.set(-2)
      opt.ifPresent(_ => bool.set(true))
      assertTrue(!opt.isPresent)
      assertTrue(!bool.get())
      integer.set(200)
      assertTrue(!opt.isPresent)
    }
  }
}

private[test] object CapOptionalTest extends CapConverter
