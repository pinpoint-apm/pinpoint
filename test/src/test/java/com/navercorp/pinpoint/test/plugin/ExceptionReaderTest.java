package com.navercorp.pinpoint.test.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExceptionReaderTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() {
        Exception inner = new Exception("inner-1");
        RuntimeException runtimeException = new RuntimeException("exception-1", inner);

        ExceptionWriter writer = new ExceptionWriter();
        String exMessage = writer.write("test", runtimeException);
        logger.debug("writer:{}", exMessage);

        String[] parsedMessage = exMessage.split(PinpointPluginTestStatement.JUNIT_OUTPUT_DELIMITER_REGEXP);
        List<String> error = PinpointPluginTestStatement.slice(parsedMessage);

        ExceptionReader reader = new ExceptionReader();

        Exception readException = reader.read(parsedMessage[1], parsedMessage[2], error);
        logger.debug("reader", readException);

        Assertions.assertEquals(getExceptionClass(readException), RuntimeException.class.getName());
        Throwable cause = readException.getCause();
        Assertions.assertEquals(getExceptionClass(cause), Exception.class.getName());

    }

    private String getExceptionClass(Throwable th) {
        if (th instanceof PinpointPluginTestException) {
            return ((PinpointPluginTestException) th).getExceptionClass();
        }
        throw new RuntimeException("unexpected class class:" + th.getClass());
    }
}