package com.navercorp.pinpoint.profiler.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Log4j2LoggingSystemTest {

    @Test
    public void start() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("");
        Objects.requireNonNull(resource, "resource");
        Path profilePath = Paths.get(resource.toURI());


        try (Log4j2LoggingSystem loggingSystem = new Log4j2LoggingSystem(profilePath)) {
            loggingSystem.start();

            Logger test = LogManager.getLogger("test");
            test.debug("test");

            Path configLocation = loggingSystem.getConfigLocation();
            Assertions.assertEquals(profilePath.resolve("log4j2-test.xml"), configLocation);
        }
    }
}