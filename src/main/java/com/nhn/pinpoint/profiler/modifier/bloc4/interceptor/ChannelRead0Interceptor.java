package com.nhn.pinpoint.profiler.modifier.bloc4.interceptor;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.bootstrap.util.NumberUtils;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.SpanId;

/**
 * @author netspider
 */
public class ChannelRead0Interceptor extends SpanSimpleAroundInterceptor implements TargetClassLoader {

	private MetaObject<java.nio.charset.Charset> getUriEncoding = new MetaObject<java.nio.charset.Charset>("__getUriEncoding");

    public ChannelRead0Interceptor() {
        super(ChannelRead0Interceptor.class);
    }

	@Override
	public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
		io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
		io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

		trace.markBeforeTime();
		if (trace.canSampled()) {
			trace.recordServiceType(ServiceType.BLOC);
			final String requestURL = request.getUri();
			trace.recordRpcName(requestURL);

			String endPoint = getIpPort(ctx.channel().localAddress());
			String remoteAddress = getIp(ctx.channel().remoteAddress());

			trace.recordEndPoint(endPoint);
			trace.recordRemoteAddress(remoteAddress);
			trace.recordAttribute(AnnotationKey.HTTP_URL, request.getUri());
		}

		if (!trace.isRoot()) {
			recordParentInfo(trace, request, ctx);
		}
	}

	@Override
	protected Trace createTrace(Object target, Object[] args) {

		final io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

		final boolean sampling = samplingEnable(request);
		if (!sampling) {
			// 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야
			// 한다.
			// sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에
			// sampling disable 파라미터를 박을수 있다.
			final Trace trace = getTraceContext().disableSampling();
			if (isDebug) {
				logger.debug("mark disable sampling. skip trace");
			}
			return trace;
		}

		final TraceId traceId = populateTraceIdFromRequest(request);
		if (traceId != null) {
			final Trace trace = getTraceContext().continueTraceObject(traceId);
			if (trace.canSampled()) {
				if (isDebug) {
					String requestURL = request.getUri();
					io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
					String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
					logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] { traceId, requestURL, remoteAddr });
				}
				return trace;
			} else {
				if (isDebug) {
					String requestURL = request.getUri();
					io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
					String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
					logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] { traceId, requestURL, remoteAddr });
				}
				return trace;
			}
		} else {
			final Trace trace = getTraceContext().newTraceObject();
			if (trace.canSampled()) {
				if (isDebug) {
					String requestURL = request.getUri();
					io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
					String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
					logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[] { requestURL, remoteAddr });
				}
				return trace;
			} else {
				if (isDebug) {
					String requestURL = request.getUri();
					io.netty.channel.ChannelHandlerContext ctx = (io.netty.channel.ChannelHandlerContext) args[0];
					String remoteAddr = ((SocketChannel) ctx.channel()).remoteAddress().toString();
					logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[] { requestURL, remoteAddr });
				}
				return trace;
			}
		}
	}

	@Override
	public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
		if (trace.canSampled()) {
			io.netty.handler.codec.http.FullHttpRequest request = (io.netty.handler.codec.http.FullHttpRequest) args[1];

			if (HttpMethod.POST.name().equals(request.getMethod().name()) || HttpMethod.PUT.name().equals(request.getMethod().name())) {
				// TODO record post body
			} else {
				java.nio.charset.Charset uriEncoding = getUriEncoding.invoke(target);
				String parameters = getRequestParameter(request, 64, 512, uriEncoding);
				if (parameters != null && parameters.length() > 0) {
					trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
				}
			}

			trace.recordApi(getMethodDescriptor());
		}
		trace.recordException(throwable);
		trace.markAfterTime();
	}

	private boolean samplingEnable(io.netty.handler.codec.http.FullHttpRequest request) {
		// optional 값.
		String samplingFlag = request.headers().get(Header.HTTP_SAMPLED.toString());
		return SamplingFlagUtils.isSamplingFlag(samplingFlag);
	}

	/**
	 * Pupulate source trace from HTTP Header.
	 * 
	 * @param request
	 * @return
	 */
	private TraceId populateTraceIdFromRequest(io.netty.handler.codec.http.FullHttpRequest request) {
		final HttpHeaders headers = request.headers();

		String transactionId = headers.get(Header.HTTP_TRACE_ID.toString());
		if (transactionId != null) {
			long parentSpanId = NumberUtils.parseLong(headers.get(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
			// TODO NULL이 되는게 맞는가?
			long spanId = NumberUtils.parseLong(headers.get(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
			short flags = NumberUtils.parseShort(headers.get(Header.HTTP_FLAGS.toString()), (short) 0);

			final TraceId id = getTraceContext().createTraceId(transactionId, parentSpanId, spanId, flags);
			if (isDebug) {
				logger.debug("TraceID exist. continue trace. {}", id);
			}
			return id;
		} else {
			return null;
		}
	}

	private String getRequestParameter(io.netty.handler.codec.http.FullHttpRequest request, int eachLimit, int totalLimit, java.nio.charset.Charset uriEncoding) {
		String uri = request.getUri();
		QueryStringDecoder decoder = new QueryStringDecoder(uri, uriEncoding);
		Map<String, List<String>> parameters = decoder.parameters();

		final StringBuilder params = new StringBuilder(64);

		for (Entry<String, List<String>> entry : parameters.entrySet()) {
			if (params.length() != 0) {
				params.append('&');
			}

			if (params.length() > totalLimit) {
				params.append("...");
				break;
			}

			String key = entry.getKey();

			params.append(StringUtils.drop(key, eachLimit));
			params.append("=");

			Object value = entry.getValue().get(0);

			if (value != null) {
				params.append(StringUtils.drop(StringUtils.toString(value), eachLimit));
			}
		}

		return params.toString();
	}

	private String getIp(SocketAddress socketAddress) {
		if (socketAddress instanceof InetSocketAddress) {
			InetSocketAddress addr = (InetSocketAddress) socketAddress;
			return addr.getAddress().getHostAddress();
		} else {
			return "NOT_SUPPORTED_ADDRESS";
		}
	}

	private String getIpPort(SocketAddress socketAddress) {
		String address = socketAddress.toString();

		if (socketAddress instanceof InetSocketAddress) {
			InetSocketAddress addr = (InetSocketAddress) socketAddress;
			return addr.getAddress().getHostAddress() + ":" + addr.getPort();
		}

		if (address.startsWith("/")) {
			return address.substring(1);
		} else {
			if (address.contains("/")) {
				return address.substring(address.indexOf("/") + 1);
			} else {
				return address;
			}
		}
	}

	private void recordParentInfo(RecordableTrace trace, io.netty.handler.codec.http.FullHttpRequest request, io.netty.channel.ChannelHandlerContext ctx) {
		HttpHeaders headers = request.headers();
		String parentApplicationName = headers.get(Header.HTTP_PARENT_APPLICATION_NAME.toString());
		
		if (parentApplicationName != null) {
			// FIXME record Acceptor Host는 URL상의 host를 가져와야한다. 일단 가져올 수 있는 방법이 없어보여 IP라도 추가해둠.
			String acceptorHost = getIpPort(ctx.channel().localAddress());
			trace.recordAcceptorHost(acceptorHost);

			final String type = headers.get(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
			final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
			trace.recordParentApplication(parentApplicationName, parentApplicationType);
		}
	}
}