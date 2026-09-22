package com.kotori316.scala_lib;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.unsafe.UnsafeHacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.jar.Attributes;

import static net.minecraftforge.fml.Logging.LOADING;

/**
 * All methods are copied from {@link net.minecraftforge.fml.javafmlmod.FMLModContainer}
 * <p>
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
final class ModuleOpener {
    private static final Logger LOGGER = LogManager.getLogger(ModuleOpener.class);
    static final String OPEN_KEY = "Add-Opens";
    static final String EXPORT_KEY = "Add-Exports";

    static void openModule(ModuleLayer layer, Module self, SecureJar jar) throws Exception {
        var manifest = jar.moduleDataProvider().getManifest().getMainAttributes();
        addOpenOrExports(layer, self, true, manifest);
        addOpenOrExports(layer, self, false, manifest);
    }

    private static void addOpenOrExports(ModuleLayer layer, Module self, boolean open, Attributes attrs) throws Exception {
        String key = open ? OPEN_KEY : EXPORT_KEY;
        var entry = attrs.getValue(key);
        if (entry == null) {
            return;
        }

        // Cases for `Add-Exports: <module>/<package>( <module>/<package>)*`
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
                LOGGER.warn(LOADING, "Invalid {} entry in {}: {}", key, self.getName(), pair);
            }
        }
    }

    private static Method implAddExportsOrOpens;

    private static void addOpenOrExport(Module target, String pkg, Module reader, boolean open) throws Exception {
        if (implAddExportsOrOpens == null) {
            implAddExportsOrOpens = Module.class.getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
            UnsafeHacks.setAccessible(implAddExportsOrOpens);
        }

        LOGGER.info(LOADING, "{} {}/{} to {}", open ? "Opening" : "Exporting", target.getName(), pkg, reader.getName());
        implAddExportsOrOpens.invoke(target, pkg, reader, open, /*syncVM*/true);
    }
}
