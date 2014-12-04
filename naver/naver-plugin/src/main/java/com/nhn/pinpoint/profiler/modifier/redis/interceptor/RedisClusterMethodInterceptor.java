package com.nhn.pinpoint.profiler.modifier.redis.interceptor;

import java.util.Map;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.common.ServiceType;

/**
 * RedisCluster(nBase-ARC client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterMethodInterceptor extends SpanEventSimpleAroundInterceptor implements TargetClassLoader {

    public RedisClusterMethodInterceptor() {
        super(RedisClusterMethodInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        String destinationId = null;
        String endPoint = null;
        if (target instanceof MapTraceValue) {
            final Map<String, Object> traceValue = ((MapTraceValue) target).__getTraceBindValue();
            if (traceValue != null) {
                destinationId = (String) traceValue.get("destinationId");
                endPoint = (String) traceValue.get("endPoint");
            }
        }

        trace.recordApi(getMethodDescriptor());
        trace.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        trace.recordDestinationId(destinationId != null ? destinationId : ServiceType.NBASE_ARC.toString());
        trace.recordServiceType(ServiceType.NBASE_ARC);
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}