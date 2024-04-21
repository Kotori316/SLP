package com.kotori316.scala_lib;

import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.EventBusErrorMessage;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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
    @SuppressWarnings("removal")
    public ScalaModContainer(IModInfo info, String className, ModFileScanData modFileScanResults, ModuleLayer gameLayer) {
        super(info);
        this.className = className;
        this.gameLayer = gameLayer;
        LOGGER.debug(LOADING, "Creating scala container Class: {}, with classLoader {}", className, getClass().getClassLoader());
        this.scanData = modFileScanResults;
        this.isScalaObject = className.endsWith("$");

        this.eventBus = BusBuilder.builder()
            .setExceptionHandler(this::onEventFailed)
            .allowPerPhasePost()
            .markerType(IModBusEvent.class)
            .build();
        final net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext contextExtension = createContext(getEventBus());
        this.contextExtension = () -> contextExtension;
    }

    /**
     * Create class instance and get new instance or object representing the mod.
     * Also, this method injects Automatic Event Subscribers for the mod.
     *
     * @throws ModLoadingException thrown if any errors({@link ReflectiveOperationException}) happened.
     */
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    protected void constructMod() {
        try {
            // Here to avoid NPE of scala object.
            var layer = gameLayer.findModule(this.modInfo.getOwningFile().moduleName()).orElseThrow();
            modClass = Class.forName(layer, className);
            LOGGER.trace(LOADING, "Scala Class Loaded {} with {}.", modClass, modClass.getClassLoader());
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Failed to load class {}", className, e);
            throw new ModLoadingException(modInfo, "fml.modloading.failedtoloadmodclass", e);
        }
        try {
            if (isScalaObject) {
                LOGGER.trace(LOADING, "Scala Mod instance object for {} is about to get via MODULE$ field. {}", this.modId, modClass.getName());
                modInstance = modClass.getField("MODULE$").get(null);
                LOGGER.trace(LOADING, "Scala Mod instance for {} was got. {}", this.modId, modInstance);
            } else {
                LOGGER.trace(LOADING, "Scala Mod instance for {} is about to create. {}", this.modId, modClass.getName());
                Map.Entry<Constructor<?>, Object[]> constructors = getConstructor(modClass, this.modId, getEventBus(), this, FMLLoader.getDist());
                constructors.getKey().setAccessible(true);
                modInstance = constructors.getKey().newInstance(constructors.getValue());
                LOGGER.trace(LOADING, "Scala Mod instance for {} created. {}", this.modId, modInstance);
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error(LOADING, "Failed to create/get mod instance. ModID: {}, class {}", this.modId, modClass.getName(), e);
            throw new ModLoadingException(modInfo, "fml.modloading.failedtoloadmod", e, modClass);
        }

        try {
            LOGGER.trace(LOADING, "Injecting Automatic event subscribers for {}", this.modId);
            AutomaticEventSubscriber.inject(this, this.scanData, this.modClass.getClassLoader());
            LOGGER.trace(LOADING, "Completed Automatic event subscribers for {}", this.modId);
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Failed to register automatic subscribers. ModID: {}, class {}", this.modId, modClass.getName(), e);
            throw new ModLoadingException(modInfo, "fml.modloading.failedtoloadmod", e, modClass);
        }
    }

    private void onEventFailed(IEventBus bus, Event event, EventListener[] listeners, int i, Throwable throwable) {
        LOGGER.error(new EventBusErrorMessage(event, i, listeners, throwable));
    }

    @Override
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

    @SuppressWarnings("removal")
    private static net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext createContext(IEventBus bus) {
        try {
            net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext instance = UnsafeHacks.newInstance(net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext.class);
            FMLModContainer container = UnsafeHacks.newInstance(FMLModContainer.class);
            UnsafeHacks.setField(FMLModContainer.class.getDeclaredField("eventBus"), container, bus);
            UnsafeHacks.setField(net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext.class.getDeclaredField("container"), instance, container);
            return instance;
        } catch (ReflectiveOperationException e) {
            LOGGER.fatal("Error happened in creating dummy instance.", e);
            return null;
        }
    }

    static Map.Entry<Constructor<?>, Object[]> getConstructor(Class<?> modClass, String modId, IEventBus bus, ModContainer container, Dist dist) {
        var constructors = modClass.getDeclaredConstructors();
        LOGGER.trace(LOADING, "Found {} constructors for {}", constructors.length, modId);
        var args = Map.of(
            IEventBus.class, bus,
            ModContainer.class, container,
            Dist.class, dist
        );
        var constructor = Stream.of(constructors)
            .filter(c -> Stream.of(c.getParameterTypes()).map(args::get).allMatch(Objects::nonNull))
            .max(Comparator.comparingInt(Constructor::getParameterCount))
            .orElseThrow(() -> new RuntimeException("No mod constructor with allowed arg types were found for " + modId));
        var constructorArgs = Stream.of(constructor.getParameterTypes()).map(args::get).toArray();
        return Map.entry(constructor, constructorArgs);
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
