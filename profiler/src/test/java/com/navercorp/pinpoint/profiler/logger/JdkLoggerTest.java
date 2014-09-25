package com.nhn.pinpoint.profiler.logger;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public class JdkLoggerTest {
    @Test
    public void test() {
        Logger logger = Logger.getLogger(this.getClass().getName());

        logger.info("tset");
        // format은 역시 안되네.
        logger.log(Level.INFO, "Test %s", "sdfsdf");

        logger.log(Level.INFO, "Test ", new Exception());

        logger.logp(Level.INFO, JdkLoggerTest.class.getName(), "test()", "tsdd");

        // logging.properties 에 fine으로 되어 있으므로 출력안되야 됨.
        logger.finest("로그가 나오는지?");

    }
}
