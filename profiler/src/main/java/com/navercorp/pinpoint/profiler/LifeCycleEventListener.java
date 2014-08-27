package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;


/**
 * @author emeroad
 */
public class LifeCycleEventListener {

    private final static PLogger logger = PLoggerFactory.getLogger(LifeCycleEventListener.class.getName());

    private Agent agent;
    private boolean started = false;

    public LifeCycleEventListener(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("agent must not be null");
        }
        this.agent = agent;
    }

    public synchronized void start() {
        logger.info("LifeCycleEventListener start");

        if (started) {
            logger.info("already started");
            return;
        }

        agent.start();
        started = true;
    }

    public synchronized void stop() {
        logger.info("LifeCycleEventListener stop");

        if (!started) {
            logger.info("already stopped");
            return;
        }
        started = false;
        agent.stop();
    }
}
