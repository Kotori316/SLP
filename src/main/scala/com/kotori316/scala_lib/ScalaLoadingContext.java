package com.kotori316.scala_lib;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

public class ScalaLoadingContext extends net.minecraftforge.fml.javafmlmod.SubContextForScala {

    private final ScalaModContainer container;

    public ScalaLoadingContext(ScalaModContainer container) {
        this.container = container;
    }

    /**
     * @return The mod's event bus, to allow subscription to Mod specific events
     */
    @Override
    public IEventBus getModEventBus() {
        return container.getEventBus();
    }


    /**
     * Helper to get the right instance from the {@link ModLoadingContext} correctly.
     *
     * @return The Scala language specific extension from the ModLoadingContext
     */
    public static ScalaLoadingContext get() {
        return ModLoadingContext.get().extension();
    }
}
