package com.kotori316.scala_lib.config

import cats.Invariant

trait ED[A] {
  def toString(a: A): String

  def fromString(string: String): Option[A]
}

object ED {
  def apply[A](implicit ed: ED[A]): ED[A] = ed

  def apply[A](encoder: A => String, decoder: String => Option[A]): ED[A] = new ED[A] {
    override def toString(a: A): String = encoder(a)

    override def fromString(string: String): Option[A] = decoder(string)
  }

  implicit final val invariantED: Invariant[ED] = new Invariant[ED] {
    override def imap[A, B](fa: ED[A])(f: A => B)(g: B => A): ED[B] = apply(
      g andThen fa.toString,
      fa.fromString _ andThen (_.map(f))
    )
  }

  implicit final val edString: ED[String] = ED(identity, Option(_))
  implicit final val edInt: ED[Int] = ED(_.toString, _.toIntOption)
  implicit final val edDouble: ED[Double] = ED(_.toString, _.toDoubleOption)
  implicit final val edBoolean: ED[Boolean] = ED(_.toString, _.toBooleanOption)
}
