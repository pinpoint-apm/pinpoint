package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;

/**
 *
 */
public class FutureGetInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(FutureGetInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetaObject<Object> getOperation = new MetaObject<Object>("__getOperation");
    private MetaObject<Object> getServiceCode = new MetaObject<Object>("__getServiceCode");
    
    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;

    @Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			logger.info("trace not found");
			return;
		}
		
		trace.traceBlockBegin();
		trace.markBeforeTime();
	}

    @Override
    public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			logger.afterInterceptor(target, args, result);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			logger.info("trace not found");
			return;
		}
		
		trace.recordApi(methodDescriptor);
		String annotation = "future.get() timeout:" + args[0] + " " + ((TimeUnit)args[1]).name();
		trace.recordAttribute(AnnotationKey.ARCUS_COMMAND, annotation);
		
		// find the target node
		Operation op = (Operation) getOperation.invoke(target);
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
		
		// determine the service type
		String serviceCode = (String) getServiceCode.invoke((Operation)op);
		if (serviceCode != null) {
			trace.recordDestinationId(serviceCode);
			trace.recordServiceType(ServiceType.ARCUS);
		} else {
			trace.recordDestinationId("MEMCACHED");
			trace.recordServiceType(ServiceType.MEMCACHED);
		}
		
		trace.recordException(op.getException());
		if (op.isCancelled()) {
			trace.recordAttribute(AnnotationKey.EXCEPTION, "cancelled by user");
		}
		
		trace.markAfterTime();
		trace.traceBlockEnd();
    }
    
    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.methodDescriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

}
