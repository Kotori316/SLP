package com.kotori316.scala_lib.config

import java.nio.file.{Files, Path}
import java.util.regex.Pattern

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
    private final val ignoreSpacePattern = Pattern.compile(""" *(?<key>[^;#]+)=(?<value>.+?) *(?:|(?<comment>[;#].+))""")

    def updateValue[A](key: ConfigKey[A], allLines: scala.collection.Seq[String])(implicit EncoderDecoder: ED[A]): scala.collection.Seq[String] = {
      var found = false
      val changed = for {
        line <- allLines
        matcher = ignoreSpacePattern.matcher(line)
      } yield {
        if (matcher.matches()) {
          val keyName = matcher.group("key")
          if (keyName == key.name) {
            found = true
            if (matcher.start("comment") > 0) s"$keyName=${EncoderDecoder.toString(key.get)} ${matcher.group("comment")}"
            else s"$keyName=${EncoderDecoder.toString(key.get)}"
          } else {
            line
          }
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
        val matcher = ignoreSpacePattern.matcher(allLines.next())
        if (matcher.matches()) {
          val keyName = matcher.group("key")
          if (keyName == key.name) {
            EncoderDecoder.fromString(matcher.group("value")) match {
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
