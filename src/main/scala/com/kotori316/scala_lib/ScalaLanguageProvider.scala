package com.kotori316.scala_lib

import java.util.function.{Consumer, Supplier}

import cats.data.Validated
import net.minecraftforge.fml.Logging.SCAN
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.{ILifecycleEvent, IModLanguageProvider, ModFileScanData}
import org.apache.logging.log4j.{LogManager, Logger}
import org.objectweb.asm.Type

class ScalaLanguageProvider extends IModLanguageProvider {

  import ScalaLanguageProvider._

  override def name() = "kotori_scala"

  override def getFileVisitor: Consumer[ModFileScanData] = scanData => {
    import scala.jdk.CollectionConverters._
    val targets = scanData.getAnnotations.asScala
      .filter(_.annotationType() == MOD_ANNOTATION)
      .map { data =>
        val className = data.clazz().getClassName
        val id = data.annotationData().get("value").asInstanceOf[String]
        ScalaLanguageTarget(className, id)
      }
    ModClassData.findInstance(targets.toList) match {
      case Validated.Valid(a) =>
        val map = a.map { case t@ScalaLanguageTarget(name, id) =>
          LOGGER.debug(SCAN, "Found @Mod class {} with id {}", name: Any, id: Any)
          id -> t
        }.toMap
        scanData.addLanguageLoader(map.asJava)
      case Validated.Invalid(e) =>
        val modList = e.mkString(", ")
        throw new RuntimeException(s"Exception in loading mods. $modList")
    }
  }

  override def consumeLifecycleEvent[R <: ILifecycleEvent[R]](consumeEvent: Supplier[R]): Unit = ()
}

object ScalaLanguageProvider {
  val MOD_ANNOTATION: Type = FMLJavaModLanguageProvider.MODANNOTATION
  val LOGGER: Logger = LogManager.getLogger(getClass)
}
