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
}
