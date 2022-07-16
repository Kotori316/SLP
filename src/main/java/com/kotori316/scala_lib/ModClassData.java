package com.kotori316.scala_lib;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface ModClassData {
    String className();

    String modID();

    default boolean isScalaObj() {
        return className().endsWith("$");
    }

    static <T extends ModClassData> List<T> findInstance(Collection<T> targets) {
        var byModId = targets.stream().collect(Collectors.groupingBy(ModClassData::modID));
        return byModId.values().stream().map(ts -> {
            if (ts.size() == 1) {
                return ts.get(0);
            } else {
                var classData = ts.stream().filter(Predicate.not(ModClassData::isScalaObj)).toList();
                if (classData.size() == 1) {
                    return classData.get(0);
                } else {
                    throw new RuntimeException("Exception in loading mods. %s".formatted(targets));
                }
            }
        }).toList();
    }
}
