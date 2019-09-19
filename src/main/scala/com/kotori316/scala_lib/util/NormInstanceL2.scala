package com.kotori316.scala_lib.util

import cats._
import cats.implicits._
import com.kotori316.scala_lib.util.Norm._

import scala.language.{higherKinds, implicitConversions}

object NormInstanceL2 {
  implicit val numberNorm: Norm[Number] = (a: Number) => Math.abs(a.doubleValue())
  implicit val byteNorm: Norm[Byte] = (a: Byte) => a.abs
  implicit val shortNorm: Norm[Short] = (a: Short) => a.abs
  implicit val intNorm: Norm[Int] = (a: Int) => a.abs
  implicit val longNorm: Norm[Long] = (a: Long) => a.abs
  implicit val floatNorm: Norm[Float] = (a: Float) => a.abs
  implicit val doubleNorm: Norm[Double] = (a: Double) => a.abs

  implicit def pairNorm[A: Norm, B: Norm]: Norm[(A, B)] = (a: (A, B)) => Math.sqrt(sq(a._1.norm) + sq(a._2.norm))

  implicit def foldAbleNorm[A, F[_] : Foldable](implicit n: Norm[A], m: Monoid[A]): Norm[F[A]] = (a: F[A]) => Math.sqrt(a.foldLeft(0d) { case (a, b) => a + sq(b.norm) })

  def sq(double: Double): Double = double * double
}
