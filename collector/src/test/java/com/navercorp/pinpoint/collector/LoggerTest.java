package com.nhn.pinpoint.collector;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class LoggerTest {
    @Test
    public void log() {
        Logger test = LoggerFactory.getLogger(LoggerTest.class);
        test.info("info");
        test.debug("debug");
    }
}
