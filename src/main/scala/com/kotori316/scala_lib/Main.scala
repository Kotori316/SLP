package com.kotori316.scala_lib

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
    println("-" * 34)
  }
}
