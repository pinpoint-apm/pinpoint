package com.nhn.pinpoint.profiler.modifier.connector.asynchttpclient.interceptor;

import java.util.Collection;
import java.util.Iterator;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.config.DumpType;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.Header;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceId;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TargetClassLoader;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;
import com.nhn.pinpoint.profiler.util.InterceptorUtils;
import com.nhn.pinpoint.profiler.util.SimpleSampler;
import com.nhn.pinpoint.profiler.util.SimpleSamplerFactory;
import com.nhn.pinpoint.profiler.util.StringUtils;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.cookie.Cookie;

/**
 * intercept com.ning.http.client.AsyncHttpClient.executeRequest(Request, AsyncHandler<T>)
 * 
 * @author netspider
 * 
 */
public class ExecuteRequestInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	protected PLogger logger;
	protected boolean isDebug;

	protected TraceContext traceContext;
	protected MethodDescriptor descriptor;

	protected boolean collectCookie;
	protected DumpType cookieDumpType;
	protected SimpleSampler cookieSampler;

	protected boolean collectEntity;
	protected DumpType entityDumpType;
	protected SimpleSampler entitySampler;

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		final Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			return;
		}

		if (args.length == 0 || !(args[0] instanceof com.ning.http.client.Request)) {
			return;
		}

		final com.ning.http.client.Request httpRequest = (com.ning.http.client.Request) args[0];
		final FluentCaseInsensitiveStringsMap httpRequestHeaders = httpRequest.getHeaders();
		final boolean sampling = trace.canSampled();

		if (!sampling) {
			if (isDebug) {
				logger.debug("set Sampling flag=false");
			}
			if (httpRequest != null) {
				httpRequestHeaders.add(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
			}
			return;
		}

		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceId nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());
		trace.recordServiceType(ServiceType.HTTP_CLIENT);

		if (httpRequest != null) {
			httpRequestHeaders.add(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
			httpRequestHeaders.add(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
			httpRequestHeaders.add(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
			httpRequestHeaders.add(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
			httpRequestHeaders.add(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
			httpRequestHeaders.add(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
		}
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

		if (args.length == 0 || !(args[0] instanceof com.ning.http.client.Request)) {
			return;
		}

		try {
			final com.ning.http.client.Request httpRequest = (com.ning.http.client.Request) args[0];

			if (httpRequest != null) {
				// httpRequest에 뭔가 access하는 작업은 위험이 있으므로 after에서 작업한다.
				trace.recordAttribute(AnnotationKey.HTTP_URL, httpRequest.getUrl());

				String endpoint = getEndpoint(httpRequest.getURI().getHost(), httpRequest.getURI().getPort());
				trace.recordDestinationId(endpoint);

				recordHttpRequest(trace, httpRequest, result);
			}

			trace.recordApi(descriptor);
			trace.recordException(result);
			trace.markAfterTime();
		} finally {
			trace.traceBlockEnd();
		}
	}

	private String getEndpoint(String host, int port) {
		if (host == null) {
			return "UnknownHttpClient";
		}
		if (port < 0) {
			return host;
		}
		StringBuilder sb = new StringBuilder(host.length() + 8);
		sb.append(host);
		sb.append(':');
		sb.append(port);
		return sb.toString();
	}

	private void recordHttpRequest(Trace trace, com.ning.http.client.Request httpRequest, Object result) {
		final boolean isException = InterceptorUtils.isThrowable(result);
		if (collectCookie) {
			if (DumpType.ALWAYS == cookieDumpType) {
				recordCookie(httpRequest, trace);
			} else if (DumpType.EXCEPTION == cookieDumpType && isException) {
				recordCookie(httpRequest, trace);
			}
		}
		if (collectEntity) {
			if (DumpType.ALWAYS == entityDumpType) {
				recordEntity(httpRequest, trace);
			} else if (DumpType.EXCEPTION == entityDumpType && isException) {
				recordEntity(httpRequest, trace);
			}
		}
	}

	protected void recordCookie(com.ning.http.client.Request httpRequest, Trace trace) {
		if (cookieSampler.isSampling()) {
			Collection<Cookie> cookies = httpRequest.getCookies();
			StringBuilder sb = new StringBuilder();

			Iterator<Cookie> iterator = cookies.iterator();
			while (iterator.hasNext()) {
				Cookie cookie = iterator.next();
				sb.append(cookie.getName()).append("=").append(cookie.getValue());
				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
			trace.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(sb.toString(), 1024));
		}
	}

	protected void recordEntity(com.ning.http.client.Request httpRequest, Trace trace) {
		if (entitySampler.isSampling()) {
			FluentStringsMap requestParams = httpRequest.getParams();
			if (requestParams != null) {
				trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(requestParams.toString(), 1024));
			}
		}
	}

	/**
	 * copy: EntityUtils Obtains character set of the entity, if known.
	 * 
	 * @param entity
	 *            must not be null
	 * @return the character set, or null if not found
	 * @throws ParseException
	 *             if header elements cannot be parsed
	 * @throws IllegalArgumentException
	 *             if entity is null
	 */
	public static String getContentCharSet(final HttpEntity entity) throws ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		String charset = null;
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					charset = param.getValue();
				}
			}
		}
		return charset;
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;

		final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
		this.collectCookie = profilerConfig.isApacheHttpClient4ProfileCookie();
		this.cookieDumpType = profilerConfig.getApacheHttpClient4ProfileCookieDumpType();
		if (collectCookie) {
			this.cookieSampler = SimpleSamplerFactory.createSampler(collectCookie, profilerConfig.getApacheHttpClient4ProfileCookieSamplingRate());
		}

		this.collectEntity = profilerConfig.isApacheHttpClient4ProfileEntity();
		this.entityDumpType = profilerConfig.getApacheHttpClient4ProfileEntityDumpType();
		if (collectEntity) {
			this.entitySampler = SimpleSamplerFactory.createSampler(collectEntity, profilerConfig.getApacheHttpClient4ProfileEntitySamplingRate());
		}
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}
}
