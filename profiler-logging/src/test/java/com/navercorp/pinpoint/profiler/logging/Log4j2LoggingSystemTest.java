package com.navercorp.pinpoint.profiler.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.junit.Assert.*;

public class Log4j2LoggingSystemTest {

    @Test
    public void start() {
        URL resource = this.getClass().getClassLoader().getResource("");
        LoggingSystem loggingSystem = new Log4j2LoggingSystem(resource.getPath());
        loggingSystem.start();

        Logger test = LoggerFactory.getLogger("test");
        test.debug("test");
        
        loggingSystem.stop();
    }

    @Test
    public void stop() {
    }
}