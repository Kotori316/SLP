package com.kotori316.scala_lib.util

import cats.implicits._
import com.kotori316.scala_lib.util.NBTSaveFunction._
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{INBTBase, NBTTagCompound}

case class ItemAmount private(item: Item, amount: Long, tag: Option[NBTTagCompound] = None) {

  def isEmpty = (this eq ItemAmount.empty) || (this.item eq Items.AIR) || amount <= 0

  def nonEmpty = !isEmpty

  def toStack: ItemStack = {
    val stack = new ItemStack(item, amount.toInt)
    tag.foreach(stack.setTag)
    stack
  }

  def split(a: Long): (ItemAmount, ItemAmount) = {
    val leftAmount = Math.min(this.amount, a)
    val left = setCount(leftAmount)
    val right = shrink(leftAmount)
    (left, right)
  }

  def add(that: ItemAmount): Option[ItemAmount] = ItemAmount.add(this, that)

  def setCount(newAmount: Long): ItemAmount = copy(amount = newAmount)

  def grow(add: Long): ItemAmount = copy(amount = amount + add)

  def shrink(decrease: Long): ItemAmount = copy(amount = amount - decrease)

  def getMaxStackSize: Int = item.getMaxDamage(toStack)

  def getTranslationKey: String = item.getTranslationKey(toStack)

  def hasTag: Boolean = tag.isDefined

  def setTag(newTag: NBTTagCompound): ItemAmount = copy(tag = Option(newTag).map(_.copy()))

  def updateTag(consumer: NBTTagCompound => Unit): ItemAmount = {
    val nbt = if (hasTag) tag.get.copy() else new NBTTagCompound
    consumer(nbt)
    copy(tag = Option(nbt).filterNot(_.isEmpty))
  }

  def removeTag: ItemAmount = copy(tag = None)

  def addTag(key: String, value: INBTBase): ItemAmount = updateTag { nbt =>
    nbt.put(key, value)
  }

  def addTag[A, B <: INBTBase](key: String, value: A)(implicit save: NBTSaveFunction[A, B]): ItemAmount = updateTag { nbt =>
    nbt.put(key, save.toNBT(value))
  }

  def equalWithoutAmount(that: ItemAmount): Boolean = this.item == that.item && this.tag == that.tag
}

object ItemAmount {
  final val Key_Amount = "amount"
  final val Key_Tag = "tag"
  final val empty = ItemAmount(Items.AIR, 0)

  def apply(item: Item, amount: Long, tag: Option[NBTTagCompound] = None): ItemAmount = new ItemAmount(item, amount, tag.map(_.copy()))

  def apply(stack: ItemStack): ItemAmount = new ItemAmount(stack.getItem, stack.getCount, Option(stack.getTag).map(_.copy()))

  implicit val save: NBTSave[ItemAmount] = new NBTSave[ItemAmount] {
    override def toNBT(a: ItemAmount) = {
      val nbt = a.item.toNBT
      nbt.put(Key_Amount, a.amount.toNBT)
      a.tag.foreach(p => nbt.put(Key_Tag, p))
      nbt
    }

    override def fromNBT(nbt: NBTTagCompound) = {
      val item = fromNBTCompound(nbt)(itemSave)
      val amount = nbt.getLong(Key_Amount)
      val tag = Option(nbt).filter(_.contains(Key_Tag)).map(_.getCompound(Key_Tag))
      ItemAmount(item, amount, tag)
    }
  }

  def add(a: ItemAmount, b: ItemAmount): Option[ItemAmount] = {
    if (a == empty) b.pure[Option]
    else if (b == empty) a.pure[Option]
    else {
      if (a equalWithoutAmount b)
        Some(a.copy(amount = a.amount + b.amount))
      else
        None
    }
  }
}
