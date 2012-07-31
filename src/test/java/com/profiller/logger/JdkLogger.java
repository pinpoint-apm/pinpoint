package com.profiller.logger;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JdkLogger {
    @Test
    public void test() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.info("tset");
        logger.log(Level.INFO, "Test %s", "sdfsdf");

        logger.log(Level.INFO, "Test ", new Exception());
        logger.logp(Level.INFO, JdkLogger.class.getName(), "test()", "tsdd");
    }
}
