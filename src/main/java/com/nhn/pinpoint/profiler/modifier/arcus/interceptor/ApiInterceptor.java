package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

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
public class ApiInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(ApiInterceptor.class.getName());
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
    
    String getAnnotation(Object[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<args.length; i++) {
			sb.append(i).append(":");
			sb.append(args[i].toString());
			sb.append(" ");
		}
		return sb.toString();
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
		trace.recordAttribute(AnnotationKey.ARCUS_COMMAND, getAnnotation(args));
		
		// determine the service type
		String serviceCode = (String) getServiceCode.invoke(target);
		if (serviceCode != null) {
			trace.recordDestinationId(serviceCode);
			trace.recordServiceType(ServiceType.ARCUS);
		} else {
			trace.recordDestinationId("MEMCACHED");
			trace.recordServiceType(ServiceType.MEMCACHED);
		}

		// find the target node
		Operation op = (Operation) getOperation.invoke(((Future<?>)result));
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
		
		trace.markAfterTime();
		trace.traceBlockEnd();
    }
}
