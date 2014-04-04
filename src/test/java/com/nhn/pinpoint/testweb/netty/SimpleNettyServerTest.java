package com.nhn.pinpoint.testweb.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.testweb.util.AsyncHttpInvoker;
import com.ning.http.client.Response;

/**
 * line game의 netty server lib을 참조해서 만든 서버 프로토타입
 * 
 * @author netspider
 * 
 */
public class SimpleNettyServerTest {

	static final Logger logger = LoggerFactory.getLogger(SimpleNettyServerTest.class);
	static final int SERVER_PORT = 2222;
	static final int CLIENT_COUNT = 10;

	@Test
	public void server() throws IOException, InterruptedException {
		logger.info("TEST BEGIN");

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool() ));
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
						Assert.assertEquals(200, response.getStatusCode());
						Assert.assertEquals("HelloNetty", response.getResponseBody());
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

		logger.info("TEST END");
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
