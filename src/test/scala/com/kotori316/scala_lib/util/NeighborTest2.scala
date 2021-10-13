package com.kotori316.scala_lib.util

import cats.kernel.{Group, Monoid}
import com.kotori316.scala_lib.util.Neighbor._
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

private[util] class NeighborTest2 {

  case class P(x: Int, y: Int)

  implicit val NeighborOfP: Neighbor[P] = (origin: P) => {
    val x = origin.x
    val y = origin.y
    Set(
      origin.copy(x = x + 1),
      origin.copy(x = x - 1),
      origin.copy(y = y + 1),
      origin.copy(y = y - 1),
    )
  }

  implicit val monoidOfP: Group[P] = new Group[P] {
    override def inverse(a: P): P = P(-a.x, -a.y)

    override def empty: P = P(0, 0)

    override def combine(x: P, y: P): P = P(x.x + x.y, x.y + y.y)

    override def remove(x: P, y: P): P = P(x.x - x.y, x.y - y.y)

    override def combineN(a: P, n: Int): P = P(a.x * n, a.y * n)
  }

  @Test
  def `GridWorld`(): Unit = {
    val o = Monoid[P].empty
    assertNotEquals(Set.empty[P], o.next)
  }

  @Test def `Exclude (0, 1)`(): Unit = {
    val cond: P => Boolean = p => p != P(0, 1)
    assertAll(
      () => assertFalse(cond(P(0, 1)), "Condition check"),
      () => assertTrue(cond(P(0, 0)), "Condition check"),
      () => assertTrue(cond(P(1, 0)), "Condition check"),
      () => assertTrue(cond(P(2, 0)), "Condition check"),
      () => assertTrue(cond(P(0, 3)), "Condition check"),
      () => assertTrue(cond(P(0, 2)), "Condition check"),
    )

    val result1 = Monoid[P].empty.withInDistance(1, cond)
    val result2 = Monoid[P].empty.withInDistance(2, cond)
    val result3 = Monoid[P].empty.withInDistance(3, cond)
    val result4 = Monoid[P].empty.withInDistance(4, cond)
    val mustBeNone = result3.get(P(0, 2))
    val mustBe4 = result4.get(P(0, 2))
    assertAll(
      () => assertEquals(Set(P(1, 0), P(-1, 0), P(0, -1)), result1.keySet),
      () => assertFalse(result2.contains(P(0, 2)), s"Unreachable position P(0, 2) but $result2."),
      () => assertFalse(result2.isEmpty, s"Unreachable everywhere? $result2."),
      () => assertFalse(result3.contains(P(0, 1)), s"Unreachable position P(0, 1) but $result3."),
      () => assertFalse(result4.contains(P(0, 1)), s"Unreachable position P(0, 1) but $result4."),
      () => assertEquals(None, mustBeNone),
      () => assertEquals(Option(4), mustBe4),
      () => assertEquals(Option(3), result4.get(P(-1, 2))),
    )
  }

  @Test def `Exclude y=1`(): Unit = {
    val cond = (p: P) => p.y != 1
    assertAll(
      () => assertFalse(cond(P(0, 1))),
      () => assertFalse(cond(P(1, 1))),
      () => assertFalse(cond(P(-1, 1))),
      () => assertTrue(cond(P(-1, 0))),
      () => assertTrue(cond(P(0, 0))),
      () => assertTrue(cond(P(1, 0))),
      () => assertTrue(cond(P(2, 0))),
      () => assertTrue(cond(P(0, 2))),
      () => assertTrue(cond(P(0, 3))),
      () => assertTrue(cond(P(0, -3))),
    )

    val result1 = Monoid[P].empty.withInDistance(1, cond)
    val result4 = Monoid[P].empty.withInDistance(4, cond)
    assertAll(
      () => assertEquals(Some(1), result1.get(P(0, -1))),
      () => assertEquals(Some(1), result1.get(P(1, -0))),
      () => assertEquals(Some(2), result4.get(P(1, -1))),
      () => assertEquals(Some(2), result4.get(P(-1, -1))),
      () => assertEquals(Some(2), result4.get(P(0, -2))),
      () => assertEquals(None, result4.get(P(0, 2)), "Unreachable"),
      () => assertEquals(None, result4.get(P(0, 1)), "Unreachable"),
      () => assertEquals(None, result4.get(P(0, 3)), "Unreachable"),
      () => assertEquals(None, result4.get(P(1, 2)), "Unreachable"),
    )
  }

  @Test def `Exclude y=1 and x!=1`(): Unit = {
    val cond = (p: P) => p == P(1, 1) || p.y != 1
    assertAll(
      () => assertFalse(cond(P(0, 1))),
      () => assertTrue(cond(P(1, 1))),
      () => assertFalse(cond(P(-1, 1))),
      () => assertTrue(cond(P(-1, 0))),
      () => assertTrue(cond(P(0, 0))),
      () => assertTrue(cond(P(1, 0))),
      () => assertTrue(cond(P(2, 0))),
      () => assertTrue(cond(P(0, 2))),
      () => assertTrue(cond(P(0, 3))),
      () => assertTrue(cond(P(0, -3))),
    )

    val result1 = Monoid[P].empty.withInDistance(1, cond)
    val result4 = Monoid[P].empty.withInDistance(4, cond)
    assertAll(
      () => assertEquals(Some(1), result1.get(P(0, -1))),
      () => assertEquals(Some(1), result1.get(P(1, -0))),
      () => assertEquals(Some(2), result4.get(P(1, -1))),
      () => assertEquals(Some(2), result4.get(P(-1, -1))),
      () => assertEquals(Some(2), result4.get(P(0, -2))),
      () => assertEquals(Some(4), result4.get(P(0, 2)), "Reachable"),
      () => assertEquals(Some(4), result4.get(P(2, 2)), "Reachable"),
      () => assertEquals(None, result4.get(P(0, 1)), "Unreachable"),
      () => assertEquals(None, result4.get(P(0, 3)), "Unreachable"),
      () => assertEquals(Some(3), result4.get(P(1, 2)), "Reachable"),
    )
  }
}
