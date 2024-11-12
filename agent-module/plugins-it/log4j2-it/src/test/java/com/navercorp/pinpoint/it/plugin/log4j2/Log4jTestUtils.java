package com.navercorp.pinpoint.it.plugin.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
                .replace(" ", "").replace("log4j-core-", "").split(":");
        return threadInfo[0];
    }

    public static String getLoggerJarLocation(Object object) {
        return object.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }

    @Test
    void name() {
        Logger test = LogManager.getLogger("test");
        System.out.printf("Log4j jar location:%s", getTestVersion(test));
    }
}
