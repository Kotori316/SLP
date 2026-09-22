package com.kotori316.slp.example

import net.minecraft.SharedConstants
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.versions.forge.ForgeVersion
import org.slf4j.LoggerFactory

import scala.annotation.static

@Mod(ScalaExampleMod.MOD_ID)
class ScalaExampleMod(context: FMLJavaModLoadingContext, container: ModContainer) {
  FMLCommonSetupEvent.getBus(context.getModBusGroup).addListener(this.setUp)
  GatherDataEvent.getBus(context.getModBusGroup).addListener(this.registerDataGen)

  private def setUp(event: FMLCommonSetupEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Hello from Scala Example Mod(${container.getModId}) on ${event.getClass.getName}!")
    ScalaExampleMod.LOGGER.info(s"Cats works on startup: ${ScalaExampleMod.catsDemo}")
  }

  private def registerDataGen(event: GatherDataEvent): Unit = {
    ScalaExampleMod.LOGGER.info(s"Starting data generation in ${SharedConstants.getCurrentVersion.name} Forge ${ForgeVersion.getVersion}")
    ScalaExampleMod.LOGGER.info("Start data generation for Scala Example Mod. Input: {}", event.getInputs)
    ScalaExampleMod.LOGGER.info(s"Cats works in data generation: ${ScalaExampleMod.catsDemo}")
  }
}

private object ScalaExampleMod {
  @static
  final val MOD_ID = "slp_examples"
  final val LOGGER = LoggerFactory.getLogger(MOD_ID)

  /**
   * Exercises classes from all three bundled Cats modules to confirm the official
   * Cats binary loads correctly inside the Forge module layer at runtime.
   *   - cats-core:   [[cats.data.NonEmptyList]]
   *   - cats-kernel: [[cats.kernel.Monoid]] (via the `combineAll` syntax)
   *   - cats-free:   [[cats.free.Free]]
   */
  private def catsDemo: String = {
    import cats.Id
    import cats.arrow.FunctionK
    import cats.data.NonEmptyList
    import cats.free.Free
    import cats.implicits.*

    val nel = NonEmptyList.of(1, 2, 3)
    val sum = nel.toList.combineAll
    val program = Free.pure[Id, Int](sum).map(_ + nel.head)
    val freeResult = program.foldMap(FunctionK.id[Id])
    s"NonEmptyList=${nel.toList.mkString(",")}, Monoid sum=$sum, Free result=$freeResult"
  }
}
