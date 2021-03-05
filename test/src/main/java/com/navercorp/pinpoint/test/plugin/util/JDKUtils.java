package com.navercorp.pinpoint.test.plugin.util;

public class JDKUtils {

    private static final boolean JDK8_PLUS = getJDK8Plus();

    private static boolean getJDK8Plus() {
        try {
            Class.forName("java.util.Optional");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isJdk8Plus() {
        return JDK8_PLUS;
    }
}
