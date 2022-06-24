package com.navercorp.pinpoint.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BootLogLevelTest {

    private static final BootLogLevel TRACE = BootLogLevel.TRACE;
    private static final BootLogLevel DEBUG = BootLogLevel.DEBUG;
    private static final BootLogLevel INFO = BootLogLevel.INFO;
    private static final BootLogLevel WARN = BootLogLevel.WARN;
    private static final BootLogLevel ERROR = BootLogLevel.ERROR;

    @Test
    public void of() {
        assertEquals(TRACE, BootLogLevel.of(TRACE.name()), "trace");
        assertEquals(DEBUG, BootLogLevel.of(DEBUG.name()), "debug");
        assertEquals(INFO, BootLogLevel.of(INFO.name()), "info");
        assertEquals(WARN, BootLogLevel.of(WARN.name()), "warn");
        assertEquals(ERROR, BootLogLevel.of(ERROR.name()), "error");
        assertNull(BootLogLevel.of("notExist"), "invalid");
    }

    @Test
    public void logTrace() {
        assertTrue(TRACE.logTrace(), "trace");
        assertFalse(DEBUG.logTrace(), "debug");
        assertFalse(INFO.logTrace(), "info");
        assertFalse(WARN.logTrace(), "warn");
        assertFalse(ERROR.logTrace(), "error");
    }

    @Test
    public void logDebug() {
        assertTrue(TRACE.logDebug(), "trace");
        assertTrue(DEBUG.logDebug(), "debug");
        assertFalse(INFO.logDebug(), "info");
        assertFalse(WARN.logDebug(), "warn");
        assertFalse(ERROR.logDebug(), "error");
    }

    @Test
    public void logInfo() {
        assertTrue(TRACE.logInfo(), "trace");
        assertTrue(DEBUG.logInfo(), "debug");
        assertTrue(INFO.logInfo(), "info");
        assertFalse(WARN.logInfo(), "warn");
        assertFalse(ERROR.logInfo(), "error");
    }

    @Test
    public void logWarn() {
        assertTrue(TRACE.logWarn(), "trace");
        assertTrue(DEBUG.logWarn(), "debug");
        assertTrue(INFO.logWarn(), "info");
        assertTrue(WARN.logWarn(), "warn");
        assertFalse(ERROR.logWarn(), "error");
    }

    @Test
    public void logError() {
        assertTrue(TRACE.logError(), "trace");
        assertTrue(DEBUG.logError(), "debug");
        assertTrue(INFO.logError(), "info");
        assertTrue(WARN.logError(), "warn");
        assertTrue(ERROR.logError(), "error");
    }
}