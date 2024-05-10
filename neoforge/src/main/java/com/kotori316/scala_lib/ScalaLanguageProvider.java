package com.kotori316.scala_lib;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.IIssueReporting;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModLanguageLoader;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.neoforged.fml.Logging.SCAN;

public final class ScalaLanguageProvider implements IModLanguageLoader {
    static final Logger LOGGER = LogManager.getLogger(ScalaLanguageProvider.class);

    @Override
    public String name() {
        return "kotori_scala";
    }

    @Override
    public ModContainer loadMod(IModInfo info, ModFileScanData modFileScanResults, ModuleLayer layer) throws ModLoadingException {
        var annotatedClasses = modFileScanResults.getAnnotatedBy(Mod.class, ElementType.TYPE)
            .map(data -> {
                var clazz = data.clazz();
                var id = (String) data.annotationData().get("value");
                var dist = data.annotationData().get("dist");
                return ModClassData.of(clazz, id, AutomaticEventSubscriber.getSides(dist));
            }).toList();
        var modClasses = ModClassData.findInstance(
                annotatedClasses,
                t -> LOGGER.error("Error in loading {}. No acceptable class found", t)
            ).stream()
            .filter(d -> d.availableDistSet().contains(FMLLoader.getDist()))
            .map(ModClassData::className)
            .toList();
        return new ScalaModContainer(info, modClasses, modFileScanResults, layer);
    }

    @Override
    public void validate(IModFile file, Collection<ModContainer> loadedContainers, IIssueReporting reporter) {
        IModLanguageLoader.super.validate(file, loadedContainers, reporter);

        var modIdSet = file.getModInfos().stream()
            .filter(m -> m.getLoader() == this)
            .map(IModInfo::getModId)
            .collect(Collectors.toUnmodifiableSet());

        var mods = file.getScanResult().getAnnotatedBy(Mod.class, ElementType.TYPE)
            .map(data -> {
                var clazz = data.clazz();
                var id = (String) data.annotationData().get("value");
                var dist = data.annotationData().get("dist");

                return ModClassData.of(clazz, id, AutomaticEventSubscriber.getSides(dist));
            })
            .peek(a -> LOGGER.debug(SCAN, "Found @Mod class {} with id {}", a.className(), a.modID()))
            .collect(Collectors.groupingBy(ModClassData::modID));
        mods.keySet().stream().filter(Predicate.not(modIdSet::contains))
            .forEach(modId -> {
                var data = mods.get(modId);
                var classes = data.stream().map(ModClassData::className).toList();
                var issue = ModLoadingIssue.error("fml.modloading.javafml.dangling_entrypoint", modId, classes, file.getFilePath()).withAffectedModFile(file);
                reporter.addIssue(issue);
            });
        mods.values().stream()
            // Check mod has only 1 class for each mod id
            .filter(dataList -> dataList.size() != 1 || dataList.stream().filter(ModClassData::isScalaObj).count() != 1)
            .forEach(dataList -> {
                var modId = dataList.getFirst().modID();
                var classes = dataList.stream().map(ModClassData::className).toList();
                var issue = ModLoadingIssue.error("Duplicated mod classes for %s, found: %s", modId, classes);
                reporter.addIssue(issue);
            });
    }
}
