package com.kotori316.scala_lib;

import java.lang.reflect.Constructor;
import java.util.Optional;

import net.minecraftforge.eventbus.EventBusErrorMessage;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.AutomaticEventSubscriber;
import net.minecraftforge.fml.LifecycleEventProvider;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.Logging.LOADING;

public class ScalaModContainer extends ModContainer {
    private static final Logger LOGGER = LogManager.getLogger(ScalaModContainer.class);

    private final IModInfo info;
    private final String className;
    private final ClassLoader modClassLoader;
    private final ModFileScanData scanData;

    private final boolean isScalaObject;

    private final IEventBus eventBus;
    private Class<?> modClass;
    private Object modInstance;

    /**
     * Instance created in {@link ScalaLanguageTarget#loadMod(IModInfo, ClassLoader, ModFileScanData)}
     */
    public ScalaModContainer(IModInfo info, String className, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
        super(info);
        this.info = info;
        this.className = className;
        this.modClassLoader = modClassLoader;
        LOGGER.debug(LOADING, "Creating scala container Class: {}, with classLoader {} & {}", className, modClassLoader, getClass().getClassLoader());
        this.scanData = modFileScanResults;
        isScalaObject = className.endsWith("$");

        Runnable changeStage = () -> this.modLoadingStage = ModLoadingStage.ERROR;
        triggerMap.put(ModLoadingStage.CONSTRUCT, dummy().andThen(this::beforeEvent).andThen(this::constructMod).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.CREATE_REGISTRIES, dummy().andThen(this::beforeEvent).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.LOAD_REGISTRIES, dummy().andThen(this::beforeEvent).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.COMMON_SETUP, dummy().andThen(this::beforeEvent).andThen(this::preInitMod).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.SIDED_SETUP, dummy().andThen(this::beforeEvent).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.ENQUEUE_IMC, dummy().andThen(this::beforeEvent).andThen(this::initMod).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.PROCESS_IMC, dummy().andThen(this::beforeEvent).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.COMPLETE, dummy().andThen(this::beforeEvent).andThen(this::completeLoading).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));
        triggerMap.put(ModLoadingStage.GATHERDATA, dummy().andThen(this::beforeEvent).andThen(this::fireEvent).andThen(this::afterEvent).build(changeStage));

        this.eventBus = BusBuilder.builder().setExceptionHandler(this::onEventFailed).setTrackPhases(false).markerType(IModBusEvent.class).build();
        this.configHandler = Optional.of(this.eventBus::post);
        final FMLJavaModLoadingContext contextExtension = createContext(eventBus);
        this.contextExtension = () -> contextExtension;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void fireEvent(LifecycleEventProvider.LifecycleEvent lifecycleEvent) {
        final Event event = lifecycleEvent.getOrBuildEvent(this);
        LOGGER.debug(LOADING, "Firing event for modid {} : {}", this.getModId(), event);
        try {
            eventBus.post(event);
            LOGGER.debug(LOADING, "Fired event for modid {} : {}", this.getModId(), event);
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Caught exception during event {} dispatch for modid {}", event, this.getModId(), e);
            throw new ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", e);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void constructMod(LifecycleEventProvider.LifecycleEvent event) {
        if (isScalaObject) {
            try {
                modClass = Class.forName(className, true, modClassLoader);
                LOGGER.debug(LOADING, "Scala Class Loaded {} with {}.", modClass, modClass.getClassLoader());
                modInstance = modClass.getField("MODULE$").get(null);
                LOGGER.debug(LOADING, "Scala Mod instance for {} was got. {}", getModId(), modInstance);
            } catch (ClassNotFoundException e) {
                LOGGER.error(LOADING, "Failed to load class {}", className, e);
                throw new ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                LOGGER.error(LOADING, "Failed to create mod instance. ModID: {}, class {}", getModId(), modClass.getName(), e);
                throw new ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass);
            }
        } else {
            try {
                modClass = Class.forName(className, true, modClassLoader);
                LOGGER.debug(LOADING, "Scala Class Loaded {} with {}.", modClass, modClass.getClassLoader());
                LOGGER.debug(LOADING, "Scala Mod instance for {} is about to create. {}", getModId(), modClass.getName());
                Constructor<?> constructor = modClass.getConstructor();
                constructor.setAccessible(true);
                modInstance = constructor.newInstance();
                LOGGER.debug(LOADING, "Scala Mod instance for {} created. {}", getModId(), modInstance);
            } catch (ClassNotFoundException e) {
                LOGGER.error(LOADING, "Failed to load class {}", className, e);
                throw new ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e);
            } catch (ReflectiveOperationException e) {
                LOGGER.error(LOADING, "Failed to create mod instance. ModID: {}, class {}", getModId(), modClass.getName(), e);
                throw new ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass);
            }
        }

        try {
            LOGGER.debug(LOADING, "Injecting Automatic event subscribers for {}", getModId());
            AutomaticEventSubscriber.inject(this, this.scanData, this.modClass.getClassLoader());
            LOGGER.debug(LOADING, "Completed Automatic event subscribers for {}", getModId());
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Failed to register automatic subscribers. ModID: {}, class {}", getModId(), modClass.getName(), e);
            throw new ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass);
        }
    }

    private void onEventFailed(IEventBus bus, Event event, IEventListener[] listeners, int i, Throwable throwable) {
        LOGGER.error(new EventBusErrorMessage(event, i, listeners, throwable));
    }

    private void preInitMod(LifecycleEventProvider.LifecycleEvent event) {

    }

    private void initMod(LifecycleEventProvider.LifecycleEvent event) {

    }

    private void completeLoading(LifecycleEventProvider.LifecycleEvent event) {
//        LOGGER.debug(LOADING, "Scala container for {}: Load completed {}.", className, event);
    }

    private void beforeEvent(LifecycleEventProvider.LifecycleEvent event) {
//        LOGGER.debug(LOADING, "Scala container for {} starts {}.", className, event);
    }

    private void afterEvent(LifecycleEventProvider.LifecycleEvent event) {
        if (getCurrentState() == ModLoadingStage.ERROR) {
            LOGGER.error(LOADING, "An error occurred while dispatching event {} to {}", event.fromStage(), getModId());
            this.eventBus.shutdown();
        } else {
            assert true; // Dummy to avoid empty else block.
//            LOGGER.debug(LOADING, "Scala container for {} ends {}.", className, event);
        }
    }

    private static ScalaEventAcceptor.Builder<LifecycleEventProvider.LifecycleEvent> dummy() {
        return ScalaEventAcceptor.dummy();
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

    @Override
    @SuppressWarnings("deprecation")
    protected void acceptEvent(Event e) {
        if (this.shutdown) return; // Remove in 1.16.2
        this.getEventBus().post(e);
    }

    public static FMLJavaModLoadingContext createContext(IEventBus bus) {
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
}
