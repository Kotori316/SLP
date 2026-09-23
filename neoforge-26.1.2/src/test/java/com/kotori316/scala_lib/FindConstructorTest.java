package com.kotori316.scala_lib;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class FindConstructorTest {

    private static final ConstructorSelector SELECTOR = new ConstructorSelector(_ -> {
    });

    @NotNull
    private static Map.Entry<Constructor<?>, Object[]> getConstructorEntry(Class<?> modClass) {
        return SELECTOR.select(modClass, modClass.getSimpleName(), Map.of(
            IEventBus.class, Mockito.mock(IEventBus.class),
            ModContainer.class, Mockito.mock(ModContainer.class),
            Dist.class, Dist.DEDICATED_SERVER
        ));
    }

    @Test
    void noArgs() throws ReflectiveOperationException {
        var c = getConstructorEntry(this.getClass());
        var expected = getClass().getConstructor();
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    @Test
    void withBus() throws ReflectiveOperationException {
        var modClass = WithBus.class;
        var c = getConstructorEntry(modClass);
        var expected = modClass.getDeclaredConstructor(IEventBus.class);
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    private static final class WithBus {
        private WithBus(IEventBus ignored) {
        }
    }

    @Test
    void with3() throws ReflectiveOperationException {
        var modClass = With3.class;
        var c = getConstructorEntry(modClass);
        var expected = modClass.getDeclaredConstructor(IEventBus.class, ModContainer.class, Dist.class);
        assertEquals(expected.getParameterCount(), c.getValue().length);
        assertEquals(expected, c.getKey());
    }

    private static final class With3 {
        private With3(IEventBus ignored1, ModContainer ignored2, Dist ignored3) {
        }
    }
}
