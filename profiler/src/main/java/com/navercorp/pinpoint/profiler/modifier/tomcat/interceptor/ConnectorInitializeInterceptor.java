package com.nhn.pinpoint.profiler.modifier.tomcat.interceptor;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

import org.apache.catalina.connector.Connector;

/**
 * @author emeroad
 */
public class ConnectorInitializeInterceptor implements SimpleAroundInterceptor, TraceContextSupport, TargetClassLoader {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    
    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        Connector connector = (Connector) target;
        this.traceContext.getServerMetaDataHolder().addConnector(connector.getProtocol(), connector.getPort());

    }
}
