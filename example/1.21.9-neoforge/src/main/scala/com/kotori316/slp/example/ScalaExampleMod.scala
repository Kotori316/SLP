package com.kotori316.slp.example

import net.minecraft.SharedConstants
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForgeVersion
import net.neoforged.neoforge.registries.RegisterEvent
import org.slf4j.LoggerFactory

import scala.annotation.static

@Mod(ScalaExampleMod.MOD_ID)
class ScalaExampleMod(modEventBus: IEventBus, container: ModContainer) {
  modEventBus.addListener(this.setUp)
  modEventBus.addListener(this.register)

  private def setUp(event: FMLCommonSetupEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Hello from Scala Example Mod(${container.getModId}) on ${event.toString}!")
  }

  private def register(event: RegisterEvent): Unit = {
    event.register(
      Registries.TEST_FUNCTION,
      ResourceLocation.fromNamespaceAndPath(ScalaExampleMod.MOD_ID, "test"),
      () => ScalaExampleMod.gameTest,
    )
  }
}

object ScalaExampleMod {
  @static
  final val MOD_ID = "slp_examples"
  final val LOGGER = LoggerFactory.getLogger(MOD_ID)

  //noinspection UnstableApiUsage
  private def gameTest(helper: GameTestHelper): Unit = {
    ScalaExampleMod.LOGGER.info(s"Running game test in ${SharedConstants.getCurrentVersion.name} NeoForge ${NeoForgeVersion.getVersion}")
    helper.succeed()
  }
}
