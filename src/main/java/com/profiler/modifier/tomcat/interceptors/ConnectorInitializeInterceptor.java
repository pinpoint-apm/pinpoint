package com.profiler.modifier.tomcat.interceptors;

import com.profiler.Agent;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggingUtils;
import org.apache.catalina.connector.Connector;

import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

/**
 *
 */
public class ConnectorInitializeInterceptor implements StaticAfterInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private Agent agent;

    public ConnectorInitializeInterceptor(Agent agent) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        this.agent = agent;
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }
        Connector connector = (Connector) target;
        agent.addConnector(connector.getProtocol(), connector.getPort());

    }
}
