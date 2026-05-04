package com.kotori316.scala_lib;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface ModClassData<DIST> {
    String className();

    String modID();

    Set<DIST> availableDistSet();

    default boolean isScalaObj() {
        return className().endsWith("$");
    }

    static <D, T extends ModClassData<D>> List<T> findInstance(Collection<T> targets) {
        return findInstance(targets, t -> {
            throw new RuntimeException("Exception in loading mods. %s".formatted(targets));
        });
    }

    static <D, T extends ModClassData<D>> List<T> findInstance(Collection<T> targets, Consumer<Collection<T>> onError) {
        var byModId = targets.stream().collect(Collectors.groupingBy(ModClassData::modID));
        return byModId.values().stream().<T>mapMulti((ts, c) -> {
            if (ts.size() == 1) {
                c.accept(ts.getFirst());
            } else {
                var objectData = ts.stream().filter(ModClassData::isScalaObj).toList();
                if (objectData.size() == 1) {
                    // Ignore anything but a Scala Object.
                    c.accept(objectData.getFirst());
                } else {
                    onError.accept(objectData);
                }
            }
        }).toList();
    }
}
