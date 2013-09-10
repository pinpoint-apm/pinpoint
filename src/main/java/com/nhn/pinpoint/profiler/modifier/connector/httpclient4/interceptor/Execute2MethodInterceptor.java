package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import java.net.URI;

import com.nhn.pinpoint.profiler.context.Header;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceID;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;

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
public class Execute2MethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(Execute2MethodInterceptor.class.getName());
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

        final HttpUriRequest request = (HttpUriRequest) args[0];
        // UUID format을 그대로.
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if(isDebug) {
                logger.debug("set Sampling flag=false");
            }
            request.addHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            return;
        }

		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceID nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
		request.addHeader(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
		request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));

		request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.addHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationId());
		request.addHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(ServiceType.TOMCAT.getCode()));

		HttpHost host = extractHost(request.getURI());

        trace.recordServiceType(ServiceType.HTTP_CLIENT);

		int port = host.getPort();
        String endpoint = getEndpoint(host.getHostName(), port);

        trace.recordDestinationId(endpoint);

		trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestLine().getUri());
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

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}