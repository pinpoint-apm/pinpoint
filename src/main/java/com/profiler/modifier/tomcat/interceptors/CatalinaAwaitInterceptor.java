package com.profiler.modifier.tomcat.interceptors;

import com.profiler.interceptor.SimpleBeforeInterceptor;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.Agent;
import com.profiler.util.Assert;

/**
 *
 */
public class CatalinaAwaitInterceptor implements SimpleBeforeInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private Agent agent;

    public CatalinaAwaitInterceptor(Agent agent) {
        Assert.notNull(agent, "agent must not be null");
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
}
