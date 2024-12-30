package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class ClassPath {

    private static final String SEPARATOR = File.pathSeparator;

    public static String[] parse(String classPath) {
        Objects.requireNonNull(classPath, "classPath");
        return classPath.split(SEPARATOR);
    }

    public static String join(List<String> libPath) {
        return String.join(SEPARATOR, libPath);
    }
}
