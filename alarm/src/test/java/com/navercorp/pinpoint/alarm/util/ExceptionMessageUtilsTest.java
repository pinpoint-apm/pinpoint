package com.navercorp.pinpoint.alarm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMessageUtilsTest {

    @Test
    void rootCauseMessageReturnsDeepestCauseMessage() {
        RuntimeException failure = new IllegalStateException(
                "outer", new IllegalArgumentException("root failure"));

        assertEquals("root failure", ExceptionMessageUtils.rootCauseMessage(failure));
    }

    @Test
    void rootCauseMessageFallsBackToClassNameForNullMessage() {
        RuntimeException failure = new IllegalStateException((String) null);

        assertEquals("IllegalStateException", ExceptionMessageUtils.rootCauseMessage(failure));
    }

    @Test
    void rootCauseMessageFallsBackToClassNameForBlankMessage() {
        RuntimeException failure = new IllegalStateException("   ");

        assertEquals("IllegalStateException", ExceptionMessageUtils.rootCauseMessage(failure));
    }
}
