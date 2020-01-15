package com.kotori316.scala_lib

import cats.data._
import cats.implicits._

trait ModClassData {
  val className: String
  val modID: String

  def isScalaObj = className.endsWith("$")
}

object ModClassData {
  def unapply(arg: ModClassData): Option[(String, String)] = Option((arg.className, arg.modID))

  val Empty: ModClassData = new ModClassData {
    override val className = ""
    override val modID = ""
  }

  sealed class LoadingError(message: String) {
    override def toString = message
  }

  private[this] object NotObject extends LoadingError("Not an Object.")

  private[this] object NotClass extends LoadingError("Not a class instance.")

  case class Duplicated(id: String) extends LoadingError(s"Duplicated mod id. $id")

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

  def findInstance(data: List[ModClassData]): Validated[List[LoadingError], List[ModClassData]] = {
    val a = data.groupBy(_.modID)
      .map { case (_, value) =>
        value.tail.foldLeft(value.head.asRight[NonEmptyChain[LoadingError]]) {
          case (a, b) => a.flatMap(findInstanceBy2(_, b))
        }
      }
    if (a.exists(_.isLeft)) {
      Validated.invalid(a.collect { case Left(value) => value.toList }.toList.flatten)
    } else {
      Validated.valid(a.collect { case Right(value) => value }.toList)
    }
  }

}
