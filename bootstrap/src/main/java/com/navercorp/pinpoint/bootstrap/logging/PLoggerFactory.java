package com.nhn.pinpoint.bootstrap.logging;

import java.util.logging.Logger;

/**
 * @author emeroad
 */
public final class PLoggerFactory {

    private static PLoggerBinder loggerBinder;

    public static void initialize(PLoggerBinder loggerBinder) {
        if (PLoggerFactory.loggerBinder == null) {
            PLoggerFactory.loggerBinder = loggerBinder;
        } else {
            final Logger logger = Logger.getLogger(PLoggerFactory.class.getName());
            logger.warning("loggerBinder is not null");
        }
    }

    public static void unregister(PLoggerBinder loggerBinder) {
        // 등록한 놈만  제거 가능하도록 제한
        // testcase 작성시 가능한 logger를 등록했다가 삭제하는 로직은 beforeClass, afterClass에 넣어야 한다.
        if (loggerBinder == PLoggerFactory.loggerBinder) {
            PLoggerFactory.loggerBinder = null;
        }
    }

    public static PLogger getLogger(String name) {
        if (loggerBinder == null) {
            // 바인딩 되지 않은 상태에서 getLogger를 호출시 null ex가 발생하므로 dummy logger를 리턴하도록 함.
            return DummyPLogger.INSTANCE;
        }
        return loggerBinder.getLogger(name);
    }

    public static PLogger getLogger(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("class must not be null");
        }
        return getLogger(clazz.getName());
    }
}
