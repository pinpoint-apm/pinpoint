package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import java.io.IOException;
import java.net.URI;

import com.nhn.pinpoint.profiler.config.DumpType;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.Header;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceId;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;

import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;
import com.nhn.pinpoint.profiler.util.InterceptorUtils;
import com.nhn.pinpoint.profiler.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import org.apache.http.util.EntityUtils;

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
public class HttpUriRequestExecuteInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
    private TraceContext traceContext;

    private boolean cookie;
    private DumpType cookieDumpType;

    private boolean entity;
    private DumpType entityDumpType;

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
        if (request == null) {
            return;
        }
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

		TraceId nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
		request.addHeader(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
		request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));

		request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.addHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
		request.addHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));

		HttpHost host = extractHost(request.getURI());

        trace.recordServiceType(ServiceType.HTTP_CLIENT);

		int port = host.getPort();
        String endpoint = getEndpoint(host.getHostName(), port);

        trace.recordDestinationId(endpoint);
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

            final HttpUriRequest request = (HttpUriRequest) args[0];
            if (request != null) {
                trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestLine().getUri());

                final boolean isException = InterceptorUtils.isThrowable(result);
                if (cookie) {
                    if (DumpType.ALWAYS == cookieDumpType) {
                        dumpCookie(request, trace);
                    } else if(DumpType.EXCEPTION == cookieDumpType && isException){
                        dumpCookie(request, trace);
                    }
                }
                if (entity) {
                    if (DumpType.ALWAYS == entityDumpType) {
                        dumpEntity(request, trace);
                    } else if(DumpType.EXCEPTION == entityDumpType && isException) {
                        dumpEntity(request, trace);
                    }
                }
            }
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

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.cookie = profilerConfig.isApacheHttpClient4ProfileCookie();
        this.cookieDumpType = profilerConfig.getApacheHttpClient4ProfileCookieDumpType();
        this.entity = profilerConfig.isApacheHttpClient4ProfileEntity();
        this.entityDumpType = profilerConfig.getApacheHttpClient4ProfileEntityDumpType();

    }


    private void dumpCookie(HttpUriRequest request, Trace trace) {
        org.apache.http.Header[] cookies = request.getHeaders("Cookie");
        for (org.apache.http.Header header: cookies) {
            final String value = header.getValue();
            if (value != null && !value.isEmpty()) {
                trace.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(value, 1024));
            }
            // Cookie값이 2개 이상일수가 있나?
            break;
        }
    }

    private void dumpEntity(HttpUriRequest request, Trace trace) {
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
            try {
                final HttpEntity entity = entityRequest.getEntity();
                if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                    // entity utils의 toString시 일정 length까지만 데이터를 읽도록하는 기능이 필요함.
                    String entityString = EntityUtils.toString(entityRequest.getEntity(), "UTF8");
                    trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(entityString, 1024));
                }
            } catch (IOException e) {
                trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop("DumpError:" + e.getMessage(), 1024));
            }
        }
    }
}