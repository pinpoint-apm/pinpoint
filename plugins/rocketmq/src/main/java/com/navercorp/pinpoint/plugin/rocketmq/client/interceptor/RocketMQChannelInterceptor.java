package com.navercorp.pinpoint.plugin.rocketmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rocketmq.client.RocketMQConstants;

public class RocketMQChannelInterceptor implements AroundInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private final TraceContext traceContext;
	private final MethodDescriptor descriptor;
	public RocketMQChannelInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
		this.traceContext = traceContext;
		this.descriptor = descriptor;
	}

	@Override
	public void before(Object target, Object[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}
		Trace trace = traceContext.currentRawTraceObject();

		if (trace == null) {
			return;
		}
		if (trace.canSampled()) {
			SpanEventRecorder recorder = trace.traceBlockBegin();
			recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
			recorder.recordApi(descriptor);
			if (throwable != null) {
				recorder.recordException(throwable);
			}
		}
	}

}
