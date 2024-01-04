/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.netty;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"io.netty:netty-all:[4.1.0.Final,4.1.max]", WebServer.VERSION, PluginITConstants.VERSION})
@PinpointConfig("pinpoint-netty-plugin-test.config")
public class NettyIT {

    private static WebServer webServer;

    @BeforeAll
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @AfterAll
    public static void AfterClass() {
        webServer = WebServer.cleanup(webServer);
    }

    @Test
    public void listenerTest() throws Exception {
        final CountDownLatch awaitLatch = new CountDownLatch(1);

        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        Bootstrap bootstrap = client(workerGroup);
        Channel channel = bootstrap.connect(webServer.getHostname(), webServer.getListeningPort()).sync().channel();
        try {
            channel.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
                    awaitLatch.countDown();
                }
            });

            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            channel.writeAndFlush(request);

            boolean await = awaitLatch.await(3000, TimeUnit.MILLISECONDS);
//            Assertions.assertTrue(await);
//
//            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
//            verifier.printCache();
//
//            verifier.verifyTrace(event("NETTY", Bootstrap.class.getMethod("connect", SocketAddress.class), annotation("netty.address", webServer.getHostAndPort())));
//            verifier.verifyTrace(event("NETTY", "io.netty.channel.DefaultChannelPipeline.writeAndFlush(java.lang.Object)"));
//            verifier.verifyTrace(event("ASYNC", "Asynchronous Invocation"));
//            verifier.verifyTrace(event("NETTY_HTTP", "io.netty.handler.codec.http.HttpObjectEncoder.encode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)", annotation("http.url", "/")));
        } finally {
            channel.close().sync();
            workerGroup.shutdown();
        }
    }

    @Test
    public void writeTest() throws Exception {
        final CountDownLatch awaitLatch = new CountDownLatch(1);

        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        Bootstrap bootstrap = client(workerGroup);
        final ChannelFuture connect = bootstrap.connect(webServer.getHostname(), webServer.getListeningPort());
        connect.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    channel.pipeline().addLast(new SimpleChannelInboundHandler() {

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                            awaitLatch.countDown();
                        }

                    });
                    HttpRequest request = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
                    future.channel().writeAndFlush(request);
                }
            }

        });

        boolean await = awaitLatch.await(3000, TimeUnit.MILLISECONDS);
//        Assertions.assertTrue(await);

        final Channel channel = connect.channel();
        try {
//            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
//            verifier.printCache();
//
//            verifier.verifyTrace(event("NETTY", Bootstrap.class.getMethod("connect", SocketAddress.class), annotation("netty.address", webServer.getHostAndPort())));
//            verifier.verifyTrace(event("NETTY", "io.netty.channel.DefaultChannelPromise.addListener(io.netty.util.concurrent.GenericFutureListener)"));
//            verifier.verifyTrace(event("ASYNC", "Asynchronous Invocation"));
//            verifier.verifyTrace(event("NETTY_INTERNAL", "io.netty.util.concurrent.DefaultPromise.notifyListenersNow()"));
//            verifier.verifyTrace(event("NETTY_INTERNAL", "io.netty.util.concurrent.DefaultPromise.notifyListener0(io.netty.util.concurrent.Future, io.netty.util.concurrent.GenericFutureListener)"));
//            verifier.verifyTrace(event("NETTY", "io.netty.channel.DefaultChannelPipeline.writeAndFlush(java.lang.Object)"));
//            verifier.verifyTrace(event("NETTY_HTTP", "io.netty.handler.codec.http.HttpObjectEncoder.encode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)", annotation("http.url", "/")));
        } finally {
            channel.close().sync();
            workerGroup.shutdown();
        }
    }

    public Bootstrap client(EventLoopGroup workerGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(65535));
                    }
                });
        return bootstrap;
    }

}
