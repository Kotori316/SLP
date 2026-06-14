package com.kotori316.slp.example

import net.minecraft.SharedConstants
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
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
    ScalaExampleMod.LOGGER.info(s"Cats works on startup: ${ScalaExampleMod.catsDemo}")
  }

  private def register(event: RegisterEvent): Unit = {
    event.register(
      Registries.TEST_FUNCTION,
      Identifier.fromNamespaceAndPath(ScalaExampleMod.MOD_ID, "test"),
      () => ScalaExampleMod.gameTest,
    )
  }
}

object ScalaExampleMod {
  @static
  final val MOD_ID = "slp_examples"
  final val LOGGER = LoggerFactory.getLogger(MOD_ID)

  /**
   * Exercises classes from all three bundled Cats modules to confirm the official
   * Cats binary loads correctly inside the (Neo)Forge module layer at runtime.
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

  //noinspection UnstableApiUsage
  private def gameTest(helper: GameTestHelper): Unit = {
    ScalaExampleMod.LOGGER.info(s"Running game test in ${SharedConstants.getCurrentVersion.name} NeoForge ${NeoForgeVersion.getVersion}")
    ScalaExampleMod.LOGGER.info(s"Cats works in game test: ${ScalaExampleMod.catsDemo}")
    helper.succeed()
  }
}
