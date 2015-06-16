package com.navercorp.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;
import com.navercorp.pinpoint.plugin.arcus.ParameterUtils;

/**
 * @author emeroad
 */
@Group(ArcusConstants.ARCUS_SCOPE)
public class ApiInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements ArcusConstants {
    private final boolean traceKey;
    private final int keyIndex;
    
    private final MetadataAccessor serviceCodeAccessor;
    private final MetadataAccessor operationAccessor;
    private final MetadataAccessor asyncTraceIdAccessor;
    
    public ApiInterceptor(TraceContext context, MethodInfo targetMethod,
            @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor, @Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor, @Name(METADATA_OPERATION) MetadataAccessor operationAccessor, boolean traceKey) {
        super(context, targetMethod.getDescriptor());
        
        if (traceKey) {
            int index = ParameterUtils.findFirstString(targetMethod, 3);
        
            if (index != -1) {
                this.traceKey = true;
                this.keyIndex = index; 
            } else {
                this.traceKey = false;
                this.keyIndex = -1;
            }
        } else {
            this.traceKey = false;
            this.keyIndex = -1;
        }
        
        this.serviceCodeAccessor = serviceCodeAccessor;
        this.operationAccessor = operationAccessor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        if (traceKey) {
            final Object recordObject = args[keyIndex];
            trace.recordApi(getMethodDescriptor(), recordObject, keyIndex);
        } else {
            trace.recordApi(getMethodDescriptor());
        }
        trace.recordException(throwable);

        // find the target node
        if (result instanceof Future && operationAccessor.isApplicable(result)) {
            Operation op = operationAccessor.get(result);
            
            if (op != null) {
                MemcachedNode handlingNode = op.getHandlingNode();
                SocketAddress socketAddress = handlingNode.getSocketAddress();
     
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) socketAddress;
                    trace.recordEndPoint(address.getHostName() + ":" + address.getPort());
                }
            } else {
                logger.info("operation not found");
            }
        }

        if(serviceCodeAccessor.isApplicable(target)) {
            // determine the service type
            String serviceCode = serviceCodeAccessor.get(target);
            if (serviceCode != null) {
                trace.recordDestinationId(serviceCode);
                trace.recordServiceType(ARCUS);
            } else {
                trace.recordDestinationId("MEMCACHED");
                trace.recordServiceType(ServiceType.MEMCACHED);
            }
        } else {
            trace.recordDestinationId("MEMCACHED");
            trace.recordServiceType(ServiceType.MEMCACHED);
        }
        

        try {
            if(isAsynchronousInvocation(target, args, result, throwable)) {
                // set asynchronous trace
                final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                trace.recordNextAsyncId(asyncTraceId.getAsyncId());
                asyncTraceIdAccessor.set(result, asyncTraceId);
                if (isDebug) {
                    logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
                }
            }
        } catch(Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
        
        trace.markAfterTime();
    }
    
    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if(throwable != null || result == null) {
            return false;
        }

        if (!asyncTraceIdAccessor.isApplicable(result)) {
            logger.debug("Invalid result object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }
}
