package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;


/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class LifeCycleEventListener {

    private final static PLogger logger = PLoggerFactory.getLogger(LifeCycleEventListener.class.getName());

    private final Agent agent;

    public LifeCycleEventListener(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("agent must not be null");
        }
        this.agent = agent;
    }

    public void start() {
        logger.info("LifeCycleEventListener start");
        agent.start();
    }

    public void stop() {
        logger.info("LifeCycleEventListener stop");
        agent.stop();
    }
}
