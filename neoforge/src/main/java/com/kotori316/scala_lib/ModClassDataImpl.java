package com.kotori316.scala_lib;

import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Set;

record ModClassDataImpl(String className, String modID, Set<Dist> availableDistSet) implements ModClassData {
    @VisibleForTesting
    ModClassDataImpl(String className, String modID) {
        this(className, modID, Set.of());
    }
}
