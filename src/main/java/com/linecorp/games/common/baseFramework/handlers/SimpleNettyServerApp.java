package com.linecorp.games.common.baseFramework.handlers;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.testweb.util.AsyncHttpInvoker;
import com.ning.http.client.Response;

/**
 * <pre>
 * line game의 netty server lib을 참조해서 만든 서버 프로토타입
 * 
 * vm options for test
 * -javaagent:/Users/netspider/Documents/workspace_pinpoint/pinpoint-testbed/agent/pinpoint-bootstrap-1.0.2-SNAPSHOT.jar -Dpinpoint.agentId=netty.test.1 -Dpinpoint.applicationName=NETTY-HTTP-SERVER
 * </pre>
 * 
 * @author netspider
 * 
 */
public class SimpleNettyServerApp {

	static final Logger logger = LoggerFactory.getLogger(SimpleNettyServerApp.class);
	static final int SERVER_PORT = 2222;
	static final int CLIENT_COUNT = 1;

	public static void main(String[] args) {
		try {
			logger.info("TEST BEGIN");

			ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
			PipelineFactory factory = new PipelineFactory();
			bootstrap.setPipelineFactory(factory);
			bootstrap.bind(new InetSocketAddress(SERVER_PORT));

			final AsyncHttpInvoker invoker = new AsyncHttpInvoker();
			final CountDownLatch startLatch = new CountDownLatch(1);
			final CountDownLatch stopLatch = new CountDownLatch(CLIENT_COUNT);
			final ExecutorService executor = Executors.newCachedThreadPool();

			for (int i = 0; i < CLIENT_COUNT; i++) {
				executor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							startLatch.await();
							Response response = invoker.requestGet("http://localhost:" + SERVER_PORT, AsyncHttpInvoker.getDummyParams(), AsyncHttpInvoker.getDummyHeaders(), AsyncHttpInvoker.getDummyCookies());
							logger.info(response.getResponseBody());

							Response response2 = invoker.requestPost("http://localhost:" + SERVER_PORT, AsyncHttpInvoker.getDummyHeaders(), "I_AM_BODY");
							logger.info(response2.getResponseBody());
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							stopLatch.countDown();
						}
					}
				});
			}
			startLatch.countDown();
			stopLatch.await();

			// bootstrap.shutdown();
			// executor.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("TEST END. awaiting other requests.");
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
			}
		}
	}

	public static class PipelineFactory implements ChannelPipelineFactory {
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline p = Channels.pipeline();
			p.addLast("decoder", new HttpRequestDecoder());
			p.addLast("aggregator", new HttpChunkAggregator(65535));
			p.addLast("encoder", new HttpResponseEncoder());
			p.addLast("deflater", new HttpContentCompressor());
			p.addLast("handler", new HttpCustomServerHandler());
			return p;
		}
	}
}
