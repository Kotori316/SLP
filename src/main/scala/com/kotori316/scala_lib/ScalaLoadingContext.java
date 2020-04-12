package com.kotori316.scala_lib;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed") // In this class only.
public class ScalaLoadingContext extends net.minecraftforge.fml.javafmlmod.SubContextForScala {

    private final IEventBus bus;

    @Deprecated
    public ScalaLoadingContext(ScalaModContainer container) {
        this.bus = container.getEventBus();
    }

    @Deprecated
    public ScalaLoadingContext(IEventBus bus) {
        this.bus = bus;
    }

    /**
     * @return The mod's event bus, to allow subscription to Mod specific events
     */
    @Override
    public IEventBus getModEventBus() {
        return bus;
    }


    /**
     * Helper to get the right instance from the {@link ModLoadingContext} correctly.
     *
     * @return The Scala language specific extension from the ModLoadingContext
     */
    @Deprecated
    public static ScalaLoadingContext get() {
        Object o = ModLoadingContext.get().extension();
        if (o instanceof ScalaLoadingContext) {
            return (ScalaLoadingContext) o;
        } else if (o instanceof FMLJavaModLoadingContext) {
            FMLJavaModLoadingContext context = (FMLJavaModLoadingContext) o;
            return new ScalaLoadingContext(context.getModEventBus());
        }
        return null;
    }
}
