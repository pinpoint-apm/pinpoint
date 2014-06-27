package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import net.spy.memcached.plugin.FrontCacheGetFuture;

/**
 * @author emeroad
 */
public class ApiInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, ParameterExtractorSupport, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetaObject<Object> getOperation = new MetaObject<Object>("__getOperation");
    private MetaObject<Object> getServiceCode = new MetaObject<Object>("__getServiceCode");
    
    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;
    private ParameterExtractor parameterExtractor;

    @Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}
		
		final Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
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

		final Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		try {
            if (parameterExtractor != null) {
                final int index = parameterExtractor.getIndex();
                final Object recordObject = parameterExtractor.extractObject(args);
                trace.recordApi(methodDescriptor, recordObject, index);
            } else {
                trace.recordApi(methodDescriptor);
            }

            // find the target node
            if (result instanceof OperationFuture) {
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
            }

            // determine the service type
            String serviceCode = (String) getServiceCode.invoke(target);
            if (serviceCode != null) {
                trace.recordDestinationId(serviceCode);
                trace.recordServiceType(ServiceType.ARCUS);
            } else {
                trace.recordDestinationId("MEMCACHED");
                trace.recordServiceType(ServiceType.MEMCACHED);
            }

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

    @Override
    public void setParameterExtractor(ParameterExtractor parameterExtractor) {
        this.parameterExtractor = parameterExtractor;
    }
}
