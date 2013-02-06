package com.profiler.modifier.tomcat.interceptors;

import java.util.logging.Logger;

import com.profiler.Agent;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.Assert;

/**
 *
 */
public class CatalinaAwaitInterceptor implements StaticBeforeInterceptor {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

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
        agent.sendStartupInfo();
    }
}
