package com.kotori316.scala_lib.config

trait ConfigTemplate {
  def get[A](key: ConfigKey[A]): A

  def set[A](key: ConfigKey[A], newValue: A): Unit

  def getBoolean(key: BooleanKey): Boolean = get(key)

  def getInt(key: IntKey): Int = get(key)

  def getDouble(key: DoubleKey): Double = get(key)

  def categoryName: String

  def addTrack(key: ConfigKey[_]): Unit
}

object ConfigTemplate {
  def debugTemplate: ConfigTemplate = DebugTemplate

  object DebugTemplate extends ConfigTemplate {
    override def get[A](key: ConfigKey[A]): A = key.defaultValue

    override def set[A](key: ConfigKey[A], newValue: A): Unit = ()

    override val categoryName: String = ""

    override def addTrack(key: ConfigKey[_]): Unit = ()
  }

  abstract class ChildTemplate(val parent: ConfigTemplate, name: String) extends ConfigTemplate {
    override final val categoryName: String =
      if (parent.categoryName.isEmpty) name else parent.categoryName + "." + name
  }

  class DebugChildTemplate(parent: ConfigTemplate, name: String) extends ChildTemplate(parent, name) {
    override def get[A](key: ConfigKey[A]): A = key.defaultValue

    override def set[A](key: ConfigKey[A], newValue: A): Unit = ()

    override def addTrack(key: ConfigKey[_]): Unit = ()
  }

}
