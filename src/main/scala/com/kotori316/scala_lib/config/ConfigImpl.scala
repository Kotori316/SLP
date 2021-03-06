package com.kotori316.scala_lib.config

import java.util.NoSuchElementException

import scala.collection.immutable.SeqMap

class ConfigImpl extends ConfigTemplate {
  protected var settings: Map[ConfigKey[_], Any] = SeqMap.empty

  override def get[A](key: ConfigKey[A]): A = {
    val option = settings.get(key).map(_.asInstanceOf[A])
    option.getOrElse(throw new NoSuchElementException(s"Config $key not found in this config."))
  }

  override def set[A](key: ConfigKey[A], value: A): Unit = {
    if (settings.contains(key))
      settings = settings.updated(key, value)
    else
      throw new NoSuchElementException(s"Config $key not found in this config.")
  }

  override def categoryName: String = ""

  override def addTrack(key: ConfigKey[_]): Unit = {
    settings = settings.updated(key, key.defaultValue)
  }

  override def toString: String = s"ConfigImpl{$settings}"

  override def write(file: ConfigFile): Unit = {
    this.settings.keys.foreach {
      case SubCategoryKey(subCategory) => subCategory.write(file)
      case k => file.write(k)
    }
  }

  override def read(file: ConfigFile): Unit = {
    file.readAll(getKeys(this).toSeq)
  }

  private def getKeys(c: ConfigImpl): Iterable[ConfigKey[_]] = {
    c.settings.keys.flatMap {
      case SubCategoryKey(subCategory) => subCategory match {
        case impl: ConfigChildImpl => getKeys(impl)
        case _ => Seq()
      }
      case k => Seq(k)
    }
  }

}
