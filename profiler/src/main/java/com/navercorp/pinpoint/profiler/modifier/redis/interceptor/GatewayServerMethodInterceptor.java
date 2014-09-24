package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;

/**
 * GatewayServer(nBase-ARC client) getResource() method interceptor
 *   - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayServerMethodInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!(target instanceof MapTraceValue) || result == null || !(result instanceof MapTraceValue)) {
            return;
        }

        // result - RedisCluster
        final Map<String, Object> gatewayServerTraceValue = ((MapTraceValue) target).__getTraceBindValue();
        if (gatewayServerTraceValue != null) {
            final Map<String, Object> traceValue = ((MapTraceValue) result).__getTraceBindValue();
            // copy to destinationId
            if (traceValue != null) {
                traceValue.put("destinationId", gatewayServerTraceValue.get("destinationId"));
            }
        }
    }
}