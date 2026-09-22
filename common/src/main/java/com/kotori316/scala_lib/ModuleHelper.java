package com.kotori316.scala_lib;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.jar.Attributes;

/**
 * All logics are copied from {@link net.minecraftforge.fml.javafmlmod.FMLModContainer}
 * <p>
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
final class ModuleHelper {
    private static final String OPEN_KEY = "Add-Opens";
    private static final String EXPORT_KEY = "Add-Exports";

    private final Method implAddExportsOrOpens;
    private final Consumer<String> infoLog;
    private final Consumer<String> warnLog;

    ModuleHelper(Consumer<String> infoLog, Consumer<String> warnLog, Consumer<Method> makeAccessible) throws NoSuchMethodException {
        this.implAddExportsOrOpens = Module.class.getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
        makeAccessible.accept(this.implAddExportsOrOpens);
        this.infoLog = infoLog;
        this.warnLog = warnLog;
    }

    void addOpenOrExports(ModuleLayer layer, Module self, boolean open, Attributes attrs) throws Exception {
        String key = open ? OPEN_KEY : EXPORT_KEY;
        var entry = attrs.getValue(key);
        if (entry == null) {
            return;
        }

        for (var pair : entry.split(" ")) {
            var pts = pair.trim().split("/");
            if (pts.length == 2) {
                var module = pts[0];
                var packageName = pts[1];
                var target = layer.findModule(module).orElse(null);
                if (target == null || !target.getDescriptor().packages().contains(packageName)) {
                    continue;
                }
                addOpenOrExport(target, packageName, self, open);
            } else {
                warnLog.accept("Invalid %s entry in %s: %s".formatted(key, self.getName(), pair));
            }
        }
    }

    private void addOpenOrExport(Module target, String pkg, Module reader, boolean open) throws Exception {
        infoLog.accept("%s %s/%s to %s".formatted(open ? "Opening" : "Exporting", target.getName(), pkg, reader.getName()));
        implAddExportsOrOpens.invoke(target, pkg, reader, open, true);
    }
}
