package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

// @Experimental
public class ScatterProperties {
    private final Logger logger = LogManager.getLogger(getClass());

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
