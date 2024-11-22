package com.navercorp.pinpoint.test.plugin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.StringJoiner;

class PathUtilsTest {

    @Test
    void wrap() {
        String wrap = PathUtils.wrap("a", "b");
        String expected = join("a", "b");
        Assertions.assertEquals(expected, wrap);
    }

    @Test
    void wrap_1() {
        String wrap = PathUtils.wrap("a");
        String expected = join("a");
        Assertions.assertEquals(expected, wrap);
    }


    private String join(String... paths) {
        StringJoiner joiner = new StringJoiner(File.separator, File.separator, File.separator);
        for (String path : paths) {
            joiner.add(path);
        }
        return joiner.toString();
    }
}