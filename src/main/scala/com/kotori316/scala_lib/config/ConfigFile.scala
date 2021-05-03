package com.kotori316.scala_lib.config

trait ConfigFile {
  def write[A](key: ConfigKey[A]): Unit

  def writeAll(keys: Seq[ConfigKey[_]]): Unit = {
    for (key <- keys; typed = key.asInstanceOf[ConfigKey[Any]]) {
      write(typed)
    }
  }

  def read[A](key: ConfigKey[A]): Unit

  def readAll(keys: Seq[ConfigKey[_]]): Unit = {
    for (key <- keys; typed = key.asInstanceOf[ConfigKey[Any]]) {
      read(typed)
    }
  }
}
