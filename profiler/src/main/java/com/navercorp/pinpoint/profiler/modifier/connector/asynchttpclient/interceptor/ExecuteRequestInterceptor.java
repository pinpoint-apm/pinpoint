package com.nhn.pinpoint.profiler.modifier.connector.asynchttpclient.interceptor;

import java.io.InputStream;
import java.util.*;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.config.DumpType;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.nhn.pinpoint.bootstrap.util.InterceptorUtils;
import com.nhn.pinpoint.bootstrap.util.SimpleSampler;
import com.nhn.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.Part;
import com.ning.http.client.Request.EntityWriter;
import com.ning.http.client.cookie.Cookie;

/**
 * intercept com.ning.http.client.AsyncHttpClient.executeRequest(Request,
 * AsyncHandler<T>)
 * 
 * @author netspider
 * 
 */
public class ExecuteRequestInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	protected PLogger logger;
	protected boolean isDebug;

	protected TraceContext traceContext;
	protected MethodDescriptor descriptor;

	protected boolean dumpCookie;
	protected DumpType cookieDumpType;
	protected SimpleSampler cookieSampler;
	protected int cookieDumpSize;

	protected boolean dumpEntity;
	protected DumpType entityDumpType;
	protected SimpleSampler entitySampler;
	protected int entityDumpSize;

	protected boolean dumpParam;
	protected DumpType paramDumpType;
	protected SimpleSampler paramSampler;
	protected int paramDumpSize;

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

		final boolean sampling = trace.canSampled();

		if (!sampling) {
			if (isDebug) {
				logger.debug("set Sampling flag=false");
			}
			if (httpRequest != null) {
                final FluentCaseInsensitiveStringsMap httpRequestHeaders = httpRequest.getHeaders();
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
            final FluentCaseInsensitiveStringsMap httpRequestHeaders = httpRequest.getHeaders();
			putHeader(httpRequestHeaders, Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
			putHeader(httpRequestHeaders, Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
			putHeader(httpRequestHeaders, Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
			putHeader(httpRequestHeaders, Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
			putHeader(httpRequestHeaders, Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
			putHeader(httpRequestHeaders, Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
		}
	}

	private void putHeader(FluentCaseInsensitiveStringsMap httpRequestHeaders, String key, String value) {
		final List<String> valueList = new ArrayList<String>();
		valueList.add(value);
		httpRequestHeaders.put(key, valueList);
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
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

				recordHttpRequest(trace, httpRequest, throwable);
			}

			trace.recordApi(descriptor);
			trace.recordException(throwable);
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

	private void recordHttpRequest(Trace trace, com.ning.http.client.Request httpRequest, Throwable throwable) {
		final boolean isException = InterceptorUtils.isThrowable(throwable);
		if (dumpCookie) {
			if (DumpType.ALWAYS == cookieDumpType) {
				recordCookie(httpRequest, trace);
			} else if (DumpType.EXCEPTION == cookieDumpType && isException) {
				recordCookie(httpRequest, trace);
			}
		}
		if (dumpEntity) {
			if (DumpType.ALWAYS == entityDumpType) {
				recordEntity(httpRequest, trace);
			} else if (DumpType.EXCEPTION == entityDumpType && isException) {
				recordEntity(httpRequest, trace);
			}
		}
		if (dumpParam) {
			if (DumpType.ALWAYS == paramDumpType) {
				recordParam(httpRequest, trace);
			} else if (DumpType.EXCEPTION == paramDumpType && isException) {
				recordParam(httpRequest, trace);
			}
		}
	}

	protected void recordCookie(com.ning.http.client.Request httpRequest, Trace trace) {
		if (cookieSampler.isSampling()) {
			Collection<Cookie> cookies = httpRequest.getCookies();

			if (cookies.isEmpty()) {
				return;
			}

			StringBuilder sb = new StringBuilder(cookieDumpSize * 2);
			Iterator<Cookie> iterator = cookies.iterator();
			while (iterator.hasNext()) {
				Cookie cookie = iterator.next();
				sb.append(cookie.getName()).append("=").append(cookie.getValue());
				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
			trace.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(sb.toString(), cookieDumpSize));
		}
	}

	protected void recordEntity(final com.ning.http.client.Request httpRequest, final Trace trace) {
		if (entitySampler.isSampling()) {
			recordNonMultipartData(httpRequest, trace);
			recordMultipartData(httpRequest, trace);
		}
	}

	/**
	 * <pre>
	 * body는 string, byte, stream, entitywriter 중 하나가 입력된다.
	 * 여기에서는 stringdata만 수집하고 나머지는 일단 수집 안함.
	 * </pre>
	 * 
	 * @param httpRequest
	 * @param trace
	 */
	protected void recordNonMultipartData(final com.ning.http.client.Request httpRequest, final Trace trace) {
		final String stringData = httpRequest.getStringData();
		if (stringData != null) {
			trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(stringData, entityDumpSize));
			return;
		}

		final byte[] byteData = httpRequest.getByteData();
		if (byteData != null) {
			trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, "BYTE_DATA");
			return;
		}

		final InputStream streamData = httpRequest.getStreamData();
		if (streamData != null) {
			trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, "STREAM_DATA");
			return;
		}

		final EntityWriter entityWriter = httpRequest.getEntityWriter();
		if (entityWriter != null) {
			trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, "STREAM_DATA");
			return;
		}
	}

	/**
	 * record http multipart data
	 * 
	 * @param httpRequest
	 * @param trace
	 */
	protected void recordMultipartData(final com.ning.http.client.Request httpRequest, final Trace trace) {
		List<Part> parts = httpRequest.getParts();
		if (parts != null && parts.isEmpty()) {
			StringBuilder sb = new StringBuilder(entityDumpSize * 2);
			Iterator<Part> iterator = parts.iterator();
			while (iterator.hasNext()) {
				Part part = iterator.next();
				if (part instanceof com.ning.http.client.ByteArrayPart) {
					com.ning.http.client.ByteArrayPart p = (com.ning.http.client.ByteArrayPart) part;
					sb.append(part.getName());
					sb.append("=BYTE_ARRAY_");
					sb.append(p.getData().length);
				} else if (part instanceof com.ning.http.client.FilePart) {
					com.ning.http.client.FilePart p = (com.ning.http.client.FilePart) part;
					sb.append(part.getName());
					sb.append("=FILE_");
					sb.append(p.getMimeType());
				} else if (part instanceof com.ning.http.client.StringPart) {
					com.ning.http.client.StringPart p = (com.ning.http.client.StringPart) part;
					sb.append(part.getName());
					sb.append("=");
					sb.append(p.getValue());
				} else if (part instanceof com.ning.http.multipart.FilePart) {
					com.ning.http.multipart.FilePart p = (com.ning.http.multipart.FilePart) part;
					sb.append(part.getName());
					sb.append("=FILE_");
					sb.append(p.getContentType());
				} else if (part instanceof com.ning.http.multipart.StringPart) {
					com.ning.http.multipart.StringPart p = (com.ning.http.multipart.StringPart) part;
					sb.append(part.getName());
					// string을 꺼내오는 방법이 없고, apache http client의 adaptation
					// class라 무시.
					sb.append("=STRING");
				}

				if (sb.length() >= entityDumpSize) {
					break;
				}

				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
			trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(sb.toString(), entityDumpSize));
		}
	}

	/**
	 * record http request parameter
	 * 
	 * @param httpRequest
	 * @param trace
	 */
	protected void recordParam(final com.ning.http.client.Request httpRequest, final Trace trace) {
		if (paramSampler.isSampling()) {
			FluentStringsMap requestParams = httpRequest.getParams();
			if (requestParams != null) {
				String params = paramsToString(requestParams, paramDumpSize);
				trace.recordAttribute(AnnotationKey.HTTP_PARAM, StringUtils.drop(params, paramDumpSize));
			}
		}
	}

	/**
	 * com.ning.http.client.FluentStringsMap.toString()에서 큰따옴표, 공백, 세미콜론을 제거한 버전
	 * 
	 * @param params
	 * @param limit
	 * @return
	 */
	private String paramsToString(FluentStringsMap params, int limit) {
		StringBuilder result = new StringBuilder(limit * 2);

		for (Map.Entry<String, List<String>> entry : params.entrySet()) {
			if (result.length() > 0) {
				result.append(",");
			}
			result.append(entry.getKey());
			result.append("=");

			boolean needsComma = false;

			for (String value : entry.getValue()) {
				if (needsComma) {
					result.append(", ");
				} else {
					needsComma = true;
				}
				result.append(value);
			}
			
			if (result.length() >= limit) {
				break;
			}
		}
		return result.toString();
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;

		final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
		this.dumpCookie = profilerConfig.isNingAsyncHttpClientProfileCookie();
		this.cookieDumpType = profilerConfig.getNingAsyncHttpClientProfileCookieDumpType();
		this.cookieDumpSize = profilerConfig.getNingAsyncHttpClientProfileCookieDumpSize();
		if (dumpCookie) {
			this.cookieSampler = SimpleSamplerFactory.createSampler(dumpCookie, profilerConfig.getNingAsyncHttpClientProfileCookieSamplingRate());
		}

		this.dumpEntity = profilerConfig.isNingAsyncHttpClientProfileEntity();
		this.entityDumpType = profilerConfig.getNingAsyncHttpClientProfileEntityDumpType();
		this.entityDumpSize = profilerConfig.getNingAsyncHttpClientProfileEntityDumpSize();
		if (dumpEntity) {
			this.entitySampler = SimpleSamplerFactory.createSampler(dumpEntity, profilerConfig.getNingAsyncHttpClientProfileEntitySamplingRate());
		}

		this.dumpParam = profilerConfig.isNingAsyncHttpClientProfileParam();
		this.paramDumpType = profilerConfig.getNingAsyncHttpClientProfileParamDumpType();
		this.paramDumpSize = profilerConfig.getNingAsyncHttpClientProfileParamDumpSize();
		if (dumpParam) {
			this.paramSampler = SimpleSamplerFactory.createSampler(dumpParam, profilerConfig.getNingAsyncHttpClientProfileParamSamplingRate());
		}
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}
}
