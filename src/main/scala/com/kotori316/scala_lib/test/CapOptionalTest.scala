package com.kotori316.scala_lib.test

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
}

private[test] object CapOptionalTest extends CapConverter
