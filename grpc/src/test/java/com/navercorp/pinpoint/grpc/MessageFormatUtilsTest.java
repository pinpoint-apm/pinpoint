package com.navercorp.pinpoint.grpc;

import com.navercorp.pinpoint.grpc.trace.PSpan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessageFormatUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void debugLog() {
        PSpan.Builder builder = PSpan.newBuilder();
        builder.setApiId(1);
        builder.setStartTime(2);
        PSpan span = builder.build();

        MessageFormatUtils.LogMessage logMessage = MessageFormatUtils.debugLog(span);
        logger.debug("logMessage:{}", logMessage);
    }

    @Test
    void debugLog_null() {

        MessageFormatUtils.LogMessage logMessage = MessageFormatUtils.debugLog(null);
        logger.debug("logMessage:{}", logMessage);
    }

    @Test
    void simpleClasName() {
        PSpan.Builder builder = PSpan.newBuilder();
        builder.setApiId(1);
        builder.setStartTime(2);
        PSpan span = builder.build();

        String name = MessageFormatUtils.getSimpleClasName(span);
        logger.debug("message:{}", name);
    }

    @Test
    void simpleClasName_null() {
        String name = MessageFormatUtils.getSimpleClasName(null);
        Assertions.assertEquals("null", name);
    }
}