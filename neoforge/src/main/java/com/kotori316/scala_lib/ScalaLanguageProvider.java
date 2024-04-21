package com.kotori316.scala_lib;

import net.neoforged.neoforgespi.language.IModLanguageProvider;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.neoforged.fml.Logging.SCAN;

public final class ScalaLanguageProvider implements IModLanguageProvider {
    private static final Type MOD_ANNOTATION = Type.getType("Lnet/neoforged/fml/common/Mod;");
    static final Logger LOGGER = LogManager.getLogger(ScalaLanguageProvider.class);

    @Override
    public String name() {
        return "kotori_scala";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return scanData -> {
            var annotatedClasses = scanData.getAnnotations().stream()
                .filter(t -> t.annotationType().equals(MOD_ANNOTATION))
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
}
