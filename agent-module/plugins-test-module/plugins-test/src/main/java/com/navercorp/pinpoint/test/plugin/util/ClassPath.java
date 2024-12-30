package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ClassPath {

    private static final String SEPARATOR = File.pathSeparator;

    public static String[] parse(String classPath) {
        Objects.requireNonNull(classPath, "classPath");
        return classPath.split(SEPARATOR);
    }

    public static String join(List<String> libPath) {
        return String.join(SEPARATOR, libPath);
    }

    public static String joinPath(List<Path> paths) {
        StringJoiner joiner = new StringJoiner(SEPARATOR);
        for (Path lib: paths) {
            joiner.add(lib.toString());
        }
        return joiner.toString();
    }
}
