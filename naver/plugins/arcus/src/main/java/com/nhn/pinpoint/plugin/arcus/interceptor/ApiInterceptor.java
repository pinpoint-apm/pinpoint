package com.nhn.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.plugin.arcus.ParameterUtils;
import com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;

/**
 * @author emeroad
 */
public class ApiInterceptor extends SpanEventSimpleAroundInterceptor {
    private final boolean traceKey;
    private final int keyIndex;
    
    public ApiInterceptor(TraceContext context, MethodInfo targetMethod, boolean traceKey) {
        super(ApiInterceptor.class);
        
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
        
        this.setTraceContext(context);
        this.setMethodDescriptor(targetMethod.getDescriptor());
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

        // find the target node
        if (result instanceof Future) {
            Operation op = ((OperationAccessor)result).__getOperation();
            
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

        // determine the service type
        String serviceCode = ((ServiceCodeAccessor)target).__getServiceCode();
        
        if (serviceCode != null) {
            trace.recordDestinationId(serviceCode);
            trace.recordServiceType(ServiceType.ARCUS);
        } else {
            trace.recordDestinationId("MEMCACHED");
            trace.recordServiceType(ServiceType.MEMCACHED);
        }

        trace.markAfterTime();
    }
}
