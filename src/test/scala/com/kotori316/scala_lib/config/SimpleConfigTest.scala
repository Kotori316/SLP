package com.kotori316.scala_lib.config

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.jdk.javaapi.CollectionConverters

object SimpleConfigTest {
  val pre =
    """
      |# Comment
      |a=true
      |b=false
      |c=45
      |e=disabled # Comment2
      |""".stripMargin.linesIterator.toSeq

  class Update {
    @Test
    def testUpdate1(): Unit = {
      val template = new ConfigImpl
      val key1 = ConfigKey.createBoolean(template, "a", defaultValue = false)
      key1.set(true)
      val update1 = SimpleTextConfigFile.updateValue(key1, pre)
      assertLinesMatch(CollectionConverters.asJava(pre), CollectionConverters.asJava(update1))
    }

    @Test
    def testUpdate2(): Unit = {
      val template = new ConfigImpl
      val key2 = ConfigKey.createBoolean(template, "b", defaultValue = false)
      key2.set(true)

      val update2 = SimpleTextConfigFile.updateValue(key2, pre)
      assertLinesMatch(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=true
          |c=45
          |e=disabled # Comment2
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update2))
    }

    @Test
    def testUpdate3(): Unit = {
      val template = new ConfigImpl
      val key3 = ConfigKey.createInt(template, "c", 15)

      val update3 = SimpleTextConfigFile.updateValue(key3, pre)
      assertLinesMatch(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=false
          |c=15
          |e=disabled # Comment2
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update3))
    }

    @Test
    def testUpdate4(): Unit = {
      val template = new ConfigImpl
      val key4 = ConfigKey.createInt(template, "d", 80)
      val update4 = SimpleTextConfigFile.updateValue(key4, pre)

      assertLinesMatch(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=false
          |c=45
          |e=disabled # Comment2
          |d=80
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update4))
    }

    @Test
    def testUpdate5(): Unit = {
      val template = new ConfigImpl
      val key = ConfigKey.create(template, "e", "enabled")
      key.set("disabled")
      val update = SimpleTextConfigFile.updateValue(key, pre)

      assertLinesMatch(CollectionConverters.asJava(
        """
          |# Comment
          |a=true
          |b=false
          |c=45
          |e=disabled # Comment2
          |""".stripMargin.linesIterator.toSeq), CollectionConverters.asJava(update))
    }

    @Test
    def testUpdateAll1(): Unit = {
      val template = new ConfigImpl
      val keys: Seq[ConfigKey[Any]] = Seq(
        ConfigKey.createBoolean(template, "a", defaultValue = true),
        ConfigKey.createBoolean(template, "b", defaultValue = false),
        ConfigKey.createInt(template, "c", 45),
        ConfigKey.createInt(template, "d", 80),
        ConfigKey.create(template, "e", "enabled"),
      ).map(_.asInstanceOf[ConfigKey[Any]])
      val updated = keys.foldLeft("") { case (str, key) => SimpleTextConfigFile.updateValue(key, str.linesIterator.toSeq)(key.edInstance).mkString(System.lineSeparator()) }
      assertLinesMatch(
        CollectionConverters.asJava(
          """a=true
            |b=false
            |c=45
            |d=80
            |e=enabled""".stripMargin.linesIterator.toSeq
        ), CollectionConverters.asJava(updated.linesIterator.toSeq))
    }

    @Test
    def testUpdateAll2(): Unit = {
      val template = new ConfigImpl
      val sub1 = new ConfigChildImpl(template, "sub1")
      val keys: Seq[ConfigKey[Any]] = Seq(
        ConfigKey.create(template, "a", defaultValue = true),
        ConfigKey.create(template, "b", defaultValue = false),
        ConfigKey.create(template, "c", defaultValue = 20),
        ConfigKey.create(template, "d", defaultValue = 80),
        ConfigKey.create(template, "e", defaultValue = "enabled"),
        ConfigKey.create(sub1, "a", defaultValue = "sub1")
      ).map(_.asInstanceOf[ConfigKey[Any]])
      val updated = keys.foldLeft(Seq.empty[String]) { case (strSeq, key) => SimpleTextConfigFile.updateValue(key, strSeq)(key.edInstance).toSeq }
      assertLinesMatch(
        CollectionConverters.asJava(
          """a=true
            |b=false
            |c=20
            |d=80
            |e=enabled
            |sub1.a=sub1""".stripMargin.linesIterator.toSeq
        ), CollectionConverters.asJava(updated))
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
      val key5 = ConfigKey.create(template, "e", "enabled")

      assertAll(
        () => assertEquals(Some(true), SimpleTextConfigFile.findValue(key1, pre.iterator)),
        () => assertEquals(Some(false), SimpleTextConfigFile.findValue(key2, pre.iterator)),
        () => assertEquals(Some(45), SimpleTextConfigFile.findValue(key3, pre.iterator)),
        () => assertEquals(None, SimpleTextConfigFile.findValue(key4, pre.iterator)),
        () => assertEquals(Some("disabled"), SimpleTextConfigFile.findValue(key5, pre.iterator)),
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.scala_lib.config.SimpleConfigTest#find2Arguments"))
    def find2(key: ConfigKey[_], expect: Option[_]): Unit = {
      val configText =
        """
          |; Comment
          |;a=true
          |   #b=false
          |c=45    # What?
          |#e=disabled # Comment2
          |""".stripMargin.linesIterator.toSeq
      val typedKey: ConfigKey[Any] = key.asInstanceOf[ConfigKey[Any]]

      assertEquals(expect, SimpleTextConfigFile.findValue(typedKey, configText.iterator)(typedKey.edInstance))
    }

    //noinspection ScalaUnusedSymbol
    @ParameterizedTest
    @MethodSource(Array("com.kotori316.scala_lib.config.SimpleConfigTest#find2Arguments"))
    def find3(key: ConfigKey[_], unused: Option[_]): Unit = {
      val configText = ""
      val typedKey: ConfigKey[Any] = key.asInstanceOf[ConfigKey[Any]]
      assertEquals(None, SimpleTextConfigFile.findValue(typedKey, Iterator(configText))(typedKey.edInstance))
      assertEquals(None, SimpleTextConfigFile.findValue(typedKey, Iterator.empty)(typedKey.edInstance))
    }
  }

  def find2Arguments(): java.util.List[Array[_]] = {
    val template = ConfigTemplate.debugTemplate
    CollectionConverters.asJava(
      List(
        ConfigKey.createBoolean(template, "a", defaultValue = false),
        ConfigKey.createBoolean(template, "b", defaultValue = false),
        ConfigKey.createInt(template, "d", 80),
        ConfigKey.create(template, "e", "enabled"),
        ConfigKey.createBoolean(template, ";a", defaultValue = false),
        ConfigKey.createBoolean(template, "#b", defaultValue = false),
        ConfigKey.create(template, "#e", "enabled"),
      ).map(k => Array(k, None)) ++
        Seq(Array(ConfigKey.createInt(template, "c", 45), Some(45)))
    )
  }
}
