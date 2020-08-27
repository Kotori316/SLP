package com.kotori316.scala_lib.util

import java.util.concurrent.Callable

import com.kotori316.scala_lib.util.CapConverter._
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.Test

private[util] class CapConverterTest {
  private def n = throw new AssertionError()

  private final val capabilityConstructor = classOf[Capability[_]].getDeclaredConstructor(classOf[String], classOf[IStorage[_]], classOf[Callable[_]])
  capabilityConstructor.setAccessible(true)

  def capabilityInstance[T](name: String, storage: IStorage[T], callable: Callable[_ <: T]): Capability[T] = {
    capabilityConstructor.newInstance(name, storage, callable).asInstanceOf[Capability[T]]
  }

  def capabilityInstance[T](name: String): Capability[T] = {
    capabilityInstance(name, new IStorage[T] {
      override def writeNBT(capability: Capability[T], instance: T, side: Direction) = n

      override def readNBT(capability: Capability[T], instance: T, side: Direction, nbt: INBT): Unit = ()
    }, () => n)
  }

  @Test
  def makeTest(): Unit = {
    val capA = capabilityInstance[Int]("A")
    val capB = capabilityInstance[String]("B")

    val c1 = capA.make[Int](capA, 26)
    val c2 = capB.make[String](capA, "")
    assertAll(
      () => assertTrue(c1.isDefined.value),
      () => assertEquals(26, c1.getOrElse(0).value),
      () => assertTrue(c2.isEmpty.value),
    )
  }

  @Test
  def whenTest(): Unit = {
    val capA = capabilityInstance[Int]("A")
    val capB = capabilityInstance[String]("B")

    val c1 = capA.when(capA, condition = true, instance = 15)
    val c2 = capB.when(capA, n, n)

    assertAll(
      () => assertTrue(c1.isDefined.value),
      () => assertEquals(15, c1.getOrElse(0).value),
      () => assertTrue(c2.isEmpty.value),
    )
  }
}
