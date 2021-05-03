package com.kotori316.scala_lib.config

import java.util.NoSuchElementException

import scala.collection.immutable.SeqMap

class ConfigChildImpl(parentConfig: ConfigTemplate, subCategoryName: String)
  extends ConfigTemplate.ChildTemplate(parentConfig, subCategoryName) {
  private var settings: Map[ConfigKey[_], Any] = SeqMap.empty

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

  override def addTrack(key: ConfigKey[_]): Unit = {
    settings = settings.updated(key, key.defaultValue)
  }

  override def toString: String = s"ConfigChildImpl{$categoryName, $settings}"
}
