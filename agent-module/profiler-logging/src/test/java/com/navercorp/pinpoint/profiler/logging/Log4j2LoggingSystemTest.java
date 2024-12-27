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
        final Path log4j2Xml = profilePath.resolve("log4j2-test.xml");

        try (Log4j2LoggingSystem loggingSystem = Log4j2LoggingSystem.searchPath(profilePath)) {
            loggingSystem.start();

            Logger test = LogManager.getLogger("test");
            test.debug("test");

            String configLocation = loggingSystem.getConfigLocation();
            Assertions.assertEquals(log4j2Xml, Paths.get(configLocation));
        }
    }
}
