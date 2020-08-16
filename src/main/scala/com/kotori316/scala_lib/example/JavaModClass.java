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
        // Registering init method.
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }

    /**
     * Mod initialize method. Called via Mod Event bus, which is registered in {@link JavaModClass#JavaModClass()}.
     */
    public void init(FMLCommonSetupEvent event) {
        LOGGER.info("Hello from java init method. " + event);
    }

    /**
     * In Java code, @{@link Mod.EventBusSubscriber} can be annotated to static inner class.
     * This example class is for {@link net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus#FORGE}.
     *
     * @see net.minecraftforge.fml.common.Mod.EventBusSubscriber
     */
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class EventHandlers {
        /**
         * Methods that handles event must be static.
         *
         * @see net.minecraftforge.eventbus.api.IEventBus#register(Object)
         */
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
