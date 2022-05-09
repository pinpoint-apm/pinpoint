package com.navercorp.test.pinpoint.plugin.thread.pkg.one;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MockRunnable implements Runnable {
    private final Logger logger = LogManager.getLogger(this.getClass());
    public MockRunnable() {
    }

    @Override
    public void run() {
        logger.info("runnable-----------------");
    }
};
