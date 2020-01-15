package com.kotori316.scala_lib

import com.kotori316.scala_lib.ScalaLanguageProvider.LOGGER
import net.minecraftforge.fml.Logging.LOADING
import net.minecraftforge.forgespi.language.IModLanguageProvider.IModLanguageLoader
import net.minecraftforge.forgespi.language.{IModInfo, ModFileScanData}

case class ScalaLanguageTarget(override val className: String, override val modID: String) extends IModLanguageLoader with ModClassData {

  override def loadMod[T](info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T = {
    try {
      val fmlContainer = Class.forName("com.kotori316.scala_lib.ScalaModContainer", true, Thread.currentThread.getContextClassLoader)
      LOGGER.debug(LOADING, "Loading ScalaModContainer from classloader {} - got {}", Thread.currentThread.getContextClassLoader: Any, fmlContainer.getClassLoader: Any)
      val constructor = fmlContainer.getConstructor(classOf[IModInfo], classOf[String], classOf[ClassLoader], classOf[ModFileScanData])
      constructor.newInstance(info, className, modClassLoader, modFileScanResults).asInstanceOf[T]
    } catch {
      case e: ReflectiveOperationException =>
        LOGGER.fatal(LOADING, "Unable to load ScalaModContainer", e)
        throw new RuntimeException(e)
    }
  }
}
