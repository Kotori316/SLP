package com.kotori316.scala_lib;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.EventBusErrorMessage;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.neoforged.fml.Logging.LOADING;

public class ScalaModContainer extends ModContainer {
    private static final Logger LOGGER = LogManager.getLogger(ScalaModContainer.class);

    private final List<String> entryPoints;
    private final ModuleLayer gameLayer;
    private final Module layer;
    private final ModFileScanData scanData;

    private final IEventBus eventBus;
    private List<Class<?>> modClasses;

    public ScalaModContainer(IModInfo info, List<String> entryPoints, ModFileScanData modFileScanResults, ModuleLayer gameLayer) {
        super(info);
        this.entryPoints = entryPoints;
        this.gameLayer = gameLayer;
        this.layer = gameLayer.findModule(info.getOwningFile().moduleName()).orElseThrow();
        LOGGER.debug(LOADING, "Creating scala container for {}, with classLoader {}", entryPoints, getClass().getClassLoader());
        this.scanData = modFileScanResults;

        this.eventBus = BusBuilder.builder()
            .setExceptionHandler(this::onEventFailed)
            .allowPerPhasePost()
            .markerType(IModBusEvent.class)
            .build();
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
        modClasses = new ArrayList<>();
        var context = ModLoadingContext.get();
        try {
            context.setActiveContainer(this);
            for (String entryPoint : entryPoints) {
                Class<?> modClass;
                try {
                    // Here to avoid NPE of scala object.
                    var layer = gameLayer.findModule(this.modInfo.getOwningFile().moduleName()).orElseThrow();
                    modClass = Class.forName(layer, entryPoint);
                    LOGGER.trace(LOADING, "Scala Class Loaded {} with {}.", modClass, modClass.getClassLoader());
                } catch (Throwable e) {
                    LOGGER.error(LOADING, "Failed to load class {}", entryPoint, e);
                    throw new ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmodclass")
                        .withAffectedMod(modInfo).withCause(e));
                }
                modClasses.add(modClass);
            }
        } finally {
            context.setActiveContainer(null);
        }

        for (Class<?> modClass : modClasses) {
            var isScalaObject = modClass.getName().endsWith("$");

            try {
                Object modInstance;
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
                Throwable cause;
                if (e instanceof InvocationTargetException i) cause = i.getCause();
                else cause = e;
                LOGGER.error(LOADING, "Failed to create/get mod instance. ModID: {}, class {}", this.modId, modClass.getName(), cause);
                throw new ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmod", cause, modClass)
                    .withAffectedMod(modInfo).withCause(cause));
            }
        }

        try {
            LOGGER.trace(LOADING, "Injecting Automatic event subscribers for {}", this.modId);
            AutomaticEventSubscriber.inject(this, this.scanData, this.layer);
            LOGGER.trace(LOADING, "Completed Automatic event subscribers for {}", this.modId);
        } catch (Throwable e) {
            LOGGER.error(LOADING, "Failed to register automatic subscribers. ModID: {}, Classes: {}", this.modId, entryPoints, e);
            throw new ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmod", e, entryPoints)
                .withAffectedMod(modInfo).withCause(e));
        }
    }

    private void onEventFailed(IEventBus bus, Event event, EventListener[] listeners, int i, Throwable throwable) {
        LOGGER.error(new EventBusErrorMessage(event, i, listeners, throwable));
    }

    @Override
    public IEventBus getEventBus() {
        return eventBus;
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
            ", entryPoints='" + entryPoints + '\'' +
            ", modClasses=" + (modClasses == null ? "<not initialized>" : modClasses) +
            '}';
    }
}
