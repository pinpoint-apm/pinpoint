package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

// @Experimental
@Configuration
public class ScatterConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public enum ServerSideScan {
        v1,
        v2 // enable saltkey for fuzzyfilter
    }

    @Value("${collector.scatter.serverside-scan:v1}")
    private String serverSideScan;

    public ServerSideScan getServerSideScan() {
        return ServerSideScan.valueOf(serverSideScan);
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> visitor = new AnnotationVisitor<>(Value.class);
        visitor.visit(this, new LoggingEvent(logger));
    }

    @Override
    public String toString() {
        return "ScatterConfiguration{" +
                "serverSideScan='" + serverSideScan + '\'' +
                '}';
    }
}
