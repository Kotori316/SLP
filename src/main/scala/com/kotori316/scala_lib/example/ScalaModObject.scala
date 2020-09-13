package com.kotori316.scala_lib.example

import cats._
import cats.implicits.toShow
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.{LogManager, Logger}

/**
 * Mod class representing `scala-library`.
 */
@Mod(ScalaModObject.modId)
object ScalaModObject {
  final val modId = "scala-library-object"
  val LOGGER: Logger = LogManager.getLogger(modId)

  require(Class.forName("scala.Option").getMethod("empty").invoke(null) == None)

  // A way to get mod event bus.
  FMLJavaModLoadingContext.get().getModEventBus.addListener(this.init)

  /**
   * Initialization method of mods.
   */
  def init(event: FMLCommonSetupEvent): Unit = {
    LOGGER.info(s"Mod($modId) is loaded. " + event)
    LOGGER.info(ModID(modId).show)
  }

  case class ModID(id: String)

  // Example of Cats instance.
  implicit val showId: Show[ModID] = (t: ModID) => {
    val name = ModList.get().getModObjectById[AnyRef](t.id).map(o => o.getClass.getName).orElse("None")
    s"ID: ${t.id}, Class: $name"
  }

  /**
   * Automatic subscribing of events in inner object is NOT <strong>supported</strong>.
   * To get the correct way, see [[EventHandlers]].
   * Registering this object via [[net.minecraftforge.eventbus.api.IEventBus]]#`register` will work fine.
   */
  @Mod.EventBusSubscriber(bus = Bus.FORGE, modid = modId)
  object BadEventHandler {
    @SubscribeEvent
    def worldLogin(worldEvent: WorldEvent): Unit = {
      LOGGER.info("NEVER HAPPENED " + worldEvent)
    }
  }

}

/**
 * Dummy class.
 * This class is never loaded by forge unless you call method or try to initialize.
 */
class ScalaModObject {
  throw new java.lang.AssertionError("Mod class (not object) must not be loaded by forge. The companion object is mod instance.")
}
