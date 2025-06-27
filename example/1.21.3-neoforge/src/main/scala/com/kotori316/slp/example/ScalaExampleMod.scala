package com.kotori316.slp.example

import net.minecraft.SharedConstants
import net.minecraft.gametest.framework.{GameTest, GameTestHelper}
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.gametest.{GameTestHolder, PrefixGameTestTemplate}
import org.slf4j.LoggerFactory

import scala.annotation.static

@Mod(ScalaExampleMod.MOD_ID)
class ScalaExampleMod(modEventBus: IEventBus, container: ModContainer) {
  modEventBus.addListener(this.setUp)

  private def setUp(event: FMLCommonSetupEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Hello from Scala Example Mod(${container.getModId}) on ${event.description()}!")
  }
}

object ScalaExampleMod {
  @static
  final val MOD_ID = "slp_examples"
  final val LOGGER = LoggerFactory.getLogger(MOD_ID)

  @GameTestHolder(MOD_ID)
  @PrefixGameTestTemplate(false)
  private class ExampleGameTest {
    @GameTest(batch = MOD_ID, templateNamespace = "minecraft", template = "nether_fossils/fossil_1")
    def gameTest(helper: GameTestHelper): Unit = {
      ScalaExampleMod.LOGGER.info(s"Running game test in ${SharedConstants.getCurrentVersion.getName}")
      helper.succeed()
    }
  }
}
