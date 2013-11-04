package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.util.MetaObject;

/**
 * @author emeroad
 */
public class FutureGetInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
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
            if (isDebug) {
                logger.debug("TraceID not exist. start new trace.");
            }
			trace = traceContext.newTraceObject();
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
			logger.debug("trace not found");
			return;
		}

        try {
            trace.recordApi(methodDescriptor);
//            중요한 파라미터가 아님 레코딩 안함.
//            String annotation = "future.get() timeout:" + args[0] + " " + ((TimeUnit)args[1]).name();
//            trace.recordAttribute(AnnotationKey.ARCUS_COMMAND, annotation);

            // find the target node
            final Operation op = (Operation) getOperation.invoke(target);
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
                trace.recordServiceType(ServiceType.ARCUS_FUTURE_GET);
            } else {
                trace.recordDestinationId("MEMCACHED");
                trace.recordServiceType(ServiceType.MEMCACHED_FUTURE_GET);
            }

            if (op != null) {
                trace.recordException(op.getException());
            }
//            cancel일때 exception은 안던지는 것인가?
//            if (op.isCancelled()) {
//                trace.recordAttribute(AnnotationKey.EXCEPTION, "cancelled by user");
//            }

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
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
