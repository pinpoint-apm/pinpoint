package com.profiler;

import java.util.logging.Logger;

public class LifeCycleEventListener {

    private final static Logger logger = Logger.getLogger(LifeCycleEventListener.class.getName());

    private static boolean started = false;

    public synchronized static void start() {
        if (started) {
            logger.info("already started");
            return;
        }

        Agent.startAgent();
        started = true;
    }

    public synchronized static void stop() {
        if (!started) {
            logger.info("already stopped");
            return;
        }
        started = false;
        Agent.stopAgent();
    }
}
