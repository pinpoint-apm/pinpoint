package com.nhn.pinpoint.profiler.modifier.connector.jdkhttpconnector.interceptor;

import java.net.HttpURLConnection;
import java.net.URL;

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;

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
            request.setRequestProperty(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
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

        final URL url = request.getURL();
        final String host = url.getHost();
		final int port = url.getPort();

		// TODO protocol은 어떻게 표기하지???
        String endpoint = getEndpoint(host, port);
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endpoint);
		trace.recordDestinationId(endpoint);

		trace.recordAttribute(AnnotationKey.HTTP_URL, url.toString());
	}

    private String getEndpoint(String host, int port) {
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    @Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
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
            trace.recordException(throwable);

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