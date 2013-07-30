package com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceID;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;

public class InvokeInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(InvokeInterceptor.class.getName());
	private final boolean isDebug = logger.isDebugEnabled();

	private final MetaObject<InetSocketAddress> getServerAddress = new MetaObject<InetSocketAddress>("__getServerAddress");

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	// private int apiId;

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			logger.warn("Trace object is null");
			return;
		}

		final com.nhncorp.lucy.npc.connector.NpcHessianConnector self = (com.nhncorp.lucy.npc.connector.NpcHessianConnector) target;

		String objectName;
		String methodName;
		Charset charset;
		Object params;

		if (args.length == 3) {
			objectName = (String) args[0];
			methodName = (String) args[1];
			charset = self.getDefaultCharset();
			params = args[2];
		} else if (args.length == 4) {
			objectName = (String) args[0];
			methodName = (String) args[1];
			charset = (Charset) args[2];
			params = args[3];
		}

		//
		// TODO add sampling logic here.
		//

		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceID nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		//
		// TODO add pinpoint headers to the request message here.
		//

		trace.recordServiceType(ServiceType.NPC_CLIENT);

		InetSocketAddress serverAddress = getServerAddress.invoke(target);
		int port = serverAddress.getPort();
		trace.recordDestinationId(serverAddress.getHostName() + ((port > 0) ? ":" + port : ""));

		trace.recordAttribute(AnnotationKey.NPC_URL, serverAddress.toString());
	}

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			// result는 로깅하지 않는다.
			logger.afterInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		trace.recordApi(descriptor);
		trace.recordException(result);

		trace.markAfterTime();
		trace.traceBlockEnd();
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}
}