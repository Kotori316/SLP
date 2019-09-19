package com.kotori316.scala_lib.util

trait Norm[-A] extends Serializable {
  def norm(a: A): Double
}

object Norm {
  def apply[A](implicit norm: Norm[A]): Norm[A] = norm

  implicit class Ops[T](val t: T) extends AnyVal {
    def norm(implicit norm: Norm[T]): Double = norm.norm(t)
  }

}
