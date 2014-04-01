package com.nhn.pinpoint.testweb.netty;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConverterNotFoundException;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class HttpCustomServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpCustomServerHandler.class.getName());

	private ListeningExecutorService listeningExecutorService;

	public HttpCustomServerHandler() {
		this.listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		this.listeningExecutorService.submit(new InvokeTask(ctx, e));
	}

	private class InvokeTask implements Runnable {
		private final ChannelHandlerContext ctx;
		private final MessageEvent e;

		public InvokeTask(ChannelHandlerContext ctx, MessageEvent e) {
			this.ctx = ctx;
			this.e = e;
		}

		public void run() {
			if (!(e.getMessage() instanceof HttpRequest)) {
				logger.debug("[n/a] received message is illegal.");
				DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
				ctx.getChannel().write(res).addListener(ChannelFutureListener.CLOSE);
				return;
			}

			HttpRequest request = (HttpRequest) e.getMessage();

			try {
				StringBuilder buf = new StringBuilder();

				// this.request = (HttpRequest) e.getMessage();
				Object[] requestBody = null;
				Object functionsResult = null;

				HttpResponseStatus status = OK;
				String uri = request.getUri().substring(1);

				long requestTime = System.currentTimeMillis();

				// List<Entry<String, String>> headers = request.getHeaders();
				List<Entry<String, String>> reponseHeader = new ArrayList<Entry<String, String>>();

				buf.setLength(0);

				// if (httpMethodMapper != null) {
				HttpMethod reqMethod = request.getMethod();
				// HttpMethodInfo methodInfo =
				// httpMethodMapper.getMethod(uri.replaceAll("\\?.*", ""),
				// reqMethod);
				//
				// if (methodInfo != null) {
				if (reqMethod.equals(HttpMethod.POST) || reqMethod.equals(HttpMethod.PUT) || reqMethod.equals(HttpMethod.DELETE)) {
					// if
					// (methodInfo.getHttpMethod().equals(Path.HttpMethod.POST)
					// || methodInfo.getHttpMethod().equals(Path.HttpMethod.PUT)
					// ||
					// methodInfo.getHttpMethod().equals(Path.HttpMethod.DELETE))
					// {
					ChannelBuffer content = request.getContent();

					buf.append("HelloNetty");

					if (content.readable()) {
						try {
							// requestBody = addArgs(methodInfo, request, null);
							// functionsResult =
							// methodInfo.getMethod().invoke(methodInfo.getClassObj(),
							// requestBody);
							//
							// if (functionsResult instanceof
							// ResponseWithHttpCode) {
							// ResponseWithHttpCode result =
							// (ResponseWithHttpCode) functionsResult;
							// status = result.getCode();
							// reponseHeader.addAll(result.getHeaders());
							// buf.append(parseResponse(result.getObj()));
							// } else {
							// buf.append(parseResponse(functionsResult));
							// }
						} catch (ConverterNotFoundException e1) {
							status = HttpResponseStatus.BAD_REQUEST;
							logger.error("convert fail : exception={}", e1.getMessage());
							// buf.append(status.getCode() + "\r\n"
							// + "Check Your Type");
							buf.append(getResultString(status.getCode(), "Invalid parameter"));
						}
					} else {
						status = HttpResponseStatus.BAD_REQUEST;
						// buf.append(status.getCode() + "\r\n" +
						// "No content on request.");
						buf.append(getResultString(status.getCode(), "No content on request."));
					}
					// } else {
					// status = HttpResponseStatus.BAD_REQUEST;
					// buf.append(getResultString(status.getCode(),
					// "incorrect method of this uri"));
					// // buf.append(status.getCode() + "\r\n" +
					// // "incorrect method of this uri");
					//
					// }
				} else if (reqMethod.equals(HttpMethod.GET)) {
					// if
					// (methodInfo.getHttpMethod().equals(Path.HttpMethod.GET))
					// {
					QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
					Map<String, List<String>> queries = queryStringDecoder.getParameters();

					buf.append("HelloNetty");

					// functionsResult =
					// methodInfo.getMethod().invoke(methodInfo.getClassObj(),
					// addArgs(methodInfo, request, queries));
					// if (functionsResult instanceof ResponseWithHttpCode) {
					// ResponseWithHttpCode result = (ResponseWithHttpCode)
					// functionsResult;
					// status = result.getCode();
					// reponseHeader.addAll(result.getHeaders());
					// buf.append(parseResponse(result.getObj()));
					// } else {
					// buf.append(parseResponse(functionsResult));
					// }
					// } else {
					// status = HttpResponseStatus.BAD_REQUEST;
					// // buf.append(status.getCode() + "\r\n" +
					// // "incorrect method of this uri");
					// buf.append(getResultString(status.getCode(),
					// "incorrect method of this uri"));
					// }
				} else {
					status = HttpResponseStatus.METHOD_NOT_ALLOWED;
					// buf.append(status.getCode() + "\r\n" +
					// "method not supports");
					buf.append(getResultString(status.getCode(), "method not supports"));
				}
				// } else {
				// status = HttpResponseStatus.NOT_FOUND;
				// // buf.append(status.getCode() + "\r\n" +
				// // "we don't have this uri");
				// buf.append(getResultString(status.getCode(),
				// "we don't have this uri"));
				// }

				// requestCounter.incr(
				// getStatisticsKey(),(System.currentTimeMillis() -
				// requestTime),status.hashCode() >= 400);

				// if (methodInfo != null) {
				// if (StringUtils.isNotEmpty(System.getProperty("monitorMode"))
				// && System.getProperty("monitorMode").equals("URI")) {
				// String rareUri = uri.replaceAll("\\?.*", "");
				// requestCounter.incr(request.getMethod().getName() + " " +
				// rareUri, System.currentTimeMillis() - requestTime,
				// status.getCode() >= 400);
				// } else {
				// requestCounter.incr(methodInfo.getMethod().getName(),
				// System.currentTimeMillis() - requestTime, status.getCode() >=
				// 400);
				// }
				// } else {
				// if (StringUtils.isNotEmpty(System.getProperty("monitorMode"))
				// && System.getProperty("monitorMode").equals("URI")) {
				// String rareUri = uri.replaceAll("\\?.*", "");
				// requestCounter.incr(request.getMethod().getName() + " " +
				// rareUri, System.currentTimeMillis() - requestTime, true);
				// } else {
				// requestCounter.incr("unknown method",
				// System.currentTimeMillis() - requestTime, true);
				// }
				// }
				// }

				writeResponse(request, e, status, reponseHeader, buf);
			} catch (Exception ex) {
				handleException(request, this.ctx, ex);
			}
		}
	}

	protected void writeResponse(HttpRequest request, MessageEvent e, HttpResponseStatus status, List<Entry<String, String>> reponseHeader, StringBuilder resultContents) {
		// Decide whether to close the connection or not.
		boolean keepAlive = isKeepAlive(request);

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		response.setContent(ChannelBuffers.copiedBuffer(resultContents.toString(), CharsetUtil.UTF_8));

		// response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setHeader(CONTENT_TYPE, "application/json");

		response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
		if (keepAlive) {
			response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		for (Entry<String, String> header : reponseHeader) {
			response.setHeader(header.getKey(), header.getValue());
		}

		// Encode the cookie.
		String cookieString = request.getHeader(COOKIE);
		if (cookieString != null) {
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (Cookie cookie : cookies) {
					cookieEncoder.addCookie(cookie);
					response.addHeader(SET_COOKIE, cookieEncoder.encode());
				}
			}
		}

		ChannelFuture future = e.getChannel().write(response);

		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		} else
			future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
	}

	private String stackTraceToStr(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

		Channel channel = ctx.getChannel();
		if (channel.isOpen()) {
			/*
			 * request = (HttpRequest) e.getMessage();
			 * 
			 * // getCause를 두번하는 이유는 invocation exception에 wrap되서 오기때문 String
			 * body = request.getContent().readable() ? new
			 * String(request.getContent().array()) : ""; String cause =
			 * stackTraceToStr(e.getCause().getCause());
			 * 
			 * logger.error(
			 * "exceptionCaught : method={} \r\nURI={}, \r\nheaders={}, \r\nbody={}, \r\ncauseBy={}"
			 * , request.getMethod().getName(), request.getUri(),
			 * request.getHeaders(), body, cause);
			 * 
			 * if (channel.isWritable() && !(e.getCause() instanceof
			 * java.io.IOException)) {
			 * 
			 * HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
			 * HttpResponseStatus.INTERNAL_SERVER_ERROR);
			 * 
			 * response.setHeader(CONTENT_TYPE,
			 * "application/json; charset=UTF-8");
			 * response.setContent(ChannelBuffers
			 * .copiedBuffer(this.getResultString(500,
			 * "Internal Server Error",e.getCause().getCause().toString()),
			 * CharsetUtil.UTF_8));
			 * 
			 * channel.write(response);
			 * 
			 * 
			 * String uri = request.getUri().substring(1); HttpMethod reqMethod
			 * = request.getMethod(); HttpMethodInfo methodInfo =
			 * httpMethodMapper.getMethod(uri.replaceAll("\\?.*", ""),
			 * reqMethod);
			 * 
			 * if (methodInfo != null) {
			 * if(StringUtils.isNotEmpty(System.getProperty("monitorMode")) &&
			 * System.getProperty("monitorMode").equals("URI")) { String rareUri
			 * = uri.replaceAll("\\?.*", "");
			 * requestCounter.incr(request.getMethod().getName() + " " +
			 * rareUri, 0, true); } else {
			 * requestCounter.incr(methodInfo.getMethod().getName(), 0, true); }
			 * } else {
			 * if(StringUtils.isNotEmpty(System.getProperty("monitorMode")) &&
			 * System.getProperty("monitorMode").equals("URI")) { String rareUri
			 * = uri.replaceAll("\\?.*", "");
			 * requestCounter.incr(request.getMethod().getName() + " " +
			 * rareUri, 0, true); } else { requestCounter.incr("unknown method",
			 * 0, true); } } }
			 */

			logger.error("channel error : exception={}", e);
			channel.close();
		}
	}

	public void handleException(HttpRequest request, ChannelHandlerContext ctx, Exception e) {
		Channel channel = ctx.getChannel();
		if (channel.isOpen()) {
			String body = request.getContent().readable() ? new String(request.getContent().array()) : "";
			String cause = stackTraceToStr(e.getCause());

			logger.error("exceptionCaught : method={} \r\nURI={}, \r\nheaders={}, \r\nbody={}, \r\ncauseBy={}", request.getMethod().getName(), request.getUri(), request.getHeaders(), body, cause);

			if (channel.isWritable() && !(e.getCause() instanceof java.io.IOException)) {

				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);

				response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
				response.setContent(ChannelBuffers.copiedBuffer(this.getResultString(500, "Internal Server Error", e.getCause().toString()), CharsetUtil.UTF_8));

				channel.write(response);

				String uri = request.getUri().substring(1);
				HttpMethod reqMethod = request.getMethod();
				// HttpMethodInfo methodInfo =
				// httpMethodMapper.getMethod(uri.replaceAll("\\?.*", ""),
				// reqMethod);
				//
				// if (methodInfo != null) {
				// if (StringUtils.isNotEmpty(System.getProperty("monitorMode"))
				// && System.getProperty("monitorMode").equals("URI")) {
				// String rareUri = uri.replaceAll("\\?.*", "");
				// requestCounter.incr(request.getMethod().getName() + " " +
				// rareUri, 0, true);
				// } else {
				// requestCounter.incr(methodInfo.getMethod().getName(), 0,
				// true);
				// }
				// } else {
				// if (StringUtils.isNotEmpty(System.getProperty("monitorMode"))
				// && System.getProperty("monitorMode").equals("URI")) {
				// String rareUri = uri.replaceAll("\\?.*", "");
				// requestCounter.incr(request.getMethod().getName() + " " +
				// rareUri, 0, true);
				// } else {
				// requestCounter.incr("unknown method", 0, true);
				// }
				// }
			}
			channel.close();
		}
	}

	// public String parseResponse(Object obj) {
	// Gson gson = new Gson();
	// if (obj instanceof String) {
	// return (String) obj;
	// } else {
	// return gson.toJson(obj);
	// }
	// }

	// private Object[] addArgs(HttpMethodInfo methodInfo, HttpRequest request,
	// Map<String, List<String>> queries) {
	// return methodInfo.addArgs(conversionService, request, queries);
	// }

	private String getResultString(int statusCode, String message) {
		return String.format("{\"statusCode\":%s,\"statusMessage\":\"%s\"}", statusCode, message);
	}

	private String getResultString(int statusCode, String message, String cause) {
		return String.format("{\"statusCode\":%s,\"statusMessage\":\"%s\",\"causeBy\":\"%s\"}", statusCode, message, cause);
	}
}
