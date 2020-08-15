package com.kotori316.scala_lib.test

import cats.Eval
import com.kotori316.scala_lib.util.CapConverter
import net.minecraftforge.common.util.LazyOptional
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
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
    assertEquals(Eval.now("45"), optT.getOrElse("0"))
  }
}

private[test] object CapOptionalTest extends CapConverter
