package com.kotori316.scala_lib

import cats._
import cats.implicits._
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager

/**
  * Mod class representing `scala-library`.
  */
@Mod(ScalaMC.modId)
object ScalaMC {
  final val modId = "scala-library"
  val LOGGER = LogManager.getLogger(modId)

  require(Class.forName("scala.Option").getMethod("empty").invoke(null) == None)

  ScalaLoadingContext.get().getModEventBus.addListener(this.init)

  def init(event: FMLCommonSetupEvent): Unit = {
    LOGGER.debug(s"Mod($modId) is loaded.")
    LOGGER.debug(ModID(modId).show)
  }

  case class ModID(id: String)

  implicit val showId: Show[ModID] = (t: ModID) => {
    val name = ModList.get().getModObjectById[AnyRef](t.id).map(o => o.getClass.getName).orElse("None")
    s"ID: ${t.id}, Class: $name"
  }
}

/**
  * Dummy class.
  * This class is never loaded by forge unless you call method or try to initialize.
  */
class ScalaMC {
  throw new java.lang.AssertionError("Mod class (not object) must not be loaded by forge. The companion object is mod instance.")
}
