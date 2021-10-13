package com.kotori316.scala_lib.example

import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

/**
 * You can use [[Mod.EventBusSubscriber]] annotation to register top object.
 * Both [[Bus.FORGE]] and [[Bus.MOD]] event bus are available.
 * Fill `modid` with your modID to tell forge which mod this object belongs to.
 */
@Mod.EventBusSubscriber(modid = ScalaModObject.modId, bus = Bus.MOD)
object EventHandlers {
  @SubscribeEvent
  def init(event: FMLCommonSetupEvent): Unit = {
    ScalaModObject.LOGGER.info("Hello from event handler." + event)
  }
}
