package com.kotori316.scala_lib.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ConfigJava {
    @Test
    void testBoolean1() {
        ConfigTemplate template = ConfigTemplate.debugTemplate();
        BooleanKey key = ConfigKey.createBoolean(template, "key1", true);

        assertEquals(Boolean.TRUE, template.get(key));
        assertEquals(Boolean.TRUE, key.get());
        assertTrue(template.getBoolean(key));
        assertTrue(key.get());
    }

    @Test
    void testBoolean2() {
        ConfigTemplate template = ConfigTemplate.debugTemplate();
        BooleanKey key = ConfigKey.createBoolean(template, "key1", false);

        assertEquals(Boolean.FALSE, template.get(key));
        assertEquals(Boolean.FALSE, key.get());
        assertFalse(template.getBoolean(key));
    }

    private static void intEqual(int expected, int actual) {
        assertEquals(expected, actual);
    }

    @Test
    void testInt1() {
        ConfigTemplate template = ConfigTemplate.debugTemplate();
        IntKey key = ConfigKey.createInt(template, "key1", 5);
        IntKey key2 = ConfigKey.createInt(template, "key1", -9);

        intEqual(5, template.getInt(key));
        assertEquals(5, template.get(key));
        intEqual(-9, template.getInt(key2));
        intEqual(-9, key2.get());
    }

    @Test
    void testDouble() {
        ConfigTemplate template = ConfigTemplate.debugTemplate();
        DoubleKey key = ConfigKey.createDouble(template, "key1", 6.12);
        assertEquals(6.12, template.getDouble(key));
        assertEquals(6.12, template.get(key));
    }

    @Test
    void testSubCategory() {
        ConfigTemplate parent = ConfigTemplate.debugTemplate();
        ConfigTemplate.ChildTemplate child1 = new ConfigTemplate.DebugChildTemplate(parent, "sub1");
        ConfigTemplate.ChildTemplate child2 = new ConfigTemplate.DebugChildTemplate(parent, "sub2");
        SubCategoryKey key1 = ConfigKey.createSubCategory(child1);
        SubCategoryKey key2 = ConfigKey.createSubCategory(child2);

        assertEquals(child1, parent.get(key1));
        assertEquals(child1, key1.get());
        assertNotEquals(child1, parent.get(key2));
        assertEquals(child2, parent.get(key2));
        assertEquals(child2, key2.get());
        assertNotEquals(child2, parent.get(key1));
    }
}
