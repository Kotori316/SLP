package com.kotori316.scala_lib.config

trait ConfigTemplate {
  def get[A](key: ConfigKey[A]): A

  def set[A](key: ConfigKey[A], newValue: A): Unit

  def getBoolean(key: BooleanKey): Boolean = get(key)

  def getInt(key: IntKey): Int = get(key)

  def getDouble(key: DoubleKey): Double = get(key)

  def categoryName: String

  def addTrack(key: ConfigKey[_]): Unit

  def write(file: ConfigFile): Unit

  def read(file: ConfigFile): Unit
}

object ConfigTemplate {
  def debugTemplate: ConfigTemplate = DebugTemplate

  object DebugTemplate extends ConfigTemplate {
    override def get[A](key: ConfigKey[A]): A = key.defaultValue

    override def set[A](key: ConfigKey[A], newValue: A): Unit = ()

    override val categoryName: String = ""

    override def addTrack(key: ConfigKey[_]): Unit = ()

    override def write(file: ConfigFile): Unit = ()

    override def read(file: ConfigFile): Unit = ()
  }

  trait ChildTemplate extends ConfigTemplate {
    val parent: ConfigTemplate
  }

  class DebugChildTemplate(override val parent: ConfigTemplate, name: String) extends ChildTemplate {
    override final val categoryName: String = if (parent.categoryName.isEmpty) name else parent.categoryName + "." + name

    override def get[A](key: ConfigKey[A]): A = key.defaultValue

    override def set[A](key: ConfigKey[A], newValue: A): Unit = ()

    override def addTrack(key: ConfigKey[_]): Unit = ()

    override def write(file: ConfigFile): Unit = ()

    override def read(file: ConfigFile): Unit = ()
  }

}
