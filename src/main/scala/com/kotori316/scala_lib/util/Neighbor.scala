package com.kotori316.scala_lib.util

trait Neighbor[A] {
  def next(origin: A): Set[A]

  def nextRepeat(origin: A, n: Int, includeOrigin: Boolean): Set[A] = withInDistance(origin, n, _ => true, includeOrigin).keySet

  def withInDistance(origin: A, distance: Int, cond: A => Boolean, includeOrigin: Boolean): Map[A, Int] = {
    if (distance <= 0) return Map.empty
    val map = scala.collection.mutable.HashMap(origin -> 0)

    @scala.annotation.tailrec
    def repeat(depth: Int, neighbors: Set[A]): Unit = {
      if (depth > distance)
        return
      val before = neighbors.filter(p => map.get(p).forall(_ > depth))
      before.foreach { p => map.update(p, depth) }
      val nextPoses = before.flatMap(this.next).filter(cond)
      repeat(depth + 1, nextPoses)
    }

    repeat(1, next(origin).filter(cond))
    if (!includeOrigin)
      map.remove(origin)
    map.toMap
  }
}

object Neighbor extends cats.Invariant[Neighbor] {

  def apply[A](implicit instance: Neighbor[A]): Neighbor[A] = instance

  override def imap[U, V](fa: Neighbor[U])(f: U => V)(g: V => U): Neighbor[V] =
    (origin: V) => (g andThen fa.next andThen (_.map(f))) (origin)

  implicit class Ops[A: Neighbor](private val origin: A) {
    def next: Set[A] = Neighbor[A].next(origin)

    def nextRepeat(n: Int, includeOrigin: Boolean = false): Set[A] = Neighbor[A].nextRepeat(origin, n, includeOrigin)

    def withInDistance(distance: Int, cond: A => Boolean, includeOrigin: Boolean = false): Map[A, Int] = Neighbor[A].withInDistance(origin, distance, cond, includeOrigin)
  }

}
