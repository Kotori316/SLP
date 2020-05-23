package com.kotori316.scala_lib.example

import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

/**
 * Yo can use EventBusSubscriber annotation to register top object.
 * Both FORGE and MOD event bus are available.
 */
@Mod.EventBusSubscriber(modid = ScalaModObject.modId, bus = Bus.MOD)
object EventHandlers {
  @SubscribeEvent
  def init(event: FMLCommonSetupEvent): Unit = {
    ScalaModObject.LOGGER.info("Hello from event handler." + event)
  }
}
