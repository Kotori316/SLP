package com.kotori316.scala_lib.util

import cats._
import cats.implicits._

trait Basis[A] {
  def bases: List[A]
}

object Basis {

  implicit def fromNumeric[A](implicit numeric: Numeric[A]): Basis[A] = new Basis[A] {
    override def bases: List[A] = List(numeric.one)
  }

  implicit def fromPair[A, B](implicit numericA: Numeric[A], numericB: Numeric[B]): Basis[(A, B)] = new Basis[(A, B)] {
    override def bases: List[(A, B)] = List((numericA.one, numericB.zero), (numericA.zero, numericB.one))
  }

  implicit def fromTriple[A, B, C]
  (implicit nA: Numeric[A], nB: Numeric[B], nC: Numeric[C]): Basis[(A, B, C)] = new Basis[(A, B, C)] {
    override def bases: List[(A, B, C)] = List(
      (nA.one, nB.zero, nC.zero), (nA.zero, nB.one, nC.zero), (nA.zero, nB.zero, nC.one)
    )
  }

  def next[A](pos: A)(implicit basis: Basis[A], group: Group[A]): List[A] = {
    basis.bases >>= { b => List(pos |+| b, pos |-| b) }
  }

}