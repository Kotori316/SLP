package com.kotori316.scala_lib.config

import cats.Invariant

sealed trait ConfigKey[A] {
  protected def parent: ConfigTemplate

  def name: String

  def defaultValue: A

  def get: A = parent.get(this)

  def set(newValue: A): Unit = parent.set(this, newValue)

  def edInstance: ED[A]
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

  def create[A](parent: ConfigTemplate, configName: String, defaultValue: A)(implicit ed: ED[A]): ConfigKey[A] =
    trackKey(parent = parent, a = GenericsKey(parent, configName, defaultValue, ed))
}

final case class BooleanKey private(override val parent: ConfigTemplate, configName: String, override val defaultValue: Boolean) extends ConfigKey[Boolean] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def get: Boolean = super.get // Give concrete(not generic) return type for Java access.

  override def toString: String = s"BooleanKey{name=$name, defaultValue=$defaultValue}"

  override val edInstance: ED[Boolean] = ED[Boolean]
}

final case class IntKey private(override val parent: ConfigTemplate, configName: String, override val defaultValue: Int,
                                rangeMin: Option[Int], rangeMax: Option[Int]) extends ConfigKey[Int] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def get: Int = super.get // Give concrete(not generic) return type for Java access.

  override def toString: String = s"IntKey{name=$name, defaultValue=$defaultValue, rangeMin=$rangeMin, rangeMax=$rangeMax}"

  override val edInstance: ED[Int] = ED[Int]
}

final case class DoubleKey private(override val parent: ConfigTemplate, configName: String, override val defaultValue: Double,
                                   rangeMin: Option[Double], rangeMax: Option[Double]) extends ConfigKey[Double] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def get: Double = super.get // Give concrete(not generic) return type for Java access.

  override def toString: String = s"DoubleKey{name=$name, defaultValue=$defaultValue, rangeMin=$rangeMin, rangeMax=$rangeMax}"

  override val edInstance: ED[Double] = ED[Double]
}

final case class GenericsKey[A] private(override val parent: ConfigTemplate, configName: String, override val defaultValue: A,
                                        override val edInstance: ED[A])
  extends ConfigKey[A] {
  override val name: String = ConfigKey.getName(parent.categoryName, configName)

  override def toString: String = s"GenericsKey{name=$name, defaultValue=$defaultValue}"
}

final case class SubCategoryKey private(subCategory: ConfigTemplate.ChildTemplate) extends ConfigKey[ConfigTemplate.ChildTemplate] {
  override def name: String = subCategory.categoryName

  override def defaultValue: ConfigTemplate.ChildTemplate = subCategory

  override def parent: ConfigTemplate = subCategory.parent

  override def toString: String = s"SubCategoryKey{$name}"

  // Actually, unused.
  override def edInstance: ED[ConfigTemplate.ChildTemplate] = Invariant[ED].imap(ED[String])(_ => subCategory)(_.toString)
}
