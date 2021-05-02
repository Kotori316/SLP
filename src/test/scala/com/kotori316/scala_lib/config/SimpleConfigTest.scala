package com.kotori316.scala_lib.config

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import scala.jdk.javaapi.CollectionConverters

object SimpleConfigTest {
  val pre =
    """
      |# Comment
      |a=true
      |b=false
      |c=45
      |""".stripMargin.linesIterator.toSeq

  class Update {
    @Test
    def testUpdate1(): Unit = {
      val template = new ConfigImpl
      val key1 = ConfigKey.createBoolean(template, "a", defaultValue = false)
      key1.set(true)
      val update1 = ConfigFile.SimpleTextConfig.updateValue(key1, pre)
      assertIterableEquals(CollectionConverters.asJava(pre), CollectionConverters.asJava(update1))

    }

    @Test
    def testUpdate2(): Unit = {
      val template = new ConfigImpl
      val key2 = ConfigKey.createBoolean(template, "b", defaultValue = false)
      key2.set(true)

      val update2 = ConfigFile.SimpleTextConfig.updateValue(key2, pre)
      assertIterableEquals(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=true
          |c=45
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update2))
    }

    @Test
    def testUpdate3(): Unit = {
      val template = new ConfigImpl
      val key3 = ConfigKey.createInt(template, "c", 15)

      val update3 = ConfigFile.SimpleTextConfig.updateValue(key3, pre)
      assertIterableEquals(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=false
          |c=15
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update3))
    }

    @Test
    def testUpdate4(): Unit = {
      val template = new ConfigImpl
      val key4 = ConfigKey.createInt(template, "d", 80)
      val update4 = ConfigFile.SimpleTextConfig.updateValue(key4, pre)

      assertIterableEquals(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=false
          |c=45
          |d=80
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update4))
    }

  }

  class Find {
    @Test
    def find1(): Unit = {
      val template = new ConfigImpl
      val key1 = ConfigKey.createBoolean(template, "a", defaultValue = false)
      val key2 = ConfigKey.createBoolean(template, "b", defaultValue = false)
      val key3 = ConfigKey.createInt(template, "c", 15)
      val key4 = ConfigKey.createInt(template, "d", 80)

      assertAll(
        () => assertEquals(Some(true), ConfigFile.SimpleTextConfig.findValue(key1, pre.iterator)),
        () => assertEquals(Some(false), ConfigFile.SimpleTextConfig.findValue(key2, pre.iterator)),
        () => assertEquals(Some(45), ConfigFile.SimpleTextConfig.findValue(key3, pre.iterator)),
        () => assertEquals(None, ConfigFile.SimpleTextConfig.findValue(key4, pre.iterator)),
      )
    }
  }

}
