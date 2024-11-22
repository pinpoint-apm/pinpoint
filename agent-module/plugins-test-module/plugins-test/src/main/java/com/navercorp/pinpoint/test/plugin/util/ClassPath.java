package com.navercorp.pinpoint.test.plugin.util;

import java.util.Objects;

public class ClassPath {

    private static final String SEPARATOR = OsUtils.getClassPathSeparator();

    public static String[] parse(String classPath) {
        Objects.requireNonNull(classPath, "classPath");
        return classPath.split(SEPARATOR);
    }
}
