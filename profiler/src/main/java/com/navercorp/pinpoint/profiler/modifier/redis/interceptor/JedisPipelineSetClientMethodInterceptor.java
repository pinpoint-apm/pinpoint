package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * Redis client(jedis) pipeline method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineSetClientMethodInterceptor implements SimpleAroundInterceptor, TargetClassLoader {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!(target instanceof ObjectTraceValue) || !(args[0] instanceof ObjectTraceValue)) {
            return;
        }

        // trace host & port
        final ObjectTraceValue traceValue = (ObjectTraceValue) target;
        final Map<String, Object> map = new HashMap<String, Object>();

        // first arg - redis.clients.jedis.Client
        final ObjectTraceValue clientTraceValue = (ObjectTraceValue) args[0];
        if (clientTraceValue.__getTraceObject() != null) {
            final Map<String, Object> clientMap = (Map<String, Object>) clientTraceValue.__getTraceObject();
            map.put("host", clientMap.get("host"));
            map.put("port", clientMap.get("port"));
        }
        traceValue.__setTraceObject(map);

        return;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}