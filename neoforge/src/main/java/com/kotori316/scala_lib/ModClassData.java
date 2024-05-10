package com.kotori316.scala_lib;

import net.neoforged.api.distmarker.Dist;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface ModClassData {
    String className();

    String modID();

    Set<Dist> availableDistSet();

    default boolean isScalaObj() {
        return className().endsWith("$");
    }

    static <T extends ModClassData> List<T> findInstance(Collection<T> targets) {
        return findInstance(targets, t -> {
            throw new RuntimeException("Exception in loading mods. %s".formatted(targets));
        });
    }

    static <T extends ModClassData> List<T> findInstance(Collection<T> targets, Consumer<Collection<T>> onError) {
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

    static ModClassData of(Type t, String modId, Set<Dist> availableDistSet) {
        return new ModClassDataImpl(t.getClassName(), modId, availableDistSet);
    }
}
