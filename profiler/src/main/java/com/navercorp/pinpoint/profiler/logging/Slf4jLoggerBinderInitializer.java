package com.navercorp.pinpoint.profiler.logging;

import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

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
