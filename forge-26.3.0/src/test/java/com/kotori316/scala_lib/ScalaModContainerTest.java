package com.kotori316.scala_lib;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScalaModContainerTest {

    @NotNull
    private static Map.Entry<Constructor<?>, Object[]> getConstructorEntry(Class<?> modClass) {
        return ScalaModContainer.getConstructor(modClass, modClass.getSimpleName(),
            BusGroup.create("%s-%s".formatted("ScalaModContainerTest", UUID.randomUUID())), Mockito.mock(ModContainer.class), Dist.DEDICATED_SERVER, Mockito.mock(FMLJavaModLoadingContext.class));
    }

    @Test
    void noArgs() throws ReflectiveOperationException {
        var c = getConstructorEntry(NoArgs.class);
        var expected = NoArgs.class.getDeclaredConstructor();
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    @Test
    void withBus() throws ReflectiveOperationException {
        var modClass = WithBus.class;
        var c = getConstructorEntry(modClass);
        var expected = modClass.getDeclaredConstructor(BusGroup.class);
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    @Test
    void withContext() throws ReflectiveOperationException {
        var modClass = WithContext.class;
        var c = getConstructorEntry(modClass);
        var expected = modClass.getDeclaredConstructor(FMLJavaModLoadingContext.class);
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    private static final class NoArgs {
        private NoArgs() {
        }
    }

    private static final class WithBus {
        private WithBus(BusGroup ignored) {
        }
    }

    private static final class WithContext {
        private WithContext(FMLJavaModLoadingContext ignored) {
        }
    }

    @Test
    void with3() throws ReflectiveOperationException {
        var modClass = With3.class;
        var c = getConstructorEntry(modClass);
        var expected = modClass.getDeclaredConstructor(BusGroup.class, ModContainer.class, Dist.class);
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    private static final class With3 {
        private With3(BusGroup ignored1, ModContainer ignored2, Dist ignored3) {
        }
    }

    private static final class With4 {
        private With4(BusGroup ignored1, ModContainer ignored2, Dist ignored3, FMLJavaModLoadingContext ignored4) {
        }
    }

    @Test
    void with4() throws ReflectiveOperationException {
        var modClass = With4.class;
        var c = getConstructorEntry(modClass);
        var expected = modClass.getDeclaredConstructor(BusGroup.class, ModContainer.class, Dist.class, FMLJavaModLoadingContext.class);
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }
}