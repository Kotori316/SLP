package com.kotori316.scala_lib;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstructorSelectorTest {

    // Mirrors real usage: String=bus, Integer=container, Side=dist (enum)
    enum Side {SERVER, CLIENT}

    private static final ConstructorSelector SELECTOR = new ConstructorSelector(_ -> {
    });

    // --- full args (all 3 types available) ---

    private static final Map<Class<?>, Object> ALL_ARGS = Map.of(
        String.class, "bus",
        Integer.class, 42,
        Side.class, Side.SERVER
    );

    @Test
    void noArgs_withFullArgs() throws ReflectiveOperationException {
        var c = SELECTOR.select(NoArgs.class, "test", ALL_ARGS);
        assertEquals(NoArgs.class.getDeclaredConstructor(), c.getKey());
        assertEquals(0, c.getValue().length);
    }

    @Test
    void withBusOnly_withFullArgs() throws ReflectiveOperationException {
        var c = SELECTOR.select(WithBus.class, "test", ALL_ARGS);
        assertEquals(WithBus.class.getDeclaredConstructor(String.class), c.getKey());
        assertEquals(1, c.getValue().length);
        assertEquals("bus", c.getValue()[0]);
    }

    @Test
    void withDistOnly_withFullArgs() throws ReflectiveOperationException {
        var c = SELECTOR.select(WithDist.class, "test", ALL_ARGS);
        assertEquals(WithDist.class.getDeclaredConstructor(Side.class), c.getKey());
        assertEquals(1, c.getValue().length);
        assertEquals(Side.SERVER, c.getValue()[0]);
    }

    @Test
    void withBusAndContainer_withFullArgs() throws ReflectiveOperationException {
        var c = SELECTOR.select(WithBusAndContainer.class, "test", ALL_ARGS);
        assertEquals(WithBusAndContainer.class.getDeclaredConstructor(String.class, Integer.class), c.getKey());
        assertEquals(2, c.getValue().length);
    }

    @Test
    void withAllThree_withFullArgs() throws ReflectiveOperationException {
        var c = SELECTOR.select(WithAll.class, "test", ALL_ARGS);
        assertEquals(WithAll.class.getDeclaredConstructor(String.class, Integer.class, Side.class), c.getKey());
        assertEquals(3, c.getValue().length);
    }

    /**
     * Full args available → busOrNoArg picks WithBus (more params).
     */
    @Test
    void prefersMoreArgs_withFullArgs() throws ReflectiveOperationException {
        var c = SELECTOR.select(WithNoArgAndBus.class, "test", ALL_ARGS);
        assertEquals(WithNoArgAndBus.class.getDeclaredConstructor(String.class), c.getKey());
    }

    // --- restricted args (subset available) ---

    /**
     * Only Side in args → WithNoArgAndBus falls back to no-arg (String absent).
     */
    @Test
    void prefersMoreArgs_fallsBackToNoArg_whenBusAbsent() throws ReflectiveOperationException {
        var args = Map.<Class<?>, Object>of(Side.class, Side.SERVER);
        var c = SELECTOR.select(WithNoArgAndBus.class, "test", args);
        assertEquals(WithNoArgAndBus.class.getDeclaredConstructor(), c.getKey());
        assertEquals(0, c.getValue().length);
    }

    /**
     * Only String in args → WithAll (needs Integer+Side too) is not selectable → fails.
     */
    @Test
    void withAllThree_failsWhenArgsMissing() {
        var args = Map.<Class<?>, Object>of(String.class, "bus");
        assertThrows(RuntimeException.class, () ->
            SELECTOR.select(WithAll.class, "test", args));
    }

    /**
     * String+Integer in args → WithAll (needs Side too) not selectable → fails.
     */
    @Test
    void withAllThree_failsWhenOneArgMissing() {
        var args = Map.<Class<?>, Object>of(String.class, "bus", Integer.class, 42);
        assertThrows(RuntimeException.class, () ->
            SELECTOR.select(WithAll.class, "test", args));
    }

    /**
     * Unknown type in args → no valid constructor → fails.
     */
    @Test
    void unknownArgTypeFails() {
        assertThrows(RuntimeException.class, () ->
            SELECTOR.select(WithUnknownType.class, "test", ALL_ARGS));
    }

    /**
     * Unknown type overload ignored; the no-arg overload is still valid.
     */
    @Test
    void unknownTypeOverloadFallsBackToValid() throws ReflectiveOperationException {
        var c = SELECTOR.select(WithUnknownAndNoArg.class, "test", ALL_ARGS);
        assertEquals(WithUnknownAndNoArg.class.getDeclaredConstructor(), c.getKey());
        assertEquals(0, c.getValue().length);
    }

    // --- inner classes ---

    private static final class NoArgs {
        private NoArgs() {
        }
    }

    private static final class WithBus {
        private WithBus(String ignored) {
        }
    }

    private static final class WithDist {
        private WithDist(Side ignored) {
        }
    }

    private static final class WithBusAndContainer {
        private WithBusAndContainer(String ignored1, Integer ignored2) {
        }
    }

    private static final class WithAll {
        private WithAll(String ignored1, Integer ignored2, Side ignored3) {
        }
    }

    private static final class WithNoArgAndBus {
        private WithNoArgAndBus() {
        }

        private WithNoArgAndBus(String ignored) {
        }
    }

    private static final class WithUnknownType {
        private WithUnknownType(Long unknown) {
        }
    }

    private static final class WithUnknownAndNoArg {
        private WithUnknownAndNoArg() {
        }

        private WithUnknownAndNoArg(Long unknown) {
        }
    }
}
