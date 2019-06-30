package com.kotori316.scala_lib

import java.time.ZonedDateTime

import cats.Show
import cats.implicits._

import scala.util.Properties

/**
  * Class to check scala library is included to jar file.
  * This class have nothing with Minecraft.
  */
object Main {
  def main(args: Array[String]): Unit = {
    println("-" * 10 + " Hello Scala! " + "-" * 10)
    val comma = ", "
    val list = 1 :: 3 :: 4 :: 15 :: Nil
    println(s"Sum of ${list.mkString(comma)} = ${list.sum}")
    println()
    println(List(ZonedDateTime.now().minusYears(1), ZonedDateTime.now(), ZonedDateTime.now().plusYears(1)).show)
    println(s"Now ${timeShow.show(ZonedDateTime.now())}")
    println()
    println(s"You are running Java ${Properties.javaVersion} and Scala ${Properties.versionString}")
    println("-" * 34)
  }

  implicit val DateShow: Show[ZonedDateTime] = time => {
    s"${time.getYear} ${time.getMonthValue}/${time.getDayOfMonth} ${time.getDayOfWeek}"
  }

  val timeShow: Show[ZonedDateTime] = time =>
    f"${time.getHour}%02d:${time.getMinute}%02d:${time.getSecond}%02d @ ${time.getZone}, ${time.getOffset}"
}
