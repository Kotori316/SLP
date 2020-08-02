package com.kotori316.scala_lib

import cats.data.Kleisli
import cats.implicits._

import scala.util.control.NonFatal

private[scala_lib] object ScalaEventAcceptor {

  type TryUnit[T] = Either[Throwable, T]
  type ScalaConsumer[T] = Kleisli[TryUnit, T, T]

  def dummy[T](): Builder[T] = new Builder(Kleisli.ask)

  class Builder[T](private[scala_lib] val c: ScalaConsumer[T]) {
    def andThen(consumer: java.util.function.Consumer[T]): Builder[T] = {
      val k: ScalaConsumer[T] = Kleisli.apply { (e: T) =>
        try {
          consumer.accept(e)
          Right(e) // Return passed value to enable chaining(E => E => E).
        } catch {
          case NonFatal(ex) => Left(ex)
        }
      }
      new Builder(c andThen k)
    }

    def build(changeStage: Runnable): java.util.function.Consumer[T] = { (e: T) =>
      c(e) match {
        case Right(_) => ()
        case Left(exception) =>
          changeStage.run()
          exception match {
            case runtimeException: RuntimeException => throw runtimeException
            case other => throw new RuntimeException(other)
          }
      }
    }
  }

}
