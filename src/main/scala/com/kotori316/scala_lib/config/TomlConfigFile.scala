package com.kotori316.scala_lib.config

import java.nio.file.{Files, Path}

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.io.IndentStyle
import com.electronwill.nightconfig.toml.TomlFormat

import scala.util.Using

class TomlConfigFile(path: Path) extends ConfigFile {
  override def write[A](key: ConfigKey[A]): Unit = writeAll(Seq(key))

  override def writeAll(keys: Seq[ConfigKey[_]]): Unit = {
    val current = getCurrentConfig
    keys.foreach(key => current(key.name) = key.get)
    val writer = TomlFormat.instance().createWriter()
    writer.setIndent(IndentStyle.SPACES_2)
    Using(Files.newBufferedWriter(path)) { fileWriter => writer.write(current, fileWriter) }
  }

  override def read[A](key: ConfigKey[A]): Unit = readAll(Seq(key))

  override def readAll(keys: Seq[ConfigKey[_]]): Unit = {
    val current = getCurrentConfig
    keys.foreach {
      case b: BooleanKey => b.set(current.get[Boolean](b.name))
      case i: IntKey => i.set(current.getInt(i.name))
      case d: DoubleKey => d.set(current.get[Double](d.name))
      case k@GenericsKey(_, _, _, edInstance) => edInstance.fromString(current.get(k.name)).foreach(k.set)
    }
  }

  private def getCurrentConfig: CommentedConfig = {
    val parser = TomlFormat.instance().createParser()
    Using(Files.newBufferedReader(path)) { reader =>
      parser.parse(reader)
    }.getOrElse(TomlFormat.newConfig())
  }
}
