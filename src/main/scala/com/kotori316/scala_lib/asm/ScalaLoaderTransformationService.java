package com.kotori316.scala_lib.asm;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import javax.annotation.Nonnull;
import net.minecraftforge.fml.loading.LogMarkers;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is trying to pass jar path to mixin loading.
 * Unfortunately, this solution didn't work due to a restriction of forge.
 *
 * @see net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator
 * @see net.minecraftforge.fml.loading.ModDirTransformerDiscoverer#allExcluded()
 */
public class ScalaLoaderTransformationService implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger(ScalaLoaderTransformationService.class);

    @Nonnull
    @Override
    public String name() {
        return "kotori_scala_trans";
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void beginScanning(IEnvironment environment) {
    }

    @Override
    public List<Map.Entry<String, Path>> runScan(IEnvironment environment) {
        try {
            Path jarPath = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            LOGGER.debug(LogMarkers.SCAN, "Find `kotori_scala` jar file path. " + jarPath);
            return Collections.singletonList(Pair.of(jarPath.getFileName().toString(), jarPath));
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Error when getting jar file path. %s", e.getInput()), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
    }

    @SuppressWarnings("rawtypes")
    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }
}
