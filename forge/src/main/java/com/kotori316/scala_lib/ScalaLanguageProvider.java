package com.kotori316.scala_lib;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import static net.minecraftforge.fml.Logging.SCAN;

public final class ScalaLanguageProvider implements IModLanguageProvider {
    private static final Type MODANNOTATION = Type.getType("Lnet/minecraftforge/fml/common/Mod;");
    static final Logger LOGGER = LogManager.getLogger(ScalaLanguageProvider.class);

    @Override
    public String name() {
        return "kotori_scala";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return scanData -> {
            var annotatedClasses = scanData.getAnnotations().stream()
                .filter(t -> t.annotationType().equals(MODANNOTATION))
                .map(data -> {
                    var className = data.clazz().getClassName();
                    var id = (String) data.annotationData().get("value");
                    return new ScalaLanguageTarget(className, id);
                }).toList();
            var targets = ModClassData.findInstance(annotatedClasses);
            var map = targets.stream()
                .peek(a -> LOGGER.debug(SCAN, "Found @Mod class {} with id {}", a.className(), a.modID()))
                .collect(Collectors.toMap(ModClassData::modID, Function.identity()));
            scanData.addLanguageLoader(map);
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {
    }
}
