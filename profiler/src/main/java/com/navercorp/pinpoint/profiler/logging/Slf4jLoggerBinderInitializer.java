package com.nhn.pinpoint.profiler.logging;

import com.nhn.pinpoint.bootstrap.logging.PLoggerBinder;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * TestCase용의 쉽게 loggerBinder를 등록삭제할수 있는 api
 *
 * @author emeroad
 */
public class Slf4jLoggerBinderInitializer {

    private static final PLoggerBinder loggerBinder = new Slf4jLoggerBinder();

    public static void beforeClass() {
        PLoggerFactory.initialize(loggerBinder);
    }

    public static void afterClass() {
        PLoggerFactory.unregister(loggerBinder);
    }
}
