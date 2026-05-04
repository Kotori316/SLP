package com.kotori316.scala_lib;

import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.VisibleForTesting;
import org.objectweb.asm.Type;

import java.util.Set;

record ModClassDataImpl(String className, String modID, Set<Dist> availableDistSet) implements ModClassData<Dist> {
    @VisibleForTesting
    ModClassDataImpl(String className, String modID) {
        this(className, modID, Set.of());
    }

    static ModClassData<Dist> of(Type t, String modId, Set<Dist> availableDistSet) {
        return new ModClassDataImpl(t.getClassName(), modId, availableDistSet);
    }
}
