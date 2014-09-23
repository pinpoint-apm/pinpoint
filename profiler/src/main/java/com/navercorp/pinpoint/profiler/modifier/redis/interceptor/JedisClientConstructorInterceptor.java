package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * Redis client(jedis) constructor interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisClientConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

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
        // first arg - host
        if (args[0] instanceof String) {
            map.put("host", args[0]);
            // default port
            map.put("port", 6379);
        }

        // second arg - port
        if (args.length >= 2 && args[1] instanceof Integer) {
            map.put("port", args[1]);
        }

        traceValue.__setTraceObject(map);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}