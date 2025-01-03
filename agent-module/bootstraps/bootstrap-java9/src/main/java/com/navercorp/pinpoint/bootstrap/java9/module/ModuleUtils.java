package com.navercorp.pinpoint.bootstrap.java9.module;

import java.util.Objects;

public final class ModuleUtils {
    private static final int CLASS_BASE_VERSION = 44;
    private static final int JAVA_VERSION = readJavaVersion();

    public static String getPackageName(String fqcn, char packageSeparator) {
        Objects.requireNonNull(fqcn, "fqcn");

        final int lastPackageSeparatorIndex = fqcn.lastIndexOf(packageSeparator);
        if (lastPackageSeparatorIndex == -1) {
            return null;
        }
        return fqcn.substring(0, lastPackageSeparatorIndex);
    }

    public static String toPackageName(String dirFormat) {
        if (dirFormat == null) {
            return null;
        }
        return dirFormat.replace('/', '.');
    }

    public static int getJavaVersion() {
        return JAVA_VERSION;
    }

    private static int readJavaVersion() {
        return (int) Float.parseFloat(System.getProperty("java.class.version")) - CLASS_BASE_VERSION;
    }

    public static boolean jvmVersionUpper(int version) {
        return getJavaVersion() <= version;
    }
}
