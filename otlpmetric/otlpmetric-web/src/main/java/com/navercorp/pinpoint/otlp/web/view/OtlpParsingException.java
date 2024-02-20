package com.navercorp.pinpoint.otlp.web.view;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OtlpParsingException extends RuntimeException {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public OtlpParsingException(String message) {
        super(message);

        if (logger.isEnabled(Level.WARN)) {
            logger.warn(message);
        }
    }
}