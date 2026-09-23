package com.kotori316.slp.example;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(JavaExampleMod.MOD_ID)
public final class JavaExampleMod {
    public static final String MOD_ID = "slp_examples";
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaExampleMod.class);
    private final ModContainer modContainer;

    public JavaExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        this.modContainer = modContainer;
        modEventBus.addListener(this::setUp);
        modEventBus.addListener(this::registerGameTest);
    }

    private void setUp(FMLCommonSetupEvent event) {
        LOGGER.info("Hello from Scala Example Mod({}) on {} from Java side", modContainer.getModId(), event.toString());
    }

    private void registerGameTest(RegisterEvent event) {
        event.register(
            Registries.TEST_FUNCTION,
            Identifier.fromNamespaceAndPath(MOD_ID, "test_java"),
            () -> this::gameTest
        );
    }

    private void gameTest(GameTestHelper helper) {
        LOGGER.info("Running game test in Java side");
        helper.succeed();
    }
}
