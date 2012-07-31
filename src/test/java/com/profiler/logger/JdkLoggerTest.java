package com.profiler.logger;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JdkLoggerTest {
    @Test
    public void test() {
        Logger logger = Logger.getLogger(this.getClass().getName());

        logger.info("tset");
        // format은 역시 안되네.
        logger.log(Level.INFO, "Test %s", "sdfsdf");

        logger.log(Level.INFO, "Test ", new Exception());

        logger.logp(Level.INFO, JdkLoggerTest.class.getName(), "test()", "tsdd");
    }
}
