package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhncorp.redis.cluster.gateway.GatewayConfig;

/**
 * Gateway(nBase-ARC client) constructor interceptor
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

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

        final Map<String, Object> traceValue = new HashMap<String, Object>();
        try {
            final GatewayConfig config = (GatewayConfig) args[0];
            if (config.getDomainAddress() != null) {
                traceValue.put("destinationId", config.getDomainAddress());
            } else if (config.getIpAddress() != null) {
                traceValue.put("destinationId", config.getIpAddress());
            } else if (config.getClusterName() != null) {
                // over 1.1.x
                traceValue.put("destinationId", config.getClusterName());
            }
        } catch (Exception ignored) {
            // backward compatibility error or expect 'class not found exception - GatewayConfig'
        }

        ((MapTraceValue) target).__setTraceBindValue(traceValue);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}