package com.profiler.modifier.arcus.interceptors;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import com.profiler.logging.Logger;

import com.profiler.common.AnnotationKey;
import com.profiler.context.AsyncTrace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.protocol.BaseOperationImpl;

import com.profiler.common.ServiceType;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.util.MetaObject;

/**
 *
 */
public class BaseOperationTransitionStateInterceptor implements StaticBeforeInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(BaseOperationTransitionStateInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private MetaObject getAsyncTrace = new MetaObject("__getAsyncTrace");
	private MetaObject getServiceCode = new MetaObject("__getServiceCode");

    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;

    @Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
			LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
		}

		AsyncTrace asyncTrace = (AsyncTrace) getAsyncTrace.invoke(target);
		if (asyncTrace == null) {
			logger.debug("asyncTrace not found");
			return;
		}

		OperationState newState = (OperationState) args[0];

		BaseOperationImpl baseOperation = (BaseOperationImpl) target;
		if (newState == OperationState.READING) {
			if (logger.isDebugEnabled()) {
				logger.debug("event:" + newState + " asyncTrace:" + asyncTrace);
			}
			if (asyncTrace.getState() != AsyncTrace.STATE_INIT) {
				return;
			}
			MemcachedNode handlingNode = baseOperation.getHandlingNode();
			SocketAddress socketAddress = handlingNode.getSocketAddress();
			if (socketAddress instanceof InetSocketAddress) {
				InetSocketAddress address = (InetSocketAddress) socketAddress;
				asyncTrace.recordEndPoint(address.getHostName() + ":" + address.getPort());
			}

			String serviceCode = (String) getServiceCode.invoke(target);

			if (serviceCode == null) {
				serviceCode = "UNKNOWN";
			}
			
			ServiceType svcType = ServiceType.ARCUS;
			
			if(serviceCode.equals(ServiceType.MEMCACHED.getDesc())) {
				svcType = ServiceType.MEMCACHED;
			}

            asyncTrace.recordServiceType(svcType);
//			asyncTrace.recordRpcName(baseOperation.getClass().getSimpleName());
            asyncTrace.recordApi(methodDescriptor);

            asyncTrace.recordDestinationId(serviceCode);

			String cmd = getCommand(baseOperation);
			asyncTrace.recordAttribute(AnnotationKey.ARCUS_COMMAND, cmd);

			// TimeObject timeObject = (TimeObject)
			// asyncTrace.getAttachObject();
			// timeObject.markSendTime();

			// long createTime = asyncTrace.getBeforeTime();
			// asyncTrace.record(TraceAnnotation.ClientSend,
			// System.currentTimeMillis() - createTime);
			asyncTrace.markAfterTime();
//			asyncTrace.traceBlockEnd();
		} else if (newState == OperationState.COMPLETE || newState == OperationState.TIMEDOUT) {
			if (logger.isDebugEnabled()) {
				logger.debug("event:" + newState + " asyncTrace:" + asyncTrace);
			}
			boolean fire = asyncTrace.fire();
			if (!fire) {
				return;
			}
			Exception exception = baseOperation.getException();
            asyncTrace.recordException(exception);

			if (!baseOperation.isCancelled()) {
				TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
				// asyncTrace.record(TraceAnnotation.ClientRecv, timeObject.getSendTime());
				asyncTrace.markAfterTime();
				asyncTrace.traceBlockEnd();
			} else {
				asyncTrace.recordAttribute(AnnotationKey.EXCEPTION, "cancelled by user");
				TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
				// asyncTrace.record(TraceAnnotation.ClientRecv, timeObject.getCancelTime());
				asyncTrace.markAfterTime();
				asyncTrace.traceBlockEnd();
			}
		}
	}

	private String getCommand(BaseOperationImpl baseOperation) {
		ByteBuffer buffer = baseOperation.getBuffer();
		if (buffer == null) {
			return "UNKNOWN";
		}
		// System.out.println(buffer.array().length + " po:" + buffer.position()
		// + " limit:" + buffer.limit() + " remaining"
		// + buffer.remaining() + " aoffset:" + buffer.arrayOffset());
		return new String(buffer.array(), UTF8);
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
