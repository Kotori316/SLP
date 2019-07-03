package com.kotori316.scala_lib.util

import net.minecraft.fluid.Fluid
import net.minecraft.init.Fluids

case class FluidAmount private(fluid: Fluid, amount: Long) {
  def isEmpty = (this eq FluidAmount.empty) || (this.fluid eq Fluids.EMPTY) || amount <= 0

  def nonEmpty = !isEmpty

  def fluidEqual(that: FluidAmount) = this.fluid == that.fluid
}

object FluidAmount {
  def apply(fluid: Fluid, amount: Long): FluidAmount = new FluidAmount(fluid, amount)

  final val empty = FluidAmount(Fluids.EMPTY, 0)
}
