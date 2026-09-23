package com.kotori316.scala_lib;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Selects the best-fitting constructor for a mod class given a map of available argument types.
 * <p>
 * The algorithm: among all declared constructors whose every parameter type is present in
 * {@code args}, pick the one with the most parameters. This is the shared core logic
 * used by both the NeoForge and Forge {@code ScalaModContainer} implementations.
 */
public final class ConstructorSelector {

    private final Consumer<String> traceLog;

    public ConstructorSelector(Consumer<String> traceLog) {
        this.traceLog = traceLog;
    }

    /**
     * @param modClass the mod class to instantiate
     * @param modId    mod id, used only in error/trace messages
     * @param args     map from parameter type to the value to pass for that type
     * @return the selected constructor and the corresponding argument array
     * @throws RuntimeException if no constructor matches
     */
    public Map.Entry<Constructor<?>, Object[]> select(Class<?> modClass, String modId, Map<Class<?>, Object> args) {
        var constructors = modClass.getDeclaredConstructors();
        traceLog.accept("Found %d constructors for %s".formatted(constructors.length, modId));
        var constructor = Stream.of(constructors)
            .filter(c -> Stream.of(c.getParameterTypes()).allMatch(args::containsKey))
            .max(Comparator.comparingInt(Constructor::getParameterCount))
            .orElseThrow(() -> new RuntimeException(
                "No mod constructor with allowed arg types were found for " + modId));
        var constructorArgs = Stream.of(constructor.getParameterTypes()).map(args::get).toArray();
        return Map.entry(constructor, constructorArgs);
    }
}
