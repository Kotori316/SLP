package com.kotori316.scala_lib

import java.util.function.{Consumer, Supplier}

import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.fml.Logging.SCAN
import net.minecraftforge.forgespi.language.{ILifecycleEvent, IModLanguageProvider, ModFileScanData}
import org.apache.logging.log4j.LogManager

class ScalaLanguageProvider extends IModLanguageProvider {

  import ScalaLanguageProvider._

  override def name() = "kotori_scala"

  override def getFileVisitor: Consumer[ModFileScanData] = scanData => {
    import scala.collection.JavaConverters._
    val targets = scanData.getAnnotations.asScala
      .filter(_.getAnnotationType == MOD_ANNOTATION)
      .map { data =>
        val className = data.getClassType.getClassName
        val id = data.getAnnotationData.get("value").asInstanceOf[String]
        ScalaLanguageTarget(className, id)
      }
    val map = targets.collect { case ScalaLanguageTarget(_, id) => id }
      .map { modId =>
        targets.filter(_.modID == modId).reduce[ScalaLanguageTarget] {
          case (a, b) =>
            if (a.isScalaObj ^ b.isScalaObj) {
              Option(a).filter(_.isScalaObj).getOrElse(b)
            } else {
              throw new RuntimeException(s"Duplicate mod id. for classes ${a.className} and ${b.className}.")
            }
        }
      }.map { case t@ScalaLanguageTarget(name, id) =>
      LOGGER.debug(SCAN, "Found @Mod class {} with id {}", name: Any, id: Any)
      id -> t
    }
      .toMap
    scanData.addLanguageLoader(map.asJava)
  }

  override def consumeLifecycleEvent[R <: ILifecycleEvent[R]](consumeEvent: Supplier[R]): Unit = ()
}

object ScalaLanguageProvider {
  val MOD_ANNOTATION = FMLJavaModLanguageProvider.MODANNOTATION
  val LOGGER = LogManager.getLogger(getClass)
}
