package com.kotori316.scala_lib.config

import java.io.{Reader, Writer}
import java.nio.file.{Files, Path}
import java.util.function.Supplier

import com.google.gson.{GsonBuilder, JsonElement, JsonObject}

import scala.jdk.javaapi.CollectionConverters
import scala.util.{Try, Using}

class JsonConfigFile private(writerSupplier: () => Writer, readerSupplier: () => Reader) extends ConfigFile {
  override def write[A](key: ConfigKey[A]): Unit = writeAll(Seq(key))

  override def writeAll(keys: Seq[ConfigKey[_]]): Unit = {
    val jsonObject = JsonConfigFile.createJsonMap(keys, getAsJsonObject.getOrElse(new JsonObject))
    Using(writerSupplier.apply()) { writer =>
      JsonConfigFile.gson.toJson(jsonObject, writer)
    }
  }

  private def getAsJsonObject: Try[JsonObject] = {
    Using(readerSupplier.apply()) { reader =>
      JsonConfigFile.gson.fromJson(reader, classOf[JsonObject])
    }.filter(_ != null)
  }

  override def read[A](key: ConfigKey[A]): Unit = readAll(Seq(key))

  override def readAll(keys: Seq[ConfigKey[_]]): Unit = {
    val jsonObject = getAsJsonObject
    val map = jsonObject.toOption.map(JsonConfigFile.asScalaMap(_)).getOrElse(Map.empty)
    keys.foreach { key =>
      val typed = key.asInstanceOf[ConfigKey[Any]]
      for {
        e <- map.get(typed.name)
        string <- Try(e.getAsString).toOption
        value <- typed.edInstance.fromString(string)
      } typed.set(value)
    }
  }
}

object JsonConfigFile {

  def apply(path: Path): JsonConfigFile = new JsonConfigFile(
    readerSupplier = () => Files.newBufferedReader(path),
    writerSupplier = () => Files.newBufferedWriter(path),
  )

  def apply(readerS: Supplier[Reader], writerS: Supplier[Writer]): JsonConfigFile = new JsonConfigFile(() => writerS.get(), () => readerS.get())

  private final val gson = new GsonBuilder().setLenient().setPrettyPrinting().create()

  def asScalaMap(jsonObject: JsonObject, parentName: String = ""): Map[String, JsonElement] = {
    val append = if (parentName.isEmpty) "" else parentName + "."
    CollectionConverters.asScala(jsonObject.entrySet())
      .map(e => (e.getKey, e.getValue))
      .toMap
      .flatMap {
        case (str, obj: JsonObject) => asScalaMap(obj, str)
        case (key, value) => Map(key -> value)
      }
      .map { case (str, element) => (append + str) -> element }
  }

  @scala.annotation.tailrec
  private def getParent(name: String, obj: JsonObject): Option[JsonObject] = {
    val dotPos = name.indexOf('.')
    if (dotPos == -1) {
      // the element should be stored in the obj.
      Some(obj)
    } else {
      val key = name.substring(0, dotPos)
      if (obj.has(key)) {
        obj.get(key) match {
          case next: JsonObject => getParent(name.substring(dotPos + 1), next)
          case _ => None // The child is not a json object. It's a value.
        }
      } else {
        // The child does not exist.
        val next = new JsonObject
        obj.add(key, next)
        getParent(name.substring(dotPos + 1), next)
      }
    }
  }

  def createJsonMap(keys: Seq[ConfigKey[_]], jsonObject: JsonObject): JsonObject = {
    for {
      key <- keys
      parent <- getParent(key.name, jsonObject).toList
    } {
      key match {
        case b: BooleanKey => parent.addProperty(b.configName, b.get)
        case i: IntKey => parent.addProperty(i.configName, i.get)
        case d: DoubleKey => parent.addProperty(d.configName, d.get)
        case g: GenericsKey[Any] => parent.addProperty(g.configName, g.edInstance.toString(key.get))
      }
    }
    jsonObject
  }
}
