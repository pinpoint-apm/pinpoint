package com.profiler.modifier.connector.httpclient4.interceptor;

import java.net.URI;
import java.util.logging.Logger;

import com.profiler.context.*;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;

/**
 * Method interceptor
 * <p/>
 * <p/>
 * 
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public final HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException
 * </pre>
 */
public class Execute2MethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

	private final Logger logger = Logger.getLogger(Execute2MethodInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

	private MethodDescriptor descriptor;

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
			LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
		}
		TraceContext traceContext = DefaultTraceContext.getTraceContext();
		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceID nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());


        final HttpUriRequest request = (HttpUriRequest) args[0];
        // UUID format을 그대로.
		request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
		request.addHeader(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
		request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));
		request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
		request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.addHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationId());
		request.addHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(ServiceType.TOMCAT.getCode()));

		HttpHost host = extractHost(request.getURI());

        trace.recordServiceType(ServiceType.HTTP_CLIENT);

		int port = host.getPort();
//		trace.recordEndPoint(host.getHostName() + ((port > 0) ? ":" + port : ""));
        trace.recordDestinationId(host.getHostName() + ((port > 0) ? ":" + port : ""));
		trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestLine().getUri());
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
            // result는 로깅하지 않는다.
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args);
		}

		TraceContext traceContext = DefaultTraceContext.getTraceContext();
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
        TraceContext traceContext = DefaultTraceContext.getTraceContext();
        traceContext.cacheApi(descriptor);
    }

	private HttpHost extractHost(final URI uri) {
		if (uri == null) {
			return null;
		}
		HttpHost target = null;
		if (uri.isAbsolute()) {
			int port = uri.getPort(); // may be overridden later
			String host = uri.getHost();
			if (host == null) { // normal parse failed; let's do it ourselves
				// authority does not seem to care about the valid character-set
				// for host names
				host = uri.getAuthority();
				if (host != null) {
					// Strip off any leading user credentials
					int at = host.indexOf('@');
					if (at >= 0) {
						if (host.length() > at + 1) {
							host = host.substring(at + 1);
						} else {
							host = null; // @ on its own
						}
					}
					// Extract the port suffix, if present
					if (host != null) {
						int colon = host.indexOf(':');
						if (colon >= 0) {
							int pos = colon + 1;
							int len = 0;
							for (int i = pos; i < host.length(); i++) {
								if (Character.isDigit(host.charAt(i))) {
									len++;
								} else {
									break;
								}
							}
							if (len > 0) {
								try {
									port = Integer.parseInt(host.substring(pos, pos + len));
								} catch (NumberFormatException ex) {
								}
							}
							host = host.substring(0, colon);
						}
					}
				}
			}
			String scheme = uri.getScheme();
			if (host != null) {
				target = new HttpHost(host, port, scheme);
			}
		}
		return target;
	}
}