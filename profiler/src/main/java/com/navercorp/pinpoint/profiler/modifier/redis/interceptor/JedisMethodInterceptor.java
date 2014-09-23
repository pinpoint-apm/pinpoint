package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.Map;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue;
import com.nhn.pinpoint.common.ServiceType;

/**
 * Redis client(jedis) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisMethodInterceptor extends SpanEventSimpleAroundInterceptor implements TargetClassLoader {

    public JedisMethodInterceptor() {
        super(JedisMethodInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        String destinationId = "Unknown";
        String endPoint = "Unknown";
        if (target instanceof ObjectTraceValue) {
            // find host & port
            final ObjectTraceValue traceValue = (ObjectTraceValue) target;
            if (traceValue.__getTraceObject() != null && traceValue.__getTraceObject() instanceof Map) {
                final Map<String, Object> map = (Map<String, Object>) traceValue.__getTraceObject();
                final Object host = map.get("host");
                final Object port = map.get("port");

                if (host != null) {
                    destinationId = (String) host;
                    if (port != null) {
                        endPoint = (String) host + ":" + port;
                    } else {
                        endPoint = (String) host;
                    }
                }
            }
        }

        trace.recordApi(getMethodDescriptor());
        trace.recordEndPoint(endPoint);
        trace.recordDestinationId(destinationId);
        trace.recordServiceType(ServiceType.REDIS);
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}