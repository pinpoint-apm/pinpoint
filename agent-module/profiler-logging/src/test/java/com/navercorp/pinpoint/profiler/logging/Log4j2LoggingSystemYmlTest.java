package com.navercorp.pinpoint.profiler.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Log4j2LoggingSystemYmlTest {

    @Test
    public void start() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("");
        Objects.requireNonNull(resource, "resource");
        Path profilePath = Paths.get(resource.toURI());
        String log4jFile = profilePath.resolve("log4j2-test.yml").toString();
        System.setProperty("log4j2.component.properties", "YamlConfigurationFactory");


        try (Log4j2LoggingSystem loggingSystem = new Log4j2LoggingSystem(profilePath)) {
            loggingSystem.start();

            Logger test = LogManager.getLogger("test");
            test.debug("test");

            String configLocation = loggingSystem.getConfigLocation();
//            Assertions.assertEquals(log4jFile, configLocation);
        }
    }
}