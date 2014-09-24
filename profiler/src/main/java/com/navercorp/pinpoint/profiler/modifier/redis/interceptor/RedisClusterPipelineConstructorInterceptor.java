package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhncorp.redis.cluster.gateway.GatewayServer;

/**
 * RedisCluster pipeline(nBase-ARC client) constructor interceptor
 *   - trace destinationId & endPoint
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

        if (!(target instanceof MapTraceValue)) {
            return;
        }

        // trace destinationId & endPoint
        final Map<String, Object> traceValue = new HashMap<String, Object>();

        // first arg : GatewayServer
        final GatewayServer server = (GatewayServer) args[0];
        traceValue.put("endPoint", server.getAddress().getHost() + ":" + server.getAddress().getPort());

        if (args[0] instanceof MapTraceValue) {
            final Map<String, Object> gatewayServerTraceValue = ((MapTraceValue) args[0]).__getTraceBindValue();
            if (gatewayServerTraceValue != null) {
                traceValue.put("destinationId", gatewayServerTraceValue.get("destinationId"));
            }
        }

        ((MapTraceValue) target).__setTraceBindValue(traceValue);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
