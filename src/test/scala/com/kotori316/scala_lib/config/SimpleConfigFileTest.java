package com.kotori316.scala_lib.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

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
}
