package com.profiler.modifier.tomcat.interceptors;

import com.profiler.Agent;
import com.profiler.interceptor.SimpleAfterInterceptor;
import org.apache.catalina.connector.Connector;

import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

/**
 *
 */
public class ConnectorInitializeInterceptor implements SimpleAfterInterceptor {

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
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
        Connector connector = (Connector) target;
        agent.addConnector(connector.getProtocol(), connector.getPort());

    }
}
