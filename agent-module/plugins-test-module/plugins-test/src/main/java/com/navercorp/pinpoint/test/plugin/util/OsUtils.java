package com.navercorp.pinpoint.test.plugin.util;

public class OsUtils {
    static final String SYSTEM_OS_NAME = "os.name";
    private static final String WINDOW = "Window";

    public static boolean isWindows() {
        String osName = System.getProperty(SYSTEM_OS_NAME, "");
        return osName.contains(WINDOW);
    }

    public static String getClassPathSeparator() {
        if (OsUtils.isWindows()) {
            return ";";
        }
        return ":";
    }
}
