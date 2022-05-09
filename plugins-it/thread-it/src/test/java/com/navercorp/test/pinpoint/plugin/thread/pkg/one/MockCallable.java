package com.navercorp.test.pinpoint.plugin.thread.pkg.one;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class MockCallable implements Callable<String> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private String name;

    public MockCallable(String name) {
        this.name = name;
    }

    @Override
    public String call() {
        logger.info("callable-----------------");
        return this.name;
    }
};
