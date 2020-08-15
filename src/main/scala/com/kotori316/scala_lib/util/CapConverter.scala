package com.kotori316.scala_lib.util

import cats.Eval
import cats.data.OptionT
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional

import scala.language.implicitConversions

trait CapConverter {

  type Cap[T] = OptionT[Eval, T]

  object Cap {
    def make[T](obj: T): Cap[T] = {
      OptionT.fromOption[Eval](Option(obj))
    }

    def asJava[A](cap: Cap[A]): LazyOptional[A] = {
      LazySupplierWrapper.makeOptional(cap)
    }

    def empty[A]: Cap[A] = {
      OptionT.none
    }
  }

  implicit def toCapHelper[T](capability: Capability[T]): CapConverter.CapHelper[T] = new CapConverter.CapHelper(capability)
  implicit def toAsScalaLO[T](value: LazyOptional[T]): CapConverter.AsScalaLO[T] = new CapConverter.AsScalaLO(value)
}

object CapConverter extends CapConverter {

  implicit class CapHelper[T](val capability: Capability[T]) extends AnyVal {
    /**
     * @tparam F dummy parameter to satisfy compiler. It should be type parameter of [[net.minecraftforge.common.capabilities.ICapabilityProvider#getCapability]].
     */
    def make[F](toCheckCapability: Capability[_], instance: T): Cap[F] = {
      if (this.capability == toCheckCapability)
        Cap.make[F](instance.asInstanceOf[F])
      else
        Cap.empty[F]
    }
  }

  def transform0[T](cap: LazyOptional[T]): Eval[Option[T]] = Eval.always {
    if (cap.isPresent) {
      Option(cap.orElse(null.asInstanceOf[T]))
    } else {
      None
    }
  }

  implicit class AsScalaLO[T](val cap: LazyOptional[T]) extends AnyVal {
    def asScala: Cap[T] = OptionT(transform0[T](cap))
  }

}