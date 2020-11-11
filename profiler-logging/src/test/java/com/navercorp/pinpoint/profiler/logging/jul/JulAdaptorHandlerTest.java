package com.navercorp.pinpoint.profiler.logging.jul;

import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.mockito.Mockito.mock;

public class JulAdaptorHandlerTest {

    @Test
    public void format() {
        LoggerContext loggerContext = mock(LoggerContext.class);
        JulAdaptorHandler handler = new JulAdaptorHandler(loggerContext);
        LogRecord record = new LogRecord(Level.INFO, "Service loader found Provider{policy=round_robin, priority=5, available=true}");
        handler.format(record);
    }
}
