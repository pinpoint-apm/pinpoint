package com.profiler.modifier.connector.jdkhttpconnector.interceptor;

import java.net.HttpURLConnection;

import com.profiler.interceptor.*;
import com.profiler.logging.Logger;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.context.*;
import com.profiler.logging.LoggerFactory;
import com.profiler.sampler.util.SamplingFlagUtils;

/**
 * @author netspider
 * 
 */
public class ConnectMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(ConnectMethodInterceptor.class.getName());
	private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
    private TraceContext traceContext;

    @Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        HttpURLConnection request = (HttpURLConnection) target;
        // UUID format을 그대로.
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            request.addRequestProperty(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            return;
        }


		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceID nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		// UUID format을 그대로.
		request.setRequestProperty(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
		request.setRequestProperty(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
		request.setRequestProperty(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));

		request.setRequestProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationId());
		request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(ServiceType.TOMCAT.getCode()));

		trace.recordServiceType(ServiceType.JDK_HTTPURLCONNECTOR);

		String host = request.getURL().getHost();
		int port = request.getURL().getPort();

		// TODO protocol은 어떻게 표기하지???
		trace.recordDestinationId(host + ((port > 0) ? ":" + port : ""));

		trace.recordAttribute(AnnotationKey.HTTP_URL, request.getURL().toString());
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