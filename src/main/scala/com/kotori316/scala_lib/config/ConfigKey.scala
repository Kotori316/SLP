package com.kotori316.scala_lib.config

sealed trait ConfigKey[A] {
  def parent: ConfigTemplate

  def name: String

  def defaultValue: A

  def get: A = parent.get(this)
}

object ConfigKey {
  private def trackKey[A <: ConfigKey[_]](parent: ConfigTemplate, a: A): A = {
    parent.addTrack(a)
    a
  }

  def getName(parentName: String, configName: String): String = {
    if (parentName.isEmpty) {
      configName
    } else {
      parentName + "." + configName
    }
  }

  def createBoolean(parent: ConfigTemplate, configName: String, defaultValue: Boolean): BooleanKey = {
    trackKey(parent, BooleanKey(parent, configName, defaultValue))
  }

  def createInt(parent: ConfigTemplate, configName: String, defaultValue: Int, rangeMin: Int, rangeMax: Int): IntKey = {
    trackKey(parent, IntKey(parent, configName, defaultValue, Option(rangeMin), Option(rangeMax)))
  }

  def createInt(parent: ConfigTemplate, configName: String, defaultValue: Int): IntKey = {
    trackKey(parent, IntKey(parent, configName, defaultValue, rangeMin = None, rangeMax = None))
  }

  def createDouble(parent: ConfigTemplate, configName: String, defaultValue: Double, rangeMin: Double, rangeMax: Double): DoubleKey = {
    trackKey(parent, DoubleKey(parent, configName, defaultValue, Option(rangeMin), Option(rangeMax)))
  }

  def createDouble(parent: ConfigTemplate, configName: String, defaultValue: Double): DoubleKey = {
    trackKey(parent, DoubleKey(parent, configName, defaultValue, rangeMin = None, rangeMax = None))
  }

  def createSubCategory(subCategory: ConfigTemplate.ChildTemplate): SubCategoryKey =
    trackKey(parent = subCategory.parent, SubCategoryKey(subCategory))
}

final case class BooleanKey private(override val parent: ConfigTemplate, configName: String, override val defaultValue: Boolean) extends ConfigKey[Boolean] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def get: Boolean = super.get // Give concrete(not generic) return type for Java access.
  override def toString: String = s"BooleanKey{name=$name, defaultValue=$defaultValue}"
}

final case class IntKey private(override val parent: ConfigTemplate, configName: String, override val defaultValue: Int,
                                rangeMin: Option[Int], rangeMax: Option[Int]) extends ConfigKey[Int] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def get: Int = super.get // Give concrete(not generic) return type for Java access.
  override def toString: String = s"IntKey{name=$name, defaultValue=$defaultValue, rangeMin=$rangeMin, rangeMax=$rangeMax}"
}

final case class DoubleKey private(override val parent: ConfigTemplate, configName: String, override val defaultValue: Double,
                                   rangeMin: Option[Double], rangeMax: Option[Double]) extends ConfigKey[Double] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def get: Double = super.get // Give concrete(not generic) return type for Java access.
  override def toString: String = s"DoubleKey{name=$name, defaultValue=$defaultValue, rangeMin=$rangeMin, rangeMax=$rangeMax}"
}

final case class SubCategoryKey private(subCategory: ConfigTemplate.ChildTemplate) extends ConfigKey[ConfigTemplate.ChildTemplate] {
  override def name: String = subCategory.categoryName

  override def defaultValue: ConfigTemplate.ChildTemplate = subCategory

  override def parent: ConfigTemplate = subCategory.parent

  override def toString: String = s"SubCategoryKey{$name}"
}
