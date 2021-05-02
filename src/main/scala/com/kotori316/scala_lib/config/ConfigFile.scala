package com.kotori316.scala_lib.config

import java.nio.file.{Files, Path}

import scala.jdk.javaapi.CollectionConverters

trait ConfigFile {
  def write[A](key: ConfigKey[A]): Unit

  def read[A](key: ConfigKey[A]): Unit
}

object ConfigFile {
  class SimpleTextConfig(path: Path) extends ConfigFile {

    override def write[A](key: ConfigKey[A]): Unit = {
      implicit val ed: ED[A] = key.edInstance
      val allLines = CollectionConverters.asScala(Files.readAllLines(path))
      val newlyAdded = SimpleTextConfig.updateValue(key, allLines)
      Files.write(path, CollectionConverters.asJava(newlyAdded))
    }

    override def read[A](key: ConfigKey[A]): Unit = {
      implicit val ed: ED[A] = key.edInstance
      val allLines = CollectionConverters.asScala(Files.readAllLines(path))
      SimpleTextConfig.findValue(key, allLines.iterator).foreach(key.set)
    }
  }

  object SimpleTextConfig {

    def updateValue[A](key: ConfigKey[A], allLines: scala.collection.Seq[String])(implicit EncoderDecoder: ED[A]): scala.collection.Seq[String] = {
      var found = false
      val changed = for {
        line <- allLines
        split = line.split("[:#]", 2)
        commendIgnored = split(0)
      } yield {
        if (commendIgnored.startsWith(key.name)) {
          found = true
          if (split.length > 1) s"${key.name}=${EncoderDecoder.toString(key.get)} ${split(1)}"
          else s"${key.name}=${EncoderDecoder.toString(key.get)}"
        } else {
          line
        }
      }
      val newlyAdded = if (found) changed
      else changed :+ s"${key.name}=${EncoderDecoder.toString(key.get)}"
      newlyAdded
    }

    def findValue[A](key: ConfigKey[A], allLines: Iterator[String])(implicit EncoderDecoder: ED[A]): Option[A] = {
      while (allLines.hasNext) {
        val next = allLines.next()
        val commendIgnored = next.split("[:#]", 2)(0)
        if (commendIgnored.startsWith(key.name)) {
          val split = commendIgnored.split("=", 2)
          if (split.length > 1) {
            EncoderDecoder.fromString(split(1)) match {
              case a: Some[_] => return a
              case None =>
            }
          }
        }
      }
      None
    }
  }
}
