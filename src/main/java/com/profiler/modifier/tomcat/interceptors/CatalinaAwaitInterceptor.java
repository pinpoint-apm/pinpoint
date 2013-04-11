package com.profiler.modifier.tomcat.interceptors;

import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.Assert;

/**
 *
 */
public class CatalinaAwaitInterceptor implements StaticBeforeInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private Agent agent;

    public CatalinaAwaitInterceptor(Agent agent) {
        Assert.notNull(agent, "agent must not be null");
        this.agent = agent;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
        }
		agent.started();
		// agent.sendStartupInfo();
    }
}
