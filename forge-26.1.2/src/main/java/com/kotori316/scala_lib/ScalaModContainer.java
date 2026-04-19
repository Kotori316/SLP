package com.kotori316.scala_lib;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.AutomaticEventSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.unsafe.UnsafeHacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static net.minecraftforge.fml.Logging.LOADING;

public class ScalaModContainer extends ModContainer {
    private static final Logger LOGGER = LogManager.getLogger(ScalaModContainer.class);

    private final String className;
    private final ModuleLayer gameLayer;
    private final ModFileScanData scanData;

    private final boolean isScalaObject;

    private final BusGroup eventBusGroup;
    private Class<?> modClass;
    private Object modInstance;
    private final FMLJavaModLoadingContext context;

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

        this.eventBusGroup = BusGroup.create("modBusFor" + info.getModId());
        context = createContext(getModBusGroup());
        this.contextExtension = () -> context;
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
            var module = gameLayer.findModule(this.modInfo.getOwningFile().moduleName()).orElseThrow();
            ModuleOpener.openModule(gameLayer, module, this.modInfo.getOwningFile().getFile().getSecureJar());
            modClass = Class.forName(module, className);
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
                Map.Entry<Constructor<?>, Object[]> constructors = getConstructor(modClass, this.modId, getModBusGroup(), this, FMLLoader.getDist(), context);
                constructors.getKey().setAccessible(true);
                modInstance = constructors.getKey().newInstance(constructors.getValue());
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

    public BusGroup getModBusGroup() {
        return this.eventBusGroup;
    }

    @Override
    public boolean matches(Object mod) {
        return mod == modInstance;
    }

    @Override
    public Object getMod() {
        return modInstance;
    }

    @Override
    protected <T extends IModBusEvent> void acceptEvent(final T event) {
        LOGGER.trace(LOADING, "Firing event for modid {} : {}", this.modId, event);
        try {
            @SuppressWarnings("unchecked")
            var eventBus = (EventBus<T>) IModBusEvent.getBus(eventBusGroup, event.getClass());
            eventBus.post(event);
            LOGGER.trace(LOADING, "Fired event for modid {} : {}", this.modId, event);
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Caught exception during event {} dispatch for modid {}", event, this.modId, e);
            throw new ModLoadingException(modInfo, modLoadingStage, "fml.modloading.errorduringevent", e);
        }
    }

    @Override
    public void dispatchConfigEvent(IConfigEvent event) {
        var eventBus = EventBus.create(eventBusGroup, event.self().getClass());
        eventBus.post(event.self());
    }

    private static FMLJavaModLoadingContext createContext(BusGroup busGroup) {
        try {
            FMLJavaModLoadingContext instance = UnsafeHacks.newInstance(FMLJavaModLoadingContext.class);
            FMLModContainer container = UnsafeHacks.newInstance(FMLModContainer.class);
            UnsafeHacks.setField(FMLModContainer.class.getDeclaredField("eventBusGroup"), container, busGroup);
            UnsafeHacks.setField(FMLJavaModLoadingContext.class.getDeclaredField("container"), instance, container);
            return instance;
        } catch (ReflectiveOperationException e) {
            LOGGER.fatal("Error happened in creating dummy instance.", e);
            return null;
        }
    }

    static Map.Entry<Constructor<?>, Object[]> getConstructor(Class<?> modClass, String modId, BusGroup busGroup, ModContainer container, Dist dist, FMLJavaModLoadingContext context) {
        var constructors = modClass.getDeclaredConstructors();
        LOGGER.trace(LOADING, "Found {} constructors for {}", constructors.length, modId);
        var args = Map.of(
            BusGroup.class, busGroup,
            ModContainer.class, container,
            Dist.class, dist,
            FMLJavaModLoadingContext.class, context
        );
        var constructor = Stream.of(constructors)
            .filter(c -> Stream.of(c.getParameterTypes()).allMatch(args::containsKey))
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
