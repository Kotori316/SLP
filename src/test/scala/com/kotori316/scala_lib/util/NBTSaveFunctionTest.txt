package com.kotori316.scala_lib.util

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import NBTSaveFunction._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation

class NBTSaveFunctionTest {
  @Test
  def string(): Unit = {
    val s1 = "String"
    val nbt = s1.toNBT
    assertEquals(classOf[NBTTagCompound], nbt.getClass)
    val s2 = NBTSaveFunction.fromNBTCompound[String](nbt)
    assertEquals("String", s2)
  }

  @Test
  def resourceLocation(): Unit = {
    val location = new ResourceLocation("quarryplus", "pump")
    assertEquals(location, fromNBT[ResourceLocation, NBTTagCompound](location.toNBT))
  }
}
