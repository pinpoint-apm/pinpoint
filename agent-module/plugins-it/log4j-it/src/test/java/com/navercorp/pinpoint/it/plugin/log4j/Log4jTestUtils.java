package com.navercorp.pinpoint.it.plugin.log4j;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;

public final class Log4jTestUtils {

    private Log4jTestUtils() {
    }

    public static void checkVersion(Logger logger, Object test) {
        final String location = Log4jTestUtils.getLoggerJarLocation(logger);
        System.out.println("Log4j2 jar location:" + location);
        final String testVersion = Log4jTestUtils.getTestVersion(test);
        Assertions.assertTrue(location.contains("/" + testVersion + "/"), "test version is not " + testVersion);
    }

    public static String getTestVersion(Object test) {
        final String[] threadInfo = Thread.currentThread().getName()
                .replace(test.getClass().getName(), "")
                .replace(" Thread", "")
                .replace(" ", "").replace("log4j-", "").split(":");
        return threadInfo[0];
    }


    public static String getLoggerJarLocation(Object object) {
        return object.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }
}
