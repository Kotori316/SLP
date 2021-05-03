package com.kotori316.scala_lib.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleConfigFileTest {
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile(null, "txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void writeFile() throws IOException {
        ConfigImpl template = new ConfigImpl();
        ConfigFile file = new ConfigFile.SimpleTextConfig(tempFile);

        ConfigKey.createBoolean(template, "a", true);
        ConfigKey.createBoolean(template, "b", false);
        ConfigKey.createInt(template, "c", 45);
        ConfigKey.createInt(template, "d", 80);
        ConfigKey.create(template, "e", "enabled", ED.edString());

        template.write(file);
        List<String> expect = Arrays.asList(
            "a=true",
            "b=false",
            "c=45",
            "d=80",
            "e=enabled"
        );
        assertLinesMatch(expect, Files.readAllLines(tempFile));
    }

    @Test
    void writeFile2() throws IOException {
        ConfigImpl template = new ConfigImpl();
        ConfigTemplate.ChildTemplate template1 = new ConfigChildImpl(template, "sub1");
        ConfigTemplate.ChildTemplate template2 = new ConfigChildImpl(template, "sub2");

        ConfigKey.createInt(template, "root", 45);
        ConfigKey.createSubCategory(template1);
        ConfigKey.createSubCategory(template2);

        ConfigKey.createBoolean(template1, "a", false);
        ConfigKey.createBoolean(template1, "b", true);
        ConfigKey.createInt(template1, "c", 5);
        ConfigKey.createBoolean(template2, "d", true);
        ConfigKey.createBoolean(template2, "e", false);
        ConfigKey.createInt(template2, "f", 200);

        ConfigFile file = new ConfigFile.SimpleTextConfig(tempFile);
        template.write(file);
        List<String> expect = Arrays.asList(
            "root=45",
            "sub1.a=false",
            "sub1.b=true",
            "sub1.c=5",
            "sub2.d=true",
            "sub2.e=false",
            "sub2.f=200"
        );
        assertLinesMatch(expect, Files.readAllLines(tempFile));
    }

    @Test
    void readFile1() throws IOException {
        ConfigImpl template = new ConfigImpl();
        ConfigFile file = new ConfigFile.SimpleTextConfig(tempFile);

        BooleanKey a = ConfigKey.createBoolean(template, "a", true);
        BooleanKey b = ConfigKey.createBoolean(template, "b", false);
        IntKey c = ConfigKey.createInt(template, "c", 45);
        IntKey d = ConfigKey.createInt(template, "d", 80);
        ConfigKey<String> e = ConfigKey.create(template, "e", "enabled", ED.edString());

        Files.write(tempFile, Arrays.asList(
            "a=false",
            "b=false",
            "c=10",
            "d=562",
            "e=disabled"
        ));
        template.read(file);
        assertAll(
            () -> assertFalse(a.get()),
            () -> assertFalse(b.get()),
            () -> assertEquals(10, c.get()),
            () -> assertEquals(562, d.get()),
            () -> assertEquals("disabled", e.get())
        );
    }

    @Test
    void readFile2() throws IOException {
        ConfigImpl template = new ConfigImpl();
        ConfigTemplate.ChildTemplate template1 = new ConfigChildImpl(template, "sub1");
        ConfigTemplate.ChildTemplate template2 = new ConfigChildImpl(template, "sub2");

        IntKey root = ConfigKey.createInt(template, "root", 45);
        ConfigKey.createSubCategory(template1);
        ConfigKey.createSubCategory(template2);

        BooleanKey a = ConfigKey.createBoolean(template1, "a", true);
        BooleanKey b = ConfigKey.createBoolean(template1, "b", true);
        IntKey c = ConfigKey.createInt(template1, "c", 21);
        BooleanKey d = ConfigKey.createBoolean(template2, "d", true);
        BooleanKey e = ConfigKey.createBoolean(template2, "e", false);
        IntKey f = ConfigKey.createInt(template2, "f", 200);

        ConfigFile file = new ConfigFile.SimpleTextConfig(tempFile);
        Files.write(tempFile, Arrays.asList(
            "# Comment",
            "root=63",
            "sub1.a=false",
            "sub1.b=true",
            "sub1.c=5 # The last of sub1",
            "sub2.d=false",
            "sub2.e=false",
            "sub2.f=-63 ; The last of sub2",
            "; EOF"
        ));
        template.read(file);
        assertAll(
            () -> assertEquals(63, root.get()),
            () -> assertFalse(a.get()),
            () -> assertTrue(b.get()),
            () -> assertEquals(5, c.get()),
            () -> assertFalse(d.get()),
            () -> assertFalse(e.get()),
            () -> assertEquals(-63, f.get())
        );
    }
}
