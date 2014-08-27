package com.nhn.pinpoint.profiler.interceptor.bci;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TestLog {

    private final Logger logger = LoggerFactory.getLogger(TestLog.class.getName());

    public String constructor;

    public TestLog() {
    }

    public TestLog(String constructor) {
        this.constructor = constructor;
    }

    public String test;
    public void test() {
        logger.info("test");
    }

    public void test(String method) {
        this.test = method;
        logger.info("test:" + method);
    }
}
