package com.kotori316.scala_lib.config

import java.nio.file.{Files, Path}
import java.util.regex.Pattern

import scala.jdk.javaapi.CollectionConverters

class SimpleTextConfigFile(path: Path) extends ConfigFile {

  override def write[A](key: ConfigKey[A]): Unit = {
    val allLines = if (Files.exists(path)) CollectionConverters.asScala(Files.readAllLines(path)) else Seq.empty
    val newlyAdded = SimpleTextConfigFile.updateValue(key, allLines)(key.edInstance)
    Files.write(path, CollectionConverters.asJava(newlyAdded))
  }

  override def writeAll(keys: Seq[ConfigKey[_]]): Unit = {
    val allLines = if (Files.exists(path)) CollectionConverters.asScala(Files.readAllLines(path)) else Seq.empty
    val newlyAdded = keys.foldLeft(allLines) { case (lines, key) =>
      val typed = key.asInstanceOf[ConfigKey[Any]]
      SimpleTextConfigFile.updateValue(typed, lines)(typed.edInstance)
    }
    Files.write(path, CollectionConverters.asJava(newlyAdded))
  }

  override def read[A](key: ConfigKey[A]): Unit = {
    val allLines = if (Files.exists(path)) CollectionConverters.asScala(Files.readAllLines(path)) else Seq.empty
    SimpleTextConfigFile.findValue(key, allLines.iterator)(key.edInstance).foreach(key.set)
  }

  override def readAll(keys: Seq[ConfigKey[_]]): Unit = {
    val allLines = if (Files.exists(path)) CollectionConverters.asScala(Files.readAllLines(path)) else Seq.empty
    SimpleTextConfigFile.findAllValues(keys, allLines.iterator)
  }
}

object SimpleTextConfigFile {
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

  def findAllValues(keys: Seq[ConfigKey[_]], allLines: Iterator[String]): Unit = {
    while (allLines.hasNext) {
      val matcher = ignoreSpacePattern.matcher(allLines.next())
      if (matcher.matches()) {
        val keyName = matcher.group("key")
        for {
          key <- keys.find(_.name == keyName).map(_.asInstanceOf[ConfigKey[Any]])
          value <- key.edInstance.fromString(matcher.group("value"))
        } {
          key.set(value)
        }
      }
    }
  }
}