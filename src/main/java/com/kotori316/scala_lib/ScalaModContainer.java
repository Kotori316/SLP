package com.kotori316.scala_lib;

import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.neoforged.bus.EventBusErrorMessage;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingStage;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static net.neoforged.fml.Logging.LOADING;

public class ScalaModContainer extends ModContainer {
    private static final Logger LOGGER = LogManager.getLogger(ScalaModContainer.class);

    private final String className;
    private final ModuleLayer gameLayer;
    private final ModFileScanData scanData;

    private final boolean isScalaObject;

    private final IEventBus eventBus;
    private Class<?> modClass;
    private Object modInstance;

    /**
     * Instance created in {@link ScalaLanguageTarget#loadMod(IModInfo, ModFileScanData, ModuleLayer)}
     */
    public ScalaModContainer(IModInfo info, String className, ModFileScanData modFileScanResults, ModuleLayer gameLayer) {
        super(info);
        this.className = className;
        this.gameLayer = gameLayer;
        LOGGER.debug(LOADING, "Creating scala container Class: {}, with classLoader {}", className, getClass().getClassLoader());
        this.scanData = modFileScanResults;
        this.isScalaObject = className.endsWith("$");

        this.activityMap.put(ModLoadingStage.CONSTRUCT, this::constructMod);

        this.eventBus = BusBuilder.builder()
                .setExceptionHandler(this::onEventFailed)
                .allowPerPhasePost()
                .markerType(IModBusEvent.class)
                .build();
        this.configHandler = Optional.of(ce -> this.eventBus.post(ce.self()));
        final FMLJavaModLoadingContext contextExtension = createContext(getEventBus());
        this.contextExtension = () -> contextExtension;
    }

    /**
     * Create class instance and get new instance or object representing the mod.
     * Also, this method injects Automatic Event Subscribers for the mod.
     *
     * @throws ModLoadingException - thrown if any errors({@link ReflectiveOperationException}) happened.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void constructMod() {
        try {
            // Here to avoid NPE of scala object.
            var layer = gameLayer.findModule(this.modInfo.getOwningFile().moduleName()).orElseThrow();
            modClass = Class.forName(layer, className);
            LOGGER.trace(LOADING, "Scala Class Loaded {} with {}.", modClass, modClass.getClassLoader());
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Failed to load class {}", className, e);
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e);
        }
        try {
            if (isScalaObject) {
                LOGGER.trace(LOADING, "Scala Mod instance object for {} is about to get via MODULE$ field. {}", this.modId, modClass.getName());
                modInstance = modClass.getField("MODULE$").get(null);
                LOGGER.trace(LOADING, "Scala Mod instance for {} was got. {}", this.modId, modInstance);
            } else {
                LOGGER.trace(LOADING, "Scala Mod instance for {} is about to create. {}", this.modId, modClass.getName());
                Constructor<?> constructor = modClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                modInstance = constructor.newInstance();
                LOGGER.trace(LOADING, "Scala Mod instance for {} created. {}", this.modId, modInstance);
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error(LOADING, "Failed to create/get mod instance. ModID: {}, class {}", this.modId, modClass.getName(), e);
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e, modClass);
        }

        try {
            LOGGER.trace(LOADING, "Injecting Automatic event subscribers for {}", this.modId);
            AutomaticEventSubscriber.inject(this, this.scanData, this.modClass.getClassLoader());
            LOGGER.trace(LOADING, "Completed Automatic event subscribers for {}", this.modId);
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Failed to register automatic subscribers. ModID: {}, class {}", this.modId, modClass.getName(), e);
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e, modClass);
        }
    }

    private void onEventFailed(IEventBus bus, Event event, EventListener[] listeners, int i, Throwable throwable) {
        LOGGER.error(new EventBusErrorMessage(event, i, listeners, throwable));
    }

    public IEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public boolean matches(Object mod) {
        return mod == modInstance;
    }

    @Override
    public Object getMod() {
        return modInstance;
    }

    private static FMLJavaModLoadingContext createContext(IEventBus bus) {
        try {
            FMLJavaModLoadingContext instance = UnsafeHacks.newInstance(FMLJavaModLoadingContext.class);
            FMLModContainer container = UnsafeHacks.newInstance(FMLModContainer.class);
            UnsafeHacks.setField(FMLModContainer.class.getDeclaredField("eventBus"), container, bus);
            UnsafeHacks.setField(FMLJavaModLoadingContext.class.getDeclaredField("container"), instance, container);
            return instance;
        } catch (ReflectiveOperationException e) {
            LOGGER.fatal("Error happened in creating dummy instance.", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "ScalaModContainer{" +
            "modId='" + modId + '\'' +
            ", className='" + className + '\'' +
            ", modClass=" + (modClass == null ? "<not initialized>" : modClass) +
            '}';
    }
}
