package com.linecorp.games.common.baseFramework.handlers;

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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;

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
import com.nhn.pinpoint.testweb.connector.ningasync.NingAsyncHttpClient;

public class HttpCustomServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpCustomServerHandler.class.getName());

	private final ListeningExecutorService listeningExecutorService;

	private final ArcusClient arcus;
	private final NingAsyncHttpClient asyncHttpInvoker = new NingAsyncHttpClient();

	public HttpCustomServerHandler() {
		this.listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
		this.arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        logger.debug("HttpCustomServerHandler.messageReceived ({})", Thread.currentThread().getName());
		this.listeningExecutorService.submit(new InvokeTask(ctx, e));
	}

	private void accessArcus() {
		Future<Boolean> future = null;
		try {
			future = arcus.set("pinpoint:test", 10, "Hello, Arcus");
			future.get(100L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (future != null) {
				future.cancel(true);
			}
		}
	}

	private void accessNaver() {
		asyncHttpInvoker.requestGet("http://blog.naver.com", NingAsyncHttpClient.getDummyParams(), NingAsyncHttpClient.getDummyHeaders(), NingAsyncHttpClient.getDummyCookies());
	}

	private void accessPinPointDev() {
		asyncHttpInvoker.requestGet("http://10.101.55.177:9080/threetier.pinpoint", null, null, null);
	}

	private class InvokeTask implements Runnable {
		private final ChannelHandlerContext ctx;
		private final MessageEvent e;

		public InvokeTask(ChannelHandlerContext ctx, MessageEvent e) {
			this.ctx = ctx;
			this.e = e;
		}

		public void run() {
			logger.debug("InvokeTask.run ({}}", Thread.currentThread().getName());

			if (!(e.getMessage() instanceof HttpRequest)) {
				logger.debug("[n/a] received message is illegal.");
				DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
				ctx.getChannel().write(res).addListener(ChannelFutureListener.CLOSE);
				return;
			}

			HttpRequest request = (HttpRequest) e.getMessage();

			// intercept

			try {
				StringBuilder buf = new StringBuilder();

				// this.request = (HttpRequest) e.getMessage();
				Object[] requestBody = null;
				Object functionsResult = null;

				HttpResponseStatus status = OK;
				String uri = request.getUri().substring(1);

				// List<Entry<String, String>> headers = request.getHeaders();
				List<Entry<String, String>> reponseHeader = new ArrayList<Entry<String, String>>();

				buf.setLength(0);

				HttpMethod reqMethod = request.getMethod();
				if (reqMethod.equals(HttpMethod.POST) || reqMethod.equals(HttpMethod.PUT) || reqMethod.equals(HttpMethod.DELETE)) {
					ChannelBuffer content = request.getContent();

					// invoke bo (async ??)

					buf.append("HelloNetty");

					if (content.readable()) {
						try {

						} catch (ConverterNotFoundException e1) {
							status = HttpResponseStatus.BAD_REQUEST;
							logger.error("convert fail : exception={}", e1.getMessage());
							buf.append(getResultString(status.getCode(), "Invalid parameter"));
						}
					} else {
						status = HttpResponseStatus.BAD_REQUEST;
						buf.append(getResultString(status.getCode(), "No content on request."));
					}
				} else if (reqMethod.equals(HttpMethod.GET)) {
					QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
					Map<String, List<String>> queries = queryStringDecoder.getParameters();

					buf.append("HelloNetty");

				} else {
					status = HttpResponseStatus.METHOD_NOT_ALLOWED;
					buf.append(getResultString(status.getCode(), "method not supports"));
				}

				// for demo
				accessArcus();
				accessNaver();
				accessPinPointDev();

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

			}
			channel.close();
		}
	}

	private String getResultString(int statusCode, String message) {
		return String.format("{\"statusCode\":%s,\"statusMessage\":\"%s\"}", statusCode, message);
	}

	private String getResultString(int statusCode, String message, String cause) {
		return String.format("{\"statusCode\":%s,\"statusMessage\":\"%s\",\"causeBy\":\"%s\"}", statusCode, message, cause);
	}
}
