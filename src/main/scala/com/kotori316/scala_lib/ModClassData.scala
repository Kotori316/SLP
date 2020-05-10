package com.kotori316.scala_lib

import cats.Monad
import cats.data._
import cats.implicits._

trait ModClassData {
  val className: String
  val modID: String

  def isScalaObj: Boolean = className.endsWith("$")
}

object ModClassData {
  def unapply(arg: ModClassData): Option[(String, String)] = Option((arg.className, arg.modID))

  val Empty: ModClassData = new ModClassData {
    override val className = ""
    override val modID = ""
  }

  sealed class LoadingError(message: String) {
    override def toString: String = message
  }

  private[this] object NotObject extends LoadingError("Not an Object.")

  private[this] object NotClass extends LoadingError("Not a class instance.")

  case class Duplicated(id: String) extends LoadingError(s"Duplicated mod id, $id.")

  case class NotFound(id: String) extends LoadingError(s"Not Found $id")

  type VN[A] = ValidatedNec[LoadingError, A]

  def chooseObject(d1: ModClassData, d2: ModClassData): VN[ModClassData] = {
    val value = List(d1, d2).filter(_.isScalaObj)
    if (value.isEmpty) {
      Validated.invalidNec(NotObject)
    } else {
      Validated.condNec(value.size == 1,
        value.head, Duplicated(d1.modID)
      )
    }
  }

  def chooseClass(d1: ModClassData, d2: ModClassData): VN[ModClassData] = {
    val value = List(d1, d2).filter(!_.isScalaObj)
    if (value.isEmpty) {
      Validated.invalidNec(NotClass)
    } else {
      Validated.condNec(value.size == 1,
        value.head, Duplicated(d1.modID)
      )
    }
  }

  def findInstanceBy2(d1: ModClassData, d2: ModClassData): Either[NonEmptyChain[LoadingError], ModClassData] = {
    (chooseObject(d1, d2) findValid chooseClass(d1, d2)).toEither
  }

  /**
   * Checks if all mod classes in [[data]] are unique.
   *
   * @param data all mods data
   * @return [[cats.data.Validated.Valid]] if all mods are unique.
   *         [[cats.data.Validated.Invalid]] if loading error occurred.
   */
  def findInstance(data: List[ModClassData]): Validated[List[LoadingError], List[ModClassData]] = {
    val a = data.groupBy(_.modID)
      .map { case (key, value) =>
        value match {
          case head :: Nil => head.asRight
          case head :: tail => tail.foldLeft(head.asRight[NonEmptyChain[LoadingError]]) {
            case (a, b) => a >>= (findInstanceBy2(_, b))
          }
          case Nil => Monad[NonEmptyChain].pure(NotFound(key)).asLeft[ModClassData]
        }
      }
    if (a.exists(_.isLeft)) {
      Validated.invalid(a.collect { case Left(value) => value.toList }.toList.flatten)
    } else {
      Validated.valid(a.collect { case Right(value) => value }.toList)
    }
  }

}
