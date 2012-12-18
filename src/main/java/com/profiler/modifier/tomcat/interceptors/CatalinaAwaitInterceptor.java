package com.profiler.modifier.tomcat.interceptors;

import com.profiler.Agent;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.util.Assert;
import com.profiler.util.StringUtils;
import net.spy.memcached.compat.log.LoggerFactory;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class CatalinaAwaitInterceptor implements StaticBeforeInterceptor {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Agent agent;

    public CatalinaAwaitInterceptor(Agent agent) {
        Assert.notNull(agent, "agent must not be null");
        this.agent = agent;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        agent.sendStartupInfo();
    }
}
