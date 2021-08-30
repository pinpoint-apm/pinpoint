package com.navercorp.pinpoint.bootstrap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BootLogLevelTest {

    private static final BootLogLevel TRACE = BootLogLevel.TRACE;
    private static final BootLogLevel DEBUG = BootLogLevel.DEBUG;
    private static final BootLogLevel INFO = BootLogLevel.INFO;
    private static final BootLogLevel WARN = BootLogLevel.WARN;
    private static final BootLogLevel ERROR = BootLogLevel.ERROR;

    @Test
    public void of() {
        assertEquals("trace", TRACE, BootLogLevel.of("trace"));
        assertEquals("debug", DEBUG, BootLogLevel.of("debug"));
        assertEquals("info", INFO, BootLogLevel.of("info"));
        assertEquals("warn", WARN, BootLogLevel.of("warn"));
        assertEquals("error", ERROR, BootLogLevel.of("error"));
        assertNull("invalid", BootLogLevel.of("notExist"));
    }

    @Test
    public void logTrace() {
        assertTrue("trace", TRACE.logTrace());
        assertFalse("debug", DEBUG.logTrace());
        assertFalse("info", INFO.logTrace());
        assertFalse("warn", WARN.logTrace());
        assertFalse("error", ERROR.logTrace());
    }

    @Test
    public void logDebug() {
        assertTrue("trace", TRACE.logDebug());
        assertTrue("debug", DEBUG.logDebug());
        assertFalse("info", INFO.logDebug());
        assertFalse("warn", WARN.logDebug());
        assertFalse("error", ERROR.logDebug());
    }

    @Test
    public void logInfo() {
        assertTrue("trace", TRACE.logInfo());
        assertTrue("debug", DEBUG.logInfo());
        assertTrue("info", INFO.logInfo());
        assertFalse("warn", WARN.logInfo());
        assertFalse("error", ERROR.logInfo());
    }

    @Test
    public void logWarn() {
        assertTrue("trace", TRACE.logWarn());
        assertTrue("debug", DEBUG.logWarn());
        assertTrue("info", INFO.logWarn());
        assertTrue("warn", WARN.logWarn());
        assertFalse("error", ERROR.logWarn());
    }

    @Test
    public void logError() {
        assertTrue("trace", TRACE.logError());
        assertTrue("debug", DEBUG.logError());
        assertTrue("info", INFO.logError());
        assertTrue("warn", WARN.logError());
        assertTrue("error", ERROR.logError());
    }
}