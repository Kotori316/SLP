package com.kotori316.scala_lib.config

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ConfigImplTest {
  @Test
  def defaultValues(): Unit = {
    val config = new ConfigImpl
    val boolKey = ConfigKey.createBoolean(config, "key1", defaultValue = false)
    val intKey = ConfigKey.createInt(config, "key2", defaultValue = 5)

    assertFalse(boolKey.get)
    config.set(boolKey, true)
    assertTrue(boolKey.get)
    assertEquals(5, intKey.get)
    config.set(intKey, 355896)
    assertEquals(355896, config.get(intKey))
    assertEquals(355896, config.getInt(intKey))
  }

  @Test
  def testString(): Unit = {
    val template = ConfigTemplate.debugTemplate
    val stringKey = ConfigKey.create(template, "stringKey", "default")
    assertEquals("default", stringKey.get)
    assertEquals("default", stringKey.defaultValue)
    assertEquals("default", template.get(stringKey))
  }

  @Test
  def testInt2(): Unit = {
    val template = new ConfigImpl
    val key = ConfigKey.createInt(template, "key1", 5, 0, 100)
    assertEquals(5, template.getInt(key))
    key.set(0)
    assertEquals(0, key.get)
    key.set(100)
    assertEquals(100, key.get)
    key.set(-1)
    assertEquals(100, key.get)
    key.set(101)
    assertEquals(100, key.get)
  }

  @Test
  def setTest1(): Unit = {
    val config = new ConfigImpl
    val boolKey = ConfigKey.createBoolean(config, "boolKey", defaultValue = false)
    val intKey = ConfigKey.createInt(config, "intKey", defaultValue = 5)

    assertFalse(boolKey.get)
    assertEquals(5, intKey.get)

    boolKey.set(true)
    intKey.set(100)

    assertTrue(boolKey.get)
    assertEquals(100, intKey.get)
  }

  @Test
  def noSuchElement1(): Unit = {
    val config = new ConfigImpl
    val boolKey = ConfigKey.createBoolean(ConfigTemplate.DebugTemplate, "key1", defaultValue = false)

    assertAll(
      () => assertThrows(classOf[NoSuchElementException], () => config.get(boolKey)),
      () => assertThrows(classOf[NoSuchElementException], () => config.set(boolKey, true)),
    )
  }

  @Test
  def noSuchElement2(): Unit = {
    val config = new ConfigImpl
    val intKey = ConfigKey.createInt(ConfigTemplate.DebugTemplate, "int", -4)

    assertAll(
      () => assertThrows(classOf[NoSuchElementException], () => config.get(intKey)),
      () => assertThrows(classOf[NoSuchElementException], () => config.set(intKey, 4)),
    )
  }

  @Test
  def subCategory1(): Unit = {
    val config = new ConfigImpl
    val boolKey = ConfigKey.createBoolean(config, "key1", defaultValue = false)
    val intKey = ConfigKey.createInt(config, "key2", defaultValue = 5)
    val subCategory = new ConfigChildImpl(config, "sub1")
    val subCategoryKey = ConfigKey.createSubCategory(subCategory)
    val subIntKey = ConfigKey.createInt(subCategory, "key3", 0)

    assertAll(
      () => assertFalse(config.get(boolKey)),
      () => {
        config.set(intKey, -4)
        assertEquals(-4, intKey.get)
      },
      () => assertEquals(subCategory, config.get(subCategoryKey)),
      () => assertEquals(subCategory, subCategoryKey.get),
      () => assertThrows(classOf[NoSuchElementException], () => config.get(subIntKey)),
      () => assertEquals(0, subCategory.get(subIntKey)),
      () => assertEquals(0, subIntKey.get),
    )
  }
}
