package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.util.Objects;
import java.util.StringJoiner;

public class PathUtils {

    public static final String SEPARATOR = File.separator;

    public static String wrapPath(String first, String... more) {
        Objects.requireNonNull(first, "paths");
        StringJoiner joiner = new StringJoiner(SEPARATOR, SEPARATOR, SEPARATOR);
        joiner.add(first);
        for (String token : more) {
            joiner.add(token);
        }
        return joiner.toString();
    }

    public static String path(String... paths) {
        return String.join(SEPARATOR, paths);
    }
}
