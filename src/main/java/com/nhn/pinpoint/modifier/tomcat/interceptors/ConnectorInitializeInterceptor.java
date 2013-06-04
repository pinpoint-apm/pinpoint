package com.nhn.pinpoint.modifier.tomcat.interceptors;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import org.apache.catalina.connector.Connector;

import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

/**
 *
 */
public class ConnectorInitializeInterceptor implements SimpleAroundInterceptor {

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
    public void before(Object target, Object[] args) {

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
