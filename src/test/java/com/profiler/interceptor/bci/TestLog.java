package com.profiler.interceptor.bci;


import java.util.logging.Logger;

public class TestLog {

    private final Logger logger = Logger.getLogger(TestLog.class.getName());

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
