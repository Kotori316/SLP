package com.kotori316.slp.example

import net.minecraft.SharedConstants
import net.minecraft.gametest.framework.{GameTest, GameTestHelper}
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.gametest.GameTestHolder
import org.slf4j.LoggerFactory

import scala.annotation.static

@Mod(ScalaExampleMod.MOD_ID)
class ScalaExampleMod(context: FMLJavaModLoadingContext, container: ModContainer) {
  context.getModEventBus.addListener(this.setUp)

  private def setUp(event: FMLCommonSetupEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Hello from Scala Example Mod(${container.getModId}) on ${event.description()}!")
  }
}

private object ScalaExampleMod {
  @static
  final val MOD_ID = "slp_examples"
  final val LOGGER = LoggerFactory.getLogger(MOD_ID)

  @GameTestHolder(MOD_ID)
  private class ExampleGameTest {
    @GameTest(batch = MOD_ID, template = "minecraft:nether_fossils/fossil_1")
    def gameTest(helper: GameTestHelper): Unit = {
      ScalaExampleMod.LOGGER.info(s"Running game test in ${SharedConstants.getCurrentVersion.getName}")
      helper.succeed()
    }
  }
}
