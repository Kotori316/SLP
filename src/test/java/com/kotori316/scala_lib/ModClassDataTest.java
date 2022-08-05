package com.kotori316.scala_lib;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModClassDataTest {
    @Test
    void isScalaObj() {
        assertTrue(new ModClassDataImpl("com.kotori316.test.Mod$", "test1").isScalaObj());
    }

    @Test
    void isClass() {
        assertFalse(new ModClassDataImpl("com.kotori316.test.Mod", "test1").isScalaObj());
    }

    @Nested
    class FindInstanceTest {
        @Test
        void justOneClass() {
            var target = new ModClassDataImpl("com.kotori316.test.Mod", "test1");
            var result = assertDoesNotThrow(() -> ModClassData.findInstance(List.of(target)));
            assertEquals(List.of(target), result);
        }

        @Test
        void justOneObj() {
            var target = new ModClassDataImpl("com.kotori316.test.Mod$", "test1");
            var result = assertDoesNotThrow(() -> ModClassData.findInstance(List.of(target)));
            assertEquals(List.of(target), result);
        }

        @Test
        void twoModId() {
            var targets = Set.of(new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod2", "test2"));
            var result = Set.copyOf(assertDoesNotThrow(() -> ModClassData.findInstance(targets)));
            assertEquals(targets, result);
        }

        @Test
        void threeModIdWithObject() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1$", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod2", "test2"),
                new ModClassDataImpl("com.kotori316.test.Mod3$", "test3"),
                new ModClassDataImpl("com.kotori316.test.Mod3", "test3"),
                new ModClassDataImpl("com.kotori316.test.Mod3_1", "test3")
            );
            var result = Set.copyOf(assertDoesNotThrow(() -> ModClassData.findInstance(targets)));
            assertEquals(Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1$", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod2", "test2"),
                new ModClassDataImpl("com.kotori316.test.Mod3$", "test3")
            ), result);
        }

        @Test
        void classAndObj() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1$", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1")
            );
            var result = Set.copyOf(assertDoesNotThrow(() -> ModClassData.findInstance(targets)));
            assertEquals(Set.of(new ModClassDataImpl("com.kotori316.test.Mod1$", "test1")), result);
        }

        @Test
        void classAndObj2() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1$", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1_other", "test1")
            );
            var result = Set.copyOf(assertDoesNotThrow(() -> ModClassData.findInstance(targets)));
            assertEquals(Set.of(new ModClassDataImpl("com.kotori316.test.Mod1$", "test1")), result);
        }

        @Test
        void duplication1() {
            var targets = List.of(
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1")
            );
            assertThrows(RuntimeException.class, () -> ModClassData.findInstance(targets));
        }

        @Test
        void duplication2() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod2", "test1")
            );
            assertThrows(RuntimeException.class, () -> ModClassData.findInstance(targets));
        }

        @Test
        void duplication3() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod2", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod3", "test1")
            );
            assertThrows(RuntimeException.class, () -> ModClassData.findInstance(targets));
        }

        @Test
        void duplication4() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod2", "test2"),
                new ModClassDataImpl("com.kotori316.test.Mod3", "test1")
            );
            assertThrows(RuntimeException.class, () -> ModClassData.findInstance(targets));
        }
    }

    record ModClassDataImpl(String className, String modID) implements ModClassData {
    }
}