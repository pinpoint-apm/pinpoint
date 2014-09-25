package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * Jedis Pipeline(redis client) constructor interceptor
 * - trace endPoint
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // trace endPoint
        if (!(target instanceof MapTraceValue) || !(args[0] instanceof MapTraceValue)) {
            return;
        }

        // first arg - redis.clients.jedis.Client
        final Map<String, Object> clientTraceValue = ((MapTraceValue) args[0]).__getTraceBindValue();
        if (clientTraceValue == null) {
            return;
        }

        final Map<String, Object> traceValue = new HashMap<String, Object>();
        traceValue.put("endPoint", clientTraceValue.get("endPoint"));
        ((MapTraceValue) target).__setTraceBindValue(traceValue);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}