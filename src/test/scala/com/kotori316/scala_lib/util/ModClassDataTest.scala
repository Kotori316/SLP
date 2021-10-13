package com.kotori316.scala_lib.util

import cats.data.Validated
import cats.data.Validated.Invalid
import com.kotori316.scala_lib.ModClassData
import com.kotori316.scala_lib.ModClassData.Duplicated
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

//noinspection SpellCheckingInspection
private[util] class ModClassDataTest {
  private[this] final val forge = Data("net.minecraftforge.common.ForgeMod", "forge")
  private[this] final val mc = Data(classOf[String].getName, "minecraft")
  private[this] final val quarry = Data("com.yogpc.qp.QuarryPlus", "quarryplus")
  private[this] final val tank = Data("com.kotori316.fluidtank.FluidTank ", "fluidtank")
  private[this] final val jei = Data("mezz.jei.JustEnoughItems   ", "jei")
  val normalCase = List(forge, mc, quarry, tank, jei)

  @Test
  private[util] def findInstanceNormal(): Unit = {
    val value = ModClassData.findInstance(normalCase)
    assertTrue(value.isValid, s"Normal is valid, $value")
  }

  @Test
  def findInstance2(): Unit = {
    val data = normalCase :+ quarry.copy(className = quarry.className + "$")
    val instances = ModClassData.findInstance(data)
    assertAll(
      () => assertTrue(instances.isValid, s"Including Object, $data"),
      () => assertTrue(instances.exists(_.find(_.modID == "quarryplus").exists(_.isScalaObj)))
    )
  }

  @Test
  def findInstance3(): Unit = {
    val data = normalCase :+ quarry.copy(className = "String")
    val instances = ModClassData.findInstance(data)
    assertAll(
      () => assertTrue(instances.isInvalid, s"Duplicated, $data"),
      () => instances match {
        case Invalid(e) => assertTrue(e.contains(Duplicated("quarryplus")), s"error in quarry, $e")
        case v => fail(s"What happened to $v")
      }
    )
  }

  @Test
  def findInstance4(): Unit = {
    val data = normalCase :+
      quarry.copy(className = quarry.className + "$") :+
      quarry.copy(className = "java.lang.Object" + "$")
    val instances = ModClassData.findInstance(data)
    assertAll(
      () => assertTrue(instances.isInvalid, s"Including 2 Objects, $data"),
      () => instances match {
        case Invalid(e) => assertTrue(e.contains(Duplicated("quarryplus")), s"error in quarry, $e")
        case v => fail(s"What happened to $v")
      }
    )
  }

  @Test
  def dummy(): Unit = {
    assertTrue(true)
    assertEquals(1, 3 - 2)
  }

  @Test
  def easyGoodCase1(): Unit = {
    val oneClass = List(
      Data("com.kotori316.Mod", "my_mod")
    )
    val oneInstance = ModClassData.findInstance(oneClass)
    assertEquals(Validated.valid(List(Data("com.kotori316.Mod", "my_mod"))), oneInstance)
  }

  @Test
  def easyGoodCase1_1(): Unit = {
    val fiveClasses = List(
      Data("com.kotori316.Mod1", "my_mod1"),
      Data("com.kotori316.Mod2", "my_mod2"),
      Data("com.kotori316.Mod3", "my_mod3"),
      Data("com.kotori316.Mod4", "my_mod4"),
      Data("com.kotori316.Mod5", "my_mod5"),
    )
    val fiveInstances = ModClassData.findInstance(fiveClasses).map(_.sortBy(_.modID))
    assertEquals(Validated.valid(fiveClasses), fiveInstances)
  }

  @Test
  def easyGoodCase2(): Unit = {
    val classAndObject = List(
      Data("com.kotori316.Mod", "my_mod"), // Just a class
      Data("com.kotori316.Mod$", "my_mod") // An Object
    )
    val validatedObject = ModClassData.findInstance(classAndObject)
    assertEquals(Validated.valid(List(Data("com.kotori316.Mod$", "my_mod"))), validatedObject)
  }

  @Test
  def easyErrorCase1(): Unit = {
    val mods = List(
      Data("com.kotori316.Mod1", "my_mod1"),
      Data("com.kotori316.Mod_Extended", "my_mod1"),
    )
    val validiation = ModClassData.findInstance(mods)
    assertAll(
      () => assertTrue(validiation.isInvalid),
      () => assertTrue(validiation.swap.exists(_.contains(Duplicated("my_mod1"))))
    )
  }

  case class Data(override val className: String, override val modID: String) extends ModClassData

}
