package com.kotori316.scala_lib;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.unsafe.UnsafeHacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.Logging.LOADING;

/**
 * All methods are copied from {@link net.minecraftforge.fml.javafmlmod.FMLModContainer}
 * <p>
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
final class ModuleOpener {
    private static final Logger LOGGER = LogManager.getLogger(ModuleOpener.class);

    static void openModule(ModuleLayer layer, Module self, SecureJar jar) throws Exception {
        var manifest = jar.moduleDataProvider().getManifest().getMainAttributes();
        var helper = new ModuleHelper(
            msg -> LOGGER.info(LOADING, msg),
            msg -> LOGGER.warn(LOADING, msg),
            UnsafeHacks::setAccessible
        );
        helper.addOpenOrExports(layer, self, true, manifest);
        helper.addOpenOrExports(layer, self, false, manifest);
    }
}
