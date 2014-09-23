package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhncorp.redis.cluster.gateway.GatewayServer;

/**
 * nBase-ARC client constructor interceptor
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!(target instanceof ObjectTraceValue)) {
            return;
        }

        // trace host & port
        final ObjectTraceValue traceValue = (ObjectTraceValue) target;
        final Map<String, Object> map = new HashMap<String, Object>();

        // first arg : GatewayServer
        final GatewayServer server = (GatewayServer) args[0];
        map.put("host", server.getAddress().getHost());
        map.put("port", server.getAddress().getPort());
        traceValue.__setTraceObject(map);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
