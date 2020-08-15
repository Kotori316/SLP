package com.kotori316.scala_lib.util

import cats.Eval
import cats.data.OptionT
import net.minecraftforge.common.util._

case class LazySupplierWrapper[T](supplier: OptionT[Eval, T]) extends NonNullSupplier[OptionT[Eval, T]] {
  override def get(): OptionT[Eval, T] = supplier

  def isPresent: Boolean = supplier.isDefined.value

  def ifPresent(consumer: NonNullConsumer[_ >: T], ifEmpty: Runnable): Unit = {
    supplier.value.value match {
      case Some(value) => consumer.accept(value)
      case None => ifEmpty.run()
    }
  }

  def map[U](func: NonNullFunction[_ >: T, _ <: U]): LazySupplierWrapper[U] = {
    LazySupplierWrapper(supplier map (t => func(t)))
  }

  def filter(predicate: NonNullPredicate[_ >: T]): LazySupplierWrapper[T] = {
    LazySupplierWrapper(supplier filter (t => predicate.test(t)))
  }

  def orElse(or: T, ifEmpty: Runnable): T = supplier.getOrElse {
    ifEmpty.run()
    or
  }.value

  def orElse(or: NonNullSupplier[_ <: T], ifEmpty: Runnable): T = supplier.getOrElse {
    ifEmpty.run()
    or.get()
  }.value

  def orThrow(exceptionSupplier: NonNullSupplier[_ <: Throwable], ifEmpty: Runnable): T = supplier.getOrElse {
    ifEmpty.run()
    throw exceptionSupplier.get()
  }.value
}

object LazySupplierWrapper {
  def makeOptional[T](supplier: OptionT[Eval, T]): LazyOptional[T] = {
    LazyOptional.of(LazySupplierWrapper(supplier)).asInstanceOf[LazyOptional[T]]
  }
}
