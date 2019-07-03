package com.kotori316.scala_lib.util

import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.nbt.{INBTBase, NBTTagCompound, NBTTagLong}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.IRegistry
import net.minecraftforge.registries.ForgeRegistries

trait NBTSaveFunction[A, B <: INBTBase] {

  def toNBT(a: A): B

  def fromNBT(nbt: B): A
}

object NBTSaveFunction {

  implicit class ToNbtOps[A, B <: INBTBase](a: A)(implicit save: NBTSaveFunction[A, B]) {
    def toNBT: B = save.toNBT(a)
  }

  def fromNBT[A, B <: INBTBase](b: B)(implicit save: NBTSaveFunction[A, B]) = save.fromNBT(b)

  def fromNBTCompound[A](b: NBTTagCompound)(implicit save: NBTSave[A]) = fromNBT(b)(save)

  type NBTSave[A] = NBTSaveFunction[A, NBTTagCompound]

  def by[A, B](f: A => B, g: B => A)(implicit nbtSave: NBTSave[B]): NBTSave[A] = new NBTSave[A] {
    override def toNBT(a: A) = nbtSave.toNBT(f(a))

    override def fromNBT(nbt: NBTTagCompound) = g(nbtSave.fromNBT(nbt))
  }

  implicit val stringSave: NBTSave[String] = new NBTSave[String] {
    override def toNBT(a: String) = {
      val nbt = new NBTTagCompound
      nbt.putString("string", a)
      nbt
    }

    override def fromNBT(nbt: NBTTagCompound) = nbt.getString("string")
  }
  implicit val registryNameSave: NBTSave[ResourceLocation] = NBTSaveFunction.by[ResourceLocation, String](_.toString, new ResourceLocation(_))
  implicit val longSave: NBTSaveFunction[Long, NBTTagLong] = new NBTSaveFunction[Long, NBTTagLong] {
    override def toNBT(a: Long) = new NBTTagLong(a)

    override def fromNBT(nbt: NBTTagLong) = nbt.getLong
  }

  implicit val itemSave: NBTSave[Item] = NBTSaveFunction.by[Item, ResourceLocation](_.getRegistryName, t => ForgeRegistries.ITEMS.getValue(t))
  implicit val fluidSave: NBTSave[Fluid] = NBTSaveFunction.by[Fluid, ResourceLocation](IRegistry.FLUID.getKey, IRegistry.FLUID.get)
}
