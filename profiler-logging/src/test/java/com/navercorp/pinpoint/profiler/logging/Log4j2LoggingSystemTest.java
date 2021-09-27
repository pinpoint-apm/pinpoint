package com.navercorp.pinpoint.profiler.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Log4j2LoggingSystemTest {

    @Test
    public void start() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("");
        Path profilePath = Paths.get(resource.toURI());
        LoggingSystem loggingSystem = new Log4j2LoggingSystem(profilePath);
        loggingSystem.start();

        Logger test = LoggerFactory.getLogger("test");
        test.debug("test");
        
        loggingSystem.stop();
    }

    @Test
    public void stop() {
    }
}