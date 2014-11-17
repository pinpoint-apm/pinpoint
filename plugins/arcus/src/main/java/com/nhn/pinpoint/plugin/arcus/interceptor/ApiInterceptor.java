package com.nhn.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.interceptor.ParameterExtractor;
import com.nhn.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;

/**
 * @author emeroad
 */
public class ApiInterceptor extends SpanEventSimpleAroundInterceptor {
    private final ParameterExtractor parameterExtractor;

    public ApiInterceptor(ParameterExtractor parameterExtractor) {
        super(ApiInterceptor.class);
        this.parameterExtractor = parameterExtractor;
    }

    @Override
	public void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
		trace.markBeforeTime();
	}

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {

        if (parameterExtractor != null) {
            final int index = parameterExtractor.getIndex();
            final Object recordObject = parameterExtractor.extractObject(args);
            trace.recordApi(getMethodDescriptor(), recordObject, index);
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
