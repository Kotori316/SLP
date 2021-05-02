package com.kotori316.scala_lib.config

import java.util.NoSuchElementException

class ConfigImpl extends ConfigTemplate {
  private var settings: Map[ConfigKey[_], Any] = Map.empty

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

  def write(file: ConfigFile): Unit = {
    this.settings.keys.filterNot(_.isInstanceOf[SubCategoryKey]).foreach(k => file.write(k))
  }
}
