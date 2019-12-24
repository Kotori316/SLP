package com.kotori316.scala_lib.util

object NeighborInstance extends HighImplicit with LowImplicit

private[util] trait HighImplicit {
  implicit val IntNeighbor: Neighbor[Int] = new Neighbor[Int] {
    override def next(origin: Int): Set[Int] = Set(origin + 1, origin - 1)

    override def nextRepeat(origin: Int, n: Int, includeOrigin: Boolean): Set[Int] =
      if (includeOrigin)
        Range.inclusive(origin - n, origin + n).toSet
      else
        Range.inclusive(origin - n, origin + n).filter(_ != origin).toSet

    override def withInDistance(origin: Int, distance: Int, cond: Int => Boolean, includeOrigin: Boolean): Map[Int, Int] = {
      if (distance <= -1) return Map.empty
      val right = Range.inclusive(origin + (if (includeOrigin) 0 else 1), origin + distance)
        .takeWhile(cond)
      val left = Range.inclusive(origin - 1, origin - distance, step = -1)
        .takeWhile(cond)
      (right ++ left).map(i => i -> (origin - i).abs).toMap
    }
  }
}

private[util] trait LowImplicit {

  import Neighbor._

  import Numeric.Implicits._

  implicit def NumericNeighbor[A](implicit numeric: Numeric[A]): Neighbor[A] =
    (origin: A) => Set(origin + numeric.one, origin - numeric.one)

  implicit def PairNeighbor[A: Neighbor, B: Neighbor]: Neighbor[(A, B)] = (origin: (A, B)) => {
    val b = Set.newBuilder[(A, B)]
    b ++= origin._1.next.map(a => (a, origin._2))
    b ++= origin._2.next.map(b => (origin._1, b))
    b.result()
  }

  implicit def TripleNeighbor[A: Neighbor, B: Neighbor, C: Neighbor]: Neighbor[(A, B, C)] = (origin: (A, B, C)) => {
    val b = Set.newBuilder[(A, B, C)]
    b ++= origin._1.next.map(a => (a, origin._2, origin._3))
    b ++= origin._2.next.map(b => (origin._1, b, origin._3))
    b ++= origin._3.next.map(c => (origin._1, origin._2, c))
    b.result()
  }

}
