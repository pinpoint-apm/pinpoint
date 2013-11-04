package com.nhn.pinpoint.profiler.modifier.connector.jdkhttpconnector.interceptor;

import java.net.HttpURLConnection;

import com.nhn.pinpoint.profiler.context.Header;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceId;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;

/**
 * @author netspider
 * @author emeroad
 */
public class ConnectMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
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

		TraceId nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());


		request.setRequestProperty(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
		request.setRequestProperty(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
		request.setRequestProperty(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));

		request.setRequestProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
		request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));

		trace.recordServiceType(ServiceType.JDK_HTTPURLCONNECTOR);

		String host = request.getURL().getHost();
		int port = request.getURL().getPort();

		// TODO protocol은 어떻게 표기하지???
        String endpoint = getEndpoint(host, port);
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endpoint);
		trace.recordDestinationId(endpoint);

		trace.recordAttribute(AnnotationKey.HTTP_URL, request.getURL().toString());
	}

    private String getEndpoint(String host, int port) {
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
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