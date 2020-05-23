package com.kotori316.scala_lib.example;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(JavaModClass.MOD_ID)
public class JavaModClass {
    public static final String MOD_ID = "scala-library-java";
    public static final Logger LOGGER = LogManager.getLogger(JavaModClass.class);

    public JavaModClass() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }

    public void init(FMLCommonSetupEvent event) {
        LOGGER.info("Hello from java init method. " + event);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class EventHandlers {
        @SubscribeEvent
        public static void load(WorldEvent.Load event) {
            LOGGER.info("Caught " + event);
        }

        @SubscribeEvent
        public static void unload(WorldEvent.Unload event) {
            LOGGER.info("Caught " + event);
        }
    }
}
