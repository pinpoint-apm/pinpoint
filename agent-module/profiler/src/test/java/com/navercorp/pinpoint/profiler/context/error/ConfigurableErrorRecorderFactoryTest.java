package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.common.trace.ErrorCategory;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurableErrorRecorderFactoryTest {
    @Test
    void test() {
        assertEquals(EnumSet.allOf(ErrorCategory.class), ConfigurableErrorRecorderFactory.getEnabledTypes(null));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes(""));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN), ConfigurableErrorRecorderFactory.getEnabledTypes(" "));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN, ErrorCategory.EXCEPTION), ConfigurableErrorRecorderFactory.getEnabledTypes(" exception "));
        assertEquals(EnumSet.of(ErrorCategory.UNKNOWN, ErrorCategory.EXCEPTION, ErrorCategory.HTTP_STATUS), ConfigurableErrorRecorderFactory.getEnabledTypes(" exception , http-status"));
    }
}