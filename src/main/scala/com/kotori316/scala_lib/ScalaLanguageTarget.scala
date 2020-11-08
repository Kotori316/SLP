package com.kotori316.scala_lib

import java.lang.reflect.InvocationTargetException

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
      case invocationTargetException: InvocationTargetException =>
        LOGGER.fatal(LOADING, "Failed to build mod", invocationTargetException)
        val mleClass = getMLE
        if (mleClass.isInstance(invocationTargetException.getTargetException)) {
          throw invocationTargetException.getTargetException
        } else {
          throw createMLE(info, invocationTargetException)
        }
      case reflectiveOperationException: ReflectiveOperationException =>
        LOGGER.fatal(LOADING, "Unable to load ScalaModContainer", reflectiveOperationException)
        throw createMLE(info, reflectiveOperationException)
    }
  }

  private def createMLE[T](info: IModInfo, e: ReflectiveOperationException): Throwable = {
    val mlsClass: Class[_] = getMLS
    getMLE.getConstructor(classOf[IModInfo], mlsClass, classOf[String], classOf[Throwable])
      .newInstance(info, Enum.valueOf(mlsClass, "CONSTRUCT"), "fml.ModLoading.FailedToLoadModClass".toLowerCase, e).asInstanceOf[Throwable]
  }

  /**
   * @return Class Instance of [[net.minecraftforge.fml.ModLoadingException]]
   */
  private def getMLE: Class[_] = getClassWithCurrentLoader("net.minecraftforge.fml.ModLoadingException")

  /**
   * @return Class Instance of [[net.minecraftforge.fml.ModLoadingStage]]
   */
  private def getMLS: Class[_] = getClassWithCurrentLoader("net.minecraftforge.fml.ModLoadingStage")

  private def getClassWithCurrentLoader(name: String): Class[_] =
    Class.forName(name, true, Thread.currentThread.getContextClassLoader)

}
