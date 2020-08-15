package com.kotori316.scala_lib.test;

import net.minecraftforge.common.util.LazyOptional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionalTest1 {
    @Test
    void test1() {
        LazyOptional<String> lazyOptional = LazyOptional.of(() -> "TNT");
        assertEquals("TNT", lazyOptional.orElse("NULL"));

        lazyOptional.invalidate();
        assertEquals("NULL", lazyOptional.orElse("NULL"));
    }
}
