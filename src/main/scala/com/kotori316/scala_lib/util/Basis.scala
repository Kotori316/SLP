package com.kotori316.scala_lib.util

import cats._
import cats.implicits._

trait Basis[@scala.specialized(Int) A] {
  def bases: List[A]
}

object Basis {

  implicit def fromNumeric[A](implicit numeric: Numeric[A]): Basis[A] = new Basis[A] {
    override def bases: List[A] = List(numeric.one)
  }

  implicit def fromPair[A, B](implicit numericA: Numeric[A], numericB: Numeric[B]): Basis[(A, B)] = new Basis[(A, B)] {
    override def bases: List[(A, B)] = List((numericA.one, numericB.zero), (numericA.zero, numericB.one))
  }

  implicit def fromTriple[A, B, C](implicit nA: Numeric[A], nB: Numeric[B], nC: Numeric[C]): Basis[(A, B, C)] = new Basis[(A, B, C)] {
    override def bases: List[(A, B, C)] = List((nA.one, nB.zero, nC.zero), (nA.zero, nB.one, nC.zero), (nA.zero, nB.zero, nC.one))
  }

  def next[A](pos: A)(implicit basis: Basis[A], group: Group[A]): List[A] = {
    basis.bases >>= { b => List(pos |+| b, pos |-| b) }
  }

  def withInDistance[A: Basis : Group](origin: A, dist: Int)(cond: A => Boolean): Map[A, Int] = {
    val map = scala.collection.mutable.HashMap(origin -> 0)

    def loop(p: A, depth: Int): Unit = {
      if (depth != dist + 1) {
        for {
          t <- next(p)
          if cond(t)
          if map.get(t).forall(_ > depth)
        } {
          map.update(t, depth)
          loop(t, depth + 1)
        }
      }
    }

    loop(origin, 1)
    map.toMap
  }

  def withInDistanceSorted[A: Basis : Group](origin: A, dist: Int)(cond: A => Boolean): List[(A, Int)] = {
    withInDistance(origin, dist)(cond).toList.sortBy { case (_, i) => i }
  }
}
