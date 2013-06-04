package com.nhn.pinpoint.profiler.logging;

/**
 * TestCase용의 쉽게 loggerBinder를 등록삭제할수 있는 api
 */
public class Slf4jLoggerBinderInitializer {

    private static final LoggerBinder loggerBinder = new Slf4jLoggerBinder();

    public static void beforeClass() {
        LoggerFactory.initialize(loggerBinder);
    }

    public static void afterClass() {
        LoggerFactory.unregister(loggerBinder);
    }
}
