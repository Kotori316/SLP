package com.kotori316.scala_lib

import java.lang.reflect.InvocationTargetException

import com.kotori316.scala_lib.ScalaLanguageProvider.LOGGER
import net.minecraftforge.fml.Logging.LOADING
import net.minecraftforge.fml.{ModLoadingException, ModLoadingStage}
import net.minecraftforge.forgespi.language.IModLanguageProvider.IModLanguageLoader
import net.minecraftforge.forgespi.language.{IModInfo, ModFileScanData}

case class ScalaLanguageTarget(override val className: String, override val modID: String) extends IModLanguageLoader with ModClassData {

  /**
   * Call [[com.kotori316.scala_lib.ScalaModContainer]]
   */
  @throws(classOf[ModLoadingException])
  override def loadMod[T](info: IModInfo, modFileScanResults: ModFileScanData, moduleLayer: ModuleLayer): T = {
    try {
      val scalaContainer = Class.forName("com.kotori316.scala_lib.ScalaModContainer", true, Thread.currentThread.getContextClassLoader)
      LOGGER.debug(LOADING, "Loading ScalaModContainer from classloader {} - got {}", Thread.currentThread.getContextClassLoader: Any, scalaContainer.getClassLoader: Any)
      val constructor = scalaContainer.getConstructor(classOf[IModInfo], classOf[String], classOf[ModFileScanData], classOf[ModuleLayer])
      constructor.newInstance(info, className, modFileScanResults, moduleLayer).asInstanceOf[T]
    } catch {
      case invocationTargetException: InvocationTargetException =>
        LOGGER.fatal(LOADING, "Failed to build mod", invocationTargetException)
        throw invocationTargetException.getTargetException match {
          case modLoadingException: ModLoadingException => modLoadingException
          case _ => createMLE(info, invocationTargetException)
        }
      case reflectiveOperationException: ReflectiveOperationException =>
        LOGGER.fatal(LOADING, "Unable to load ScalaModContainer", reflectiveOperationException)
        throw createMLE(info, reflectiveOperationException)
    }
  }

  private def createMLE(info: IModInfo, e: ReflectiveOperationException): ModLoadingException = {
    new ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.ModLoading.FailedToLoadModClass".toLowerCase, e)
  }

}
