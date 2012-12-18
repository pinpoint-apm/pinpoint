package com.profiler.modifier.tomcat.interceptors;

import com.profiler.Agent;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.Assert;
import com.profiler.util.StringUtils;
import org.apache.catalina.connector.Connector;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ConnectorInitializeInterceptor implements StaticAfterInterceptor {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Agent agent;

    public ConnectorInitializeInterceptor(Agent agent) {
        Assert.notNull(agent, "agent must not be null");
        this.agent = agent;
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        Connector connector = (Connector) target;
        agent.getServerInfo().addConnector(connector.getProtocol(), connector.getPort());

    }
}
