package com.profiler.modifier.tomcat.interceptors;

import java.util.Arrays;
import java.util.logging.Logger;

import com.profiler.logging.LoggingUtils;
import org.apache.catalina.connector.Connector;

import com.profiler.Agent;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.Assert;
import com.profiler.util.StringUtils;

/**
 *
 */
public class ConnectorInitializeInterceptor implements StaticAfterInterceptor {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private Agent agent;

    public ConnectorInitializeInterceptor(Agent agent) {
        Assert.notNull(agent, "agent must not be null");
        this.agent = agent;
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.fine("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        Connector connector = (Connector) target;
        agent.getServerInfo().addConnector(connector.getProtocol(), connector.getPort());

    }
}
