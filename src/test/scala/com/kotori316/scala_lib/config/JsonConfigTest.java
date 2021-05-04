package com.kotori316.scala_lib.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import scala.jdk.javaapi.CollectionConverters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonConfigTest {
    private static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create();

    @Test
    void testAsMap() {
        JsonObject object = new JsonObject();
        object.addProperty("a", 1);
        object.addProperty("b", true);
        object.addProperty("c", "string");
        {
            JsonObject sub = new JsonObject();
            sub.addProperty("d", -63);
            sub.addProperty("e", 365);
            sub.addProperty("f", "dirt");
            object.add("sub", sub);
        }

        Map<String, JsonElement> map = CollectionConverters.asJava(JsonConfigFile.asScalaMap(object, ""));
        Map<String, JsonElement> expect = Stream.of(
            Pair.of("a", new JsonPrimitive(1)),
            Pair.of("b", new JsonPrimitive(true)),
            Pair.of("c", new JsonPrimitive("string")),
            Pair.of("sub.d", new JsonPrimitive(-63)),
            Pair.of("sub.e", new JsonPrimitive(365)),
            Pair.of("sub.f", new JsonPrimitive("dirt"))
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        assertEquals(expect, map);
    }

    @Test
    void testWrite1() {
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

        StringWriter writer = new StringWriter();
        JsonConfigFile file = JsonConfigFile.apply(() -> new StringReader(writer.toString()), () -> {
            writer.getBuffer().setLength(0);
            return writer;
        });
        template.write(file);
        String result = writer.toString();
        JsonObject object = GSON.fromJson(result, JsonObject.class);
        assertAll(
            () -> assertEquals(45, object.get("root").getAsInt()),
            () -> assertFalse(object.getAsJsonObject("sub1").getAsJsonPrimitive("a").getAsBoolean()),
            () -> assertTrue(object.getAsJsonObject("sub1").getAsJsonPrimitive("b").getAsBoolean()),
            () -> assertEquals(5, object.getAsJsonObject("sub1").getAsJsonPrimitive("c").getAsInt()),
            () -> assertTrue(object.getAsJsonObject("sub2").getAsJsonPrimitive("d").getAsBoolean()),
            () -> assertFalse(object.getAsJsonObject("sub2").getAsJsonPrimitive("e").getAsBoolean()),
            () -> assertEquals(200, object.getAsJsonObject("sub2").getAsJsonPrimitive("f").getAsInt())
        );
    }

}
