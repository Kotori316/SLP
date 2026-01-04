package com.kotori316.slp.example

import net.minecraft.SharedConstants
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.slf4j.LoggerFactory

import scala.annotation.static

@Mod(ScalaExampleMod.MOD_ID)
class ScalaExampleMod(context: FMLJavaModLoadingContext, container: ModContainer) {
  context.getModEventBus.addListener(this.setUp)
  context.getModEventBus.addListener(this.registerDataGen)

  private def setUp(event: FMLCommonSetupEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Hello from Scala Example Mod(${container.getModId}) on ${event.description()}!")
  }

  private def registerDataGen(event: GatherDataEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Starting data generation in ${SharedConstants.getCurrentVersion.getName} Forge")
    ScalaExampleMod.LOGGER.info("Start data generation for Scala Example Mod. Input: {}", event.getInputs)
  }
}

private object ScalaExampleMod {
  @static
  final val MOD_ID = "slp_examples"
  final val LOGGER = LoggerFactory.getLogger(MOD_ID)
}
