package com.kotori316.scala_lib.util

import com.kotori316.scala_lib.util.NBTSaveFunction._
import net.minecraft.fluid.Fluid
import net.minecraft.init.Fluids
import net.minecraft.nbt.NBTTagCompound

case class FluidAmount private(fluid: Fluid, amount: Long) {
  def isEmpty = (this eq FluidAmount.empty) || (this.fluid eq Fluids.EMPTY) || amount <= 0

  def nonEmpty = !isEmpty

  def fluidEqual(that: FluidAmount) = this.fluid == that.fluid

  def add(that: FluidAmount): Option[FluidAmount] = FluidAmount.add(this, that)
}

object FluidAmount {
  final val Key_Amount = "amount"
  final val AMOUNT_BUCKET = 1000l

  def apply(fluid: Fluid, amount: Long): FluidAmount = new FluidAmount(fluid, amount)

  def add(a: FluidAmount, b: FluidAmount): Option[FluidAmount] = {
    if (a eq empty) Option(b)
    else if (b eq empty) Option(a)
    else {
      if (a fluidEqual b) {
        Some(a.copy(amount = a.amount + b.amount))
      } else {
        None
      }
    }
  }

  final val empty = FluidAmount(Fluids.EMPTY, 0)
  final val BUCKET_LAVA = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET)
  final val BUCKET_WATER = FluidAmount(Fluids.WATER, AMOUNT_BUCKET)

  implicit val saveFluidAmount: NBTSaveFunction[FluidAmount, NBTTagCompound] = new NBTSave[FluidAmount] {
    override def toNBT(a: FluidAmount) = {
      val nbt = a.fluid.toNBT
      nbt.put(Key_Amount, a.amount.toNBT)
      nbt
    }

    override def fromNBT(nbt: NBTTagCompound) = {
      val fluid = fromNBTCompound[Fluid](nbt)
      val amount = nbt.getLong(Key_Amount)
      FluidAmount(fluid, amount)
    }
  }
}
