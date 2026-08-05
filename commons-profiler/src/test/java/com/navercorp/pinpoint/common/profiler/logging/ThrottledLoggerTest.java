package com.navercorp.pinpoint.common.profiler.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Test
    public void countBasedEmission() {
        final Logger logger = mock(Logger.class);
        when(logger.isEnabled(Level.INFO)).thenReturn(true);

        final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 3);
        for (int i = 0; i < 5; i++) {
            throttledLogger.info("count based");
        }

        // 5 calls with ratio 3 -> call 1 and call 4 are emitted
        verify(logger, times(2)).log(Level.INFO, "count based");
        assertThat(throttledLogger.getCounter()).isEqualTo(5);
    }

    @Test
    public void timeBasedEmission() {
        final Logger logger = mock(Logger.class);
        when(logger.isEnabled(Level.INFO)).thenReturn(true);

        // interval is long enough that only the first call within this test is emitted
        final ThrottledLogger throttledLogger = ThrottledLogger.getIntervalLogger(logger, Duration.ofSeconds(10));
        throttledLogger.info("time based");
        throttledLogger.info("time based");
        throttledLogger.info("time based");

        verify(logger, times(1)).log(Level.INFO, "time based");
        assertThat(throttledLogger.getCounter()).isEqualTo(3);
    }

    @Test
    public void disabledLevelDoesNotCount() {
        final Logger logger = mock(Logger.class);
        when(logger.isEnabled(Level.INFO)).thenReturn(false);

        final ThrottledLogger throttledLogger = ThrottledLogger.getIntervalLogger(logger, Duration.ofSeconds(3));
        throttledLogger.info("disabled");

        verify(logger, never()).log(eq(Level.INFO), anyString());
        assertThat(throttledLogger.getCounter()).isEqualTo(0);
    }
}
