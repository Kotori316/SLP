package com.kotori316.scala_lib.util

import cats.implicits._
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class BasisTestScala {
  @Test
  def dummy(): Unit = {
    assertDoesNotThrow(() => implicitly[Basis[Int]])
  }

  @Test
  def withInDistance(): Unit = {
    assertEquals(33, Basis.withInDistance(0, 32)(_ > 0).size)
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
    val pair1 = Basis.withInDistanceSorted[(Int, Int)]((0, 5), 6)(validArea)
    assertEquals(12, pair1.size)
    assertEquals((1, 0), pair1.last._1)
    assertEquals(6, pair1.last._2)
    assertEquals((0, 5), pair1.head._1)
    assert(pair1.map(_._1).forall(validArea))
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

    val distanceMap: Map[(Int, Int), Int] = Basis.withInDistance((8, 0), 8)(area)
    assertEquals(6, distanceMap(7 -> 5))
    val distance8 = distanceMap.filter(t => t._2 === 8)
    assertTrue(distance8.contains((0, 0)))
    assertTrue(distance8.contains((5, 5)))
  }

  @Test
  def nextInt(): Unit = {
    val int0Next = Basis.next(0)
    assertEquals(2, int0Next.size)
    assertTrue(int0Next contains 1)
    assertTrue(int0Next contains -1)
    val int1Next = Basis.next(5)
    assertTrue(List(4, 6).toSet === int1Next.toSet)
  }

  @Test
  def nextPair(): Unit = {
    val p1 = Basis.next((0, 0))
    assertEquals(Set(0 -> 1, 1 -> 0, -1 -> 0, 0 -> -1), p1.toSet)
  }
}
