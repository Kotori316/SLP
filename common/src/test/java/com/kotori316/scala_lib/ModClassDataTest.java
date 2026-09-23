package com.kotori316.scala_lib;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
        void classAndObjAllowDuplication() {
            var targets = Set.of(
                new ModClassDataImpl("com.kotori316.test.Mod1$", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                new ModClassDataImpl("com.kotori316.test.Mod1_other", "test1")
            );
            var result = Set.copyOf(assertDoesNotThrow(() -> ModClassData.findInstance(targets, true)));
            assertEquals(targets, result);
        }

        static Stream<Collection<ModClassDataImpl>> duplicationTestCases() {
            return Stream.of(
                List.of(
                    new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                    new ModClassDataImpl("com.kotori316.test.Mod1", "test1")
                ),
                Set.of(
                    new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                    new ModClassDataImpl("com.kotori316.test.Mod2", "test1")
                ),
                Set.of(
                    new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                    new ModClassDataImpl("com.kotori316.test.Mod2", "test1"),
                    new ModClassDataImpl("com.kotori316.test.Mod3", "test1")
                ),
                Set.of(
                    new ModClassDataImpl("com.kotori316.test.Mod1", "test1"),
                    new ModClassDataImpl("com.kotori316.test.Mod2", "test2"),
                    new ModClassDataImpl("com.kotori316.test.Mod3", "test1")
                )
            );
        }

        @ParameterizedTest
        @MethodSource("duplicationTestCases")
        void duplication(Collection<ModClassDataImpl> targets) {
            assertThrows(RuntimeException.class, () -> ModClassData.findInstance(targets));
        }

        @ParameterizedTest
        @MethodSource("duplicationTestCases")
        void disAllowDuplication(Collection<ModClassDataImpl> targets) {
            assertThrows(RuntimeException.class, () -> ModClassData.findInstance(targets, false));
        }

        @ParameterizedTest
        @MethodSource("duplicationTestCases")
        void allowDuplication(Collection<ModClassDataImpl> targets) {
            assertDoesNotThrow(() -> ModClassData.findInstance(targets, true));
        }
    }

    record ModClassDataImpl(String className, String modID) implements ModClassData<String> {
        @Override
        public Set<String> availableDistSet() {
            return Set.of();
        }
    }
}
