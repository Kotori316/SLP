package com.kotori316.scala_lib.util

import java.util.{NoSuchElementException, Optional}

import cats.Eval
import cats.data.OptionT
import net.minecraftforge.common.util._

private[scala_lib] case class LazySupplierWrapper[T](supplier: OptionT[Eval, T]) extends NonNullSupplier[T] {
  override def get(): T = supplier.getOrElse(throw new NoSuchElementException("Supplier is empty. Mixin system for `kotori_scala`(Scalable Cats Force) seems not working.")).value

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

  def getAsJava: Optional[T] = {
    import scala.jdk.javaapi.OptionConverters
    supplier.value.map(OptionConverters.toJava).value
  }
}

object LazySupplierWrapper {
  def makeOptional[T](supplier: OptionT[Eval, T]): LazyOptional[T] = {
    LazyOptional.of(LazySupplierWrapper(supplier))
  }
}
