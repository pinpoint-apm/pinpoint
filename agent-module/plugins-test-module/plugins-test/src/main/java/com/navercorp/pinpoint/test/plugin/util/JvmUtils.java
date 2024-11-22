package com.navercorp.pinpoint.test.plugin.util;

public class JvmUtils {

    static final String JVM_VERSION_KEY = "java.specification.version";
    private static final float JAVA9_CLASS_VERSION = 9.0f;

    public static boolean isJava9() {
        String javaVersion = System.getProperty(JVM_VERSION_KEY);
        return isJava9(javaVersion);
    }

    static boolean isJava9(String javaVersion) {
        float version = Float.parseFloat(javaVersion);
        return version >= JAVA9_CLASS_VERSION;
    }
}
