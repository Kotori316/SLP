package com.kotori316.scala_lib.config

class ConfigChildImpl(parentConfig: ConfigTemplate, subCategoryName: String)
  extends ConfigImpl with ConfigTemplate.ChildTemplate {
  override val parent: ConfigTemplate = parentConfig

  override final val categoryName: String =
    if (parent.categoryName.isEmpty) subCategoryName
    else parent.categoryName + "." + subCategoryName

  override def toString: String = s"ConfigChildImpl{$categoryName, $settings}"
}
