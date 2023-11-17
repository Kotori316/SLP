package com.kotori316.scala_lib;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import static com.kotori316.scala_lib.ScalaLanguageProvider.LOGGER;
import static net.minecraftforge.fml.Logging.LOADING;

public record ScalaLanguageTarget(String className, String modID)
    implements IModLanguageProvider.IModLanguageLoader, ModClassData {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T loadMod(IModInfo info, ModFileScanData modFileScanResults, ModuleLayer moduleLayer) {
        try {
            var scalaContainer = Class.forName("com.kotori316.scala_lib.ScalaModContainer", true, Thread.currentThread().getContextClassLoader());
            LOGGER.debug(LOADING, "Loading ScalaModContainer from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), scalaContainer.getClassLoader());
            var constructor = scalaContainer.getConstructor(IModInfo.class, String.class, ModFileScanData.class, ModuleLayer.class);
            return (T) constructor.newInstance(info, className, modFileScanResults, moduleLayer);
        } catch (InvocationTargetException invocationTargetException) {
            LOGGER.fatal(LOADING, "Failed to build mod", invocationTargetException);
            if (invocationTargetException.getTargetException() instanceof ModLoadingException modLoadingException) {
                throw modLoadingException;
            } else {
                throw createMLE(info, invocationTargetException);
            }
        } catch (ReflectiveOperationException reflectiveOperationException) {
            LOGGER.fatal(LOADING, "Unable to load ScalaModContainer", reflectiveOperationException);
            throw createMLE(info, reflectiveOperationException);
        }
    }

    private static ModLoadingException createMLE(IModInfo info, ReflectiveOperationException e) {
        return new ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.ModLoading.FailedToLoadModClass".toLowerCase(Locale.ROOT), e);
    }
}
