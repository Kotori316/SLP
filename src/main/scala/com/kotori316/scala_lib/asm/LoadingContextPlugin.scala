package com.kotori316.scala_lib.asm

import java.util

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService
import org.apache.logging.log4j.{LogManager, Logger}
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{Opcodes, Type}

import scala.jdk.javaapi.CollectionConverters

/**
 * Can't be used because [[ILaunchPluginService]] is not loaded jars whose MANIFEST.MF has "FMLModType: LANGPROVIDER".
 *
 * <strong>NOT USED</strong>
 */
class LoadingContextPlugin extends ILaunchPluginService {
  LoadingContextPlugin.LOGGER.debug(s"${getClass.getName} instance created.")

  override def name(): String = getClass.getName

  override def handlesClass(classType: Type, isEmpty: Boolean): util.EnumSet[ILaunchPluginService.Phase] =
    if (isEmpty) LoadingContextPlugin.NAY else LoadingContextPlugin.YAY

  override def processClass(phase: ILaunchPluginService.Phase, classNode: ClassNode, classType: Type): Boolean = {
    if (classType.toString.contains("FMLJavaModLoadingContext")) {
      CollectionConverters.asScala(classNode.methods)
        .find(m => "<init>".equals(m.name))
        .foreach(m => m.access = ~(~m.access | Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC)
      true
    } else
      false
  }
}

object LoadingContextPlugin {
  val LOGGER: Logger = LogManager.getLogger(classOf[LoadingContextPlugin])
  private val YAY = util.EnumSet.of(ILaunchPluginService.Phase.AFTER)
  private val NAY = util.EnumSet.noneOf(classOf[ILaunchPluginService.Phase])
}
