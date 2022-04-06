package com.navercorp.pinpoint.common.profiler.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThrottledLoggerTest {

    @Test
    public void LoggerTest() {
        final Logger logger = LogManager.getLogger(this.getClass());

        final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 1);

        throttledLogger.info("test logger info: logger");
        throttledLogger.debug("test logger debug: logger");
        throttledLogger.warn("test logger warn: logger");
    }

    @Test
    public void throttledLoggerTest() {
        final Logger logger = LogManager.getLogger(this.getClass());
        final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 2);

        //only odd number logs should be shown
        throttledLogger.debug("test tlogger debug1: throttled");
        throttledLogger.debug("test tLogger debug2: throttled ERROR");
        throttledLogger.debug("test tlogger debug3: throttled");
        throttledLogger.debug("test tLogger debug4: throttled ERROR");
    }

    @Test
    public void throttledLoggerZeroRatioTest() {
        final Logger logger = LogManager.getLogger(this.getClass());
        final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 0);

        throttledLogger.info("test tLogger info: zero ratio");
        throttledLogger.debug("test tLogger debug: zero ratio");
        throttledLogger.warn("test tLogger warn: zero ratio");
    }

    @Test
    public void throttledLoggerNegativeRatioTest() {
        final Logger logger = LogManager.getLogger(this.getClass());
        final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, -1);

        throttledLogger.info("test tLogger info: negative ratio");
        throttledLogger.debug("test tLogger debug: negative ratio");
        throttledLogger.warn("test tLogger warn: negative ratio");
    }
}
