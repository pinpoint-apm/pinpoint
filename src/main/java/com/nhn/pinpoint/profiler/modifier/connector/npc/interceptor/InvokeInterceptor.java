package com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

public class InvokeInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private final MetaObject<InetSocketAddress> getServerAddress = new MetaObject<InetSocketAddress>("__getServerAddress");

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

//		Trace trace = traceContext.currentRawTraceObject();
        // 이부분은 현재 remote 호출시 traceId를 넣지 않으므로 currentRawTraceObject()를 호출하지 않는다.
        final Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		String objectName;
		String methodName;
		Charset charset;
		Object params;

		if (args.length == 3) {
			objectName = (String) args[0];
			methodName = (String) args[1];
			// TODO charset을 com.nhncorp.lucy.npc.connector.AbstractConnector.getDefaultCharset() 에서 조회 가능하긴 함.
			charset = null;
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

		TraceId nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		//
		// TODO add pinpoint headers to the request message here.
		//

		trace.recordServiceType(ServiceType.NPC_CLIENT);

		InetSocketAddress serverAddress = getServerAddress.invoke(target);
		int port = serverAddress.getPort();
        String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endPoint);
		trace.recordDestinationId(endPoint);

		trace.recordAttribute(AnnotationKey.NPC_URL, serverAddress.toString());
	}

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			// result는 로깅하지 않는다.
			logger.afterInterceptor(target, args);
		}

		final Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
        try {
            trace.recordApi(descriptor);
            trace.recordException(result);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
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