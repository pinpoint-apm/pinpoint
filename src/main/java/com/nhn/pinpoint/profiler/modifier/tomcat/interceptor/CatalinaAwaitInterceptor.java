package com.nhn.pinpoint.profiler.modifier.tomcat.interceptor;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;

import com.nhn.pinpoint.profiler.Agent;

/**
 * @author emeroad
 */
public class CatalinaAwaitInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private Agent agent;

    public CatalinaAwaitInterceptor(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("agent must not be null");
        }
        this.agent = agent;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
		agent.started();
		// agent.sendStartupInfo();
    }

    @Override
    public void after(Object target, Object[] args, Object result) {

    }
}
