package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.JedisShardInfo;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * Jedis (redis client) constructor interceptor
 * - trace endPoint
 * 
 * @author jaehong.kim
 *
 */
public class JedisConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // trace endPoint
        if (!(target instanceof MapTraceValue)) {
            return;
        }

        final StringBuilder endPoint = new StringBuilder();
        try {
            // first arg - host
            if (args[0] instanceof String) {
                endPoint.append(args[0]);
                // second arg - port
                if (args.length >= 2 && args[1] instanceof Integer) {
                    endPoint.append(":").append(args[1]);
                } else {
                    // default port
                    endPoint.append(":").append(6379);
                }
            } else if (args[0] instanceof URI) {
                final URI uri = (URI) args[0];
                endPoint.append(uri.getHost());
                endPoint.append(":");
                endPoint.append(uri.getPort());
            } else if (args[0] instanceof JedisShardInfo) {
                final JedisShardInfo info = (JedisShardInfo) args[0];
                endPoint.append(info.getHost());
                endPoint.append(":");
                endPoint.append(info.getPort());
            }
        } catch (Exception e) {
            // expect 'class not found exception - JedisShardInfo'
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to trace endPoint('not found JedisShardInfo' is compatibility error). caused={}", e.getMessage(), e);
            }
        }

        final Map<String, Object> traceValue = new HashMap<String, Object>();
        traceValue.put("endPoint", endPoint.toString());
        ((MapTraceValue) target).__setTraceBindValue(traceValue);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}