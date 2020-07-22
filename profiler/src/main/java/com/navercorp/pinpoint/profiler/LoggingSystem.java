package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.common.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class LoggingSystem {
    private final String configLocation;
//    private org.apache.logging.log4j.core.LoggerContext loggerContext = new org.apache.logging.log4j.core.LoggerContext("pinpoint-agent-context");

    public LoggingSystem(String configLocation) {
        this.configLocation = Assert.requireNonNull(configLocation, "configLocation");
//        this.loggerContext = new org.apache.logging.log4j.core.LoggerContext("pinpoint-agent-context");
    }

    public void start() {
//        // log4j init
//        XmlConfigurationFactory configurationFactory = new XmlConfigurationFactory();
//        File file = new File(configLocation);
//        InputStream stream = newInputStream(file);
//        ConfigurationSource source = new ConfigurationSource(stream, file);
//        Configuration configuration = configurationFactory.getConfiguration(source);
//        loggerContext.start(configuration);
    }

    private InputStream newInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("log4J2.xml config file not found ", e);
        }
    }

    public void stop() {
//        loggerContext.stop();
    }
}