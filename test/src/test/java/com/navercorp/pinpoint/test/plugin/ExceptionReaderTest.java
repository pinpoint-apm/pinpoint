package com.navercorp.pinpoint.test.plugin;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExceptionReaderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        Assert.assertEquals(getExceptionClass(readException), RuntimeException.class.getName());
        Throwable cause = readException.getCause();
        Assert.assertEquals(getExceptionClass(cause), Exception.class.getName());

    }

    private String getExceptionClass(Throwable th) {
        if (th instanceof PinpointPluginTestException) {
            return ((PinpointPluginTestException) th).getExceptionClass();
        }
        throw new RuntimeException("unexpected class class:" + th.getClass());
    }
}