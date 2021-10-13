package com.kotori316.scala_lib.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TomlConfigTest {
    @TempDir
    Path parent;
    private IntKey root;
    private ConfigKey<String> root2;
    private BooleanKey a;
    private BooleanKey b;
    private IntKey c;
    private BooleanKey d;
    private BooleanKey e;
    private IntKey f;
    private ConfigImpl template;

    @BeforeEach
    void setUp() {
        this.template = new ConfigImpl();
        ConfigTemplate.ChildTemplate template1 = new ConfigChildImpl(template, "sub1");
        ConfigTemplate.ChildTemplate template2 = new ConfigChildImpl(template, "sub2");

        root = ConfigKey.createInt(template, "root", 45);
        root2 = ConfigKey.create(template, "str", "string", ED.edString());
        ConfigKey.createSubCategory(template1);
        ConfigKey.createSubCategory(template2);

        a = ConfigKey.createBoolean(template1, "a", false);
        b = ConfigKey.createBoolean(template1, "b", true);
        c = ConfigKey.createInt(template1, "c", 5);
        d = ConfigKey.createBoolean(template2, "d", true);
        e = ConfigKey.createBoolean(template2, "e", false);
        f = ConfigKey.createInt(template2, "f", 200);
    }

    @Test
    void testWrite() throws IOException {
        Path tempFile = parent.resolve("w1.toml");
        TomlConfigFile file = new TomlConfigFile(tempFile);
        template.write(file);

        List<String> strings = Files.readAllLines(tempFile);
        assertAll(
            () -> assertTrue(strings.stream().anyMatch(line -> line.contains("root"))),
            () -> assertTrue(strings.stream().anyMatch(line -> line.contains("sub1"))),
            () -> assertTrue(strings.stream().anyMatch(line -> line.contains("sub2")))
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCyclic() {
        Path tempFile = parent.resolve("w2.toml");
        root.set(-56);
        root2.set("new");
        a.set(true);
        c.set(63);
        d.set(false);
        f.set(150);
        TomlConfigFile file = new TomlConfigFile(tempFile);
        template.write(file);

        template.settings().foreach(t -> {
            template.set((ConfigKey<Object>) t._1, t._1.defaultValue());
            return null;
        });

        template.read(file);
        assertAll(
            () -> assertEquals(-56, root.get()),
            () -> assertEquals("new", root2.get()),
            () -> assertTrue(a.get()),
            () -> assertTrue(b.get()),
            () -> assertEquals(63, c.get()),
            () -> assertFalse(d.get()),
            () -> assertFalse(e.get()),
            () -> assertEquals(150, f.get())
        );
    }
}
