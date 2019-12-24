package com.kotori316.scala_lib.util

import cats.implicits._
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

private[util] class NeighborTest {

  import Neighbor._
  import NeighborInstance._

  @Test
  def instance(): Unit = {
    assertNotNull(Neighbor[Int], "Int instance")
    assertSame(IntNeighbor, Neighbor[Int], "Specialized for Int")
    assertNotNull(Neighbor[Long], "Long instance")
    assertNotNull(Neighbor[Short], "Short instance")
    assertNotNull(Neighbor[Byte], "Byte instance")

    assertNotNull(Neighbor[(Int, Int)], "Pair of Int instance")
    assertNotNull(Neighbor[(Short, Short)], "Pair of Short instance")
    assertNotNull(Neighbor[(Long, Long)], "Pair of Long instance")
  }

  @Test
  def nextInt(): Unit = {
    assertEquals(Set(1, -1), 0.next)
    assertEquals(Set(2, 0), 1.next)
    assertEquals(Set(46272, 46270), 46271.next)
  }

  @Test
  def nextIntTuple(): Unit = {
    assertEquals(Set((1, 0), (-1, 0), (0, 1), (0, -1)), (0, 0).next)
    assertEquals(Set((2, 5), (2, 3), (3, 4), (1, 4)), (2, 4).next)
  }

  @Test
  def nextLong(): Unit = {
    assertEquals(Set(1L, -1L), 0L.next)
    assertEquals(Set(2L, 0L), 1L.next)
    assertEquals(Set(46272L, 46270L), 46271L.next)
    assertEquals(Set(Int.MaxValue.toLong + 1001L, Int.MaxValue.toLong + 999L), (Int.MaxValue.toLong + 1000L).next)
  }

  @Test
  def repeatInt(): Unit = {
    assertEquals(Set.empty[Int], Neighbor.Ops(0).nextRepeat(0))
    assertEquals(Set.empty[Int], Neighbor.Ops(0).nextRepeat(-1))
    assertEquals(Set(-1, 1), Neighbor.Ops(0).nextRepeat(1))
    assertEquals(Set(-2, -1, 1, 2), Neighbor.Ops(0).nextRepeat(2))
    for (i <- Range.inclusive(3, 100)) {
      assertDoesNotThrow(new Executable {
        override def execute(): Unit = assertEquals(Range.inclusive(-i, i).toSet - 0, Neighbor.Ops(0).nextRepeat(i))
      })
    }
  }

  @Test
  def condAInt(): Unit = {
    type Cond = Int => Boolean
    val a: Cond = _.abs < 5
    val byA = 0.withInDistance(8, a)
    assertAll(
      () => assertTrue(byA.contains(4), "A has 4."),
      () => assertTrue(byA.contains(-4), "A has -4."),
      () => assertFalse(byA.contains(5), "A doesn't have 5."),
      () => assertFalse(byA.contains(-5), "A doesn't have -5."),
      () => assertEquals(4, byA(4)),
    )
  }

  @Test
  def condBInt(): Unit = {
    val b: Int => Boolean = i => i <= 7 && i >= 0
    val byB = 2.withInDistance(2, b)
    assertAll(
      () => assertTrue(byB.contains(0)),
      () => assertTrue(byB.contains(4)),
      () => assertFalse(byB.contains(5), "A doesn't have 5."),
      () => assertFalse(byB.contains(-1), "A doesn't have -1."),
    )
  }

  @Test
  def condCInt(): Unit = {
    val c: Int => Boolean = i => (i / 4) % 2 == 0
    val byC1 = 1.withInDistance(4, c)
    val byC2 = 5.withInDistance(18, c)
    val byC3 = 4.withInDistance(90, c)
    val byC4 = 8.withInDistance(67, c)
    assertAll(
      () => assertTrue(Range(-2, 3).filter(_ != 1).forall(byC1.keySet)),
      () => assertEquals(Map.empty, byC2),
      () => assertNotEquals(Map.empty, byC3),
      () => assertEquals(7, byC3.size),
      () => assertEquals(1, byC3(3)),
      () => assertEquals(2, byC3(2)),
      () => assertEquals(3, byC3(1)),
      () => assertEquals(Range.inclusive(9, 11).map(i => i -> (i - 8)).toMap, byC4),
    )
  }

  @Test
  def repeatPairInt(): Unit = {
    assertEquals(Set(
      (2, 0), (1, 0), (1, 1), (0, 2), (0, 1), (-2, 0), (-1, 0), (-1, -1), (0, -2), (0, -1), (1, -1), (-1, 1)
    ), (0, 0).nextRepeat(2))
    assertEquals(Set((1, 0), (-1, 0), (0, 1), (0, -1)), (0, 0).nextRepeat(1))
    assertTrue((0, 0).nextRepeat(100).nonEmpty)
  }

  @Test
  def invariantNeighbor(): Unit = {
    case class Pos2D(x: Int, y: Int)
    implicit val neighborPos: Neighbor[Pos2D] = Neighbor.imap(Neighbor[(Int, Int)])(Pos2D.tupled) {
      case Pos2D(x, y) => (x, y)
    }
    assertEquals(Set(
      Pos2D(1, 0), Pos2D(-1, 0), Pos2D(0, 1), Pos2D(0, -1)
    ), Pos2D(0, 0).nextRepeat(1))


    val validArea = (List(0, 5) >>= { y => List.range(0, 6).map(x => Pos2D(x, y)) }).toSet ++
      List.range(0, 6).map(y => Pos2D(0, y)).toSet ++
      List.range(2, 6).map(y => Pos2D(5, y)).toSet
    val distanceMap: Map[Pos2D, Int] = Pos2D(0, 0).withInDistance(30, validArea)
    assertEquals(validArea - Pos2D(0, 0), distanceMap.keySet)
    assertTrue(distanceMap.valuesIterator.max < 30)
  }

  @Test
  def withInDistance(): Unit = {
    assertAll(
      () => assertEquals(33, 0.withInDistance(32, _ >= 0, includeOrigin = true).size),
      () => assertEquals(33 - 1, 0.withInDistance(32, _ >= 0).size),
      () => assertEquals(0, 0.nextRepeat(0).size),
      () => assertEquals(1, 0.nextRepeat(0, includeOrigin = true).size),
      () => assertEquals(6, (0, 0, 0).nextRepeat(1).size),
      () => assertEquals(7, (0, 0, 0).nextRepeat(1, includeOrigin = true).size),
    )
  }

  @Test
  def withInDistanceA(): Unit = {
    /*
      5******
      4*    *
      3*    *
      2*    *
      1*
      0*****@
       012345
     */
    val validArea = (List(0, 5) >>= { y => List.range(0, 6).map(x => (x, y)) }).toSet ++
      List.range(0, 6).map(y => (0, y)).toSet
    val pair1 = (0, 5).withInDistance(6, validArea, includeOrigin = true).toList.sortBy { case (_, i) => i }
    assertAll(
      () => assertEquals(12, pair1.size),
      () => assertEquals((1, 0), pair1.last._1),
      () => assertEquals(6, pair1.last._2),
      () => assertEquals((0, 5), pair1.head._1),
      () => assert(pair1.map(_._1).forall(validArea)),
    )
  }

  @Test
  def withInDistanceB(): Unit = {
    /*
    5*********
    4*       *
    3*       *
    2*       *
    1*       *
    0********@
     012345678
     */
    val area = (0 to 8).flatMap(x => List(0, 5).map(y => (x, y))).toSet ++
      (0 to 5).flatMap(y => List(0, 8).map(x => (x, y))).toSet

    val distanceMap: Map[(Int, Int), Int] = (8, 0).withInDistance(8, area)
    val distance8 = distanceMap.filter(t => t._2 === 8)
    assertAll(
      () => assertEquals(6, distanceMap(7 -> 5)),
      () => assertTrue(distance8.contains((0, 0))),
      () => assertTrue(distance8.contains((5, 5))),
    )
  }

  @Test
  def nextInt2(): Unit = {
    val int0Next = 0.next
    val int1Next = 5.next
    assertAll(
      () => assertEquals(2, int0Next.size),
      () => assertTrue(int0Next contains 1),
      () => assertTrue(int0Next contains -1),
      () => assertTrue(List(4, 6).toSet === int1Next),
    )
  }

  @Test
  def nextPair(): Unit = {
    val p1 = (0, 0).next
    assertEquals(Set(0 -> 1, 1 -> 0, -1 -> 0, 0 -> -1), p1)
  }
}
