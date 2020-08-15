package com.kotori316.scala_lib.util

import java.util.concurrent.atomic.AtomicBoolean

import cats.Eval
import cats.data.OptionT
import cats.implicits._
import org.junit.jupiter.api.Assertions.{assertAll, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

//noinspection AccessorLikeMethodIsUnit,SpellCheckingInspection
class LazySupplierWrapperTest {
  val a = OptionT.pure[Eval]("a")
  val none = OptionT.none[Eval, String]
  val aWrapper = LazySupplierWrapper(a)
  val noneWrapper = LazySupplierWrapper(none)
  val ifEmpty: Runnable = () => ()

  @Test
  def isPresentTest(): Unit = {
    assertTrue(aWrapper.isPresent)

    assertFalse(noneWrapper.isPresent)
  }

  @Test
  def ifPresentTest(): Unit = {
    val bool1 = new AtomicBoolean(false)
    val bool2 = new AtomicBoolean(false)
    aWrapper.ifPresent((_: Any) => bool1.set(true), c(() => bool2.set(true)))
    assertAll(() => assertTrue(bool1.get()), () => assertFalse(bool2.get()))
    bool1.set(false)

    noneWrapper.ifPresent((_: Any) => bool1.set(true), c(() => bool2.set(true)))
    assertAll(() => assertFalse(bool1.get()), () => assertTrue(bool2.get()))
  }

  @Test
  def mapTest(): Unit = {
    val mapped = aWrapper.map(_ * 4)
    assertTrue((a map (_ * 4)) === mapped.supplier, s"Mapped=$mapped, expect=$a")
    assertTrue(mapped.orElse("NULL", ifEmpty) === "aaaa", s"Mapped=${mapped.orElse("NULL", ifEmpty)}")

    assertTrue(none === noneWrapper.map(s => s + "AAA").supplier, s"None check, ${noneWrapper.map(s => s + "AAA").supplier}")
    assertTrue(noneWrapper.map(s => s + "AAA").orElse("NULL", ifEmpty) === "NULL", "None check")
  }

  @Test
  def filterTest(): Unit = {
    assertAll(
      () => assertTrue(aWrapper.filter(_.length > 2).supplier === OptionT.none, "Filter length"),
      () => assertTrue(aWrapper.filter(_.length > 2).isPresent === false, "Filter length"),
      () => assertTrue(aWrapper.filter(_.length < 2).supplier === a, "Filter length"),

      () => assertTrue(noneWrapper.filter(_ => true).supplier === OptionT.none, "None filter")
    )
  }

  @Test
  def orElseTest(): Unit = {
    val bool1 = new AtomicBoolean(false)
    assertTrue(aWrapper.orElse("NULL", c(() => bool1.set(true))) === "a", s"A $aWrapper")
    assertFalse(bool1.get(), "IfEmpty not called.")

    assertTrue(noneWrapper.orElse("NULL", c(() => bool1.set(true))) === "NULL", s"A $noneWrapper")
    assertTrue(bool1.get(), "IfEmpty called.")
  }

  private def c(a: () => Unit): Runnable = () => a()
}
