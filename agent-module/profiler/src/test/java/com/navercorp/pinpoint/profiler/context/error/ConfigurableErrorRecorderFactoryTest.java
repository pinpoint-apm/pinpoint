package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.common.trace.ErrorCategory;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurableErrorRecorderFactoryTest {
    @Test
    void test() {
        assertEquals(EnumSet.allOf(ErrorCategory.class), ConfigurableErrorRecorderFactory.getEnabledTypes(null, null));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes("", null));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes(" ", null));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes("invalid", null));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN, ErrorCategory.EXCEPTION), ConfigurableErrorRecorderFactory.getEnabledTypes(" exception ", null));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN, ErrorCategory.EXCEPTION, ErrorCategory.HTTP_STATUS), ConfigurableErrorRecorderFactory.getEnabledTypes(" exception , http-status", null));
    }

    @Test
    void excludeTest() {
        assertEquals(EnumSet.allOf(ErrorCategory.class), ConfigurableErrorRecorderFactory.getEnabledTypes(null, ""));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes("", ""));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes("exception", "exception"));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN, ErrorCategory.EXCEPTION), ConfigurableErrorRecorderFactory.getEnabledTypes("exception, http-status", "http-status"));
        assertEquals(EnumSet.complementOf(EnumSet.of(ErrorCategory.EXCEPTION)), ConfigurableErrorRecorderFactory.getEnabledTypes(null, "exception"));
    }
}