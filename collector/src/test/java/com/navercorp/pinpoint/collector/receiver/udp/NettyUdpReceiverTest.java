/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.udp;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * @author emeroad
 */
@Ignore
public class NettyUdpReceiverTest {

    public static final int PORT = SocketUtils.findAvailableUdpPort(30011);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    CountDownLatch latch = new CountDownLatch(1);
    /*
    * netty io thread is single-threaded even for udp.
    * this is for running multiple workers.
    * */
    @Test
    public void server() throws IOException, InterruptedException {

        final ConnectionlessBootstrap udpServer = createUdpServer();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                udpServer.bind(new InetSocketAddress("127.0.0.1", PORT));
                try {
                    logger.debug("server-await");
                    latch.await();
                } catch (InterruptedException ignored) {
                }
                logger.debug("server-shutdown");
                udpServer.shutdown();
            }
        });
        thread.start();
        Thread.sleep(1000);
        logger.debug("start--------");
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        for (int i =0; i< 10; i++) {
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
                        start();
//                    } catch (IOException e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    }
//                }
//            });
//        }
//        executorService.awaitTermination(120, TimeUnit.SECONDS) ;


        latch.countDown();

    }

    private void start() throws IOException, InterruptedException {
        DatagramSocket so = new DatagramSocket();
        so.connect(new InetSocketAddress("127.0.0.1", PORT));
        int count = 1000;
        for (int i = 0 ; i< count; i++) {
            byte[] bytes = new byte[100];
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
            so.send(datagramPacket);
            Thread.sleep(10);
        }
        so.close();
    }

    private ConnectionlessBootstrap createUdpServer() {
        DatagramChannelFactory udpFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool(), 4);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("test", new SimpleChannelHandler() {
                    @Override
                    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                        String name = Thread.currentThread().getName();
                        logger.debug("sleep:{}", name);
                        Thread.sleep(10000);
//                        if (!name.equals("New I/O worker #1")) {
                            logger.debug("messageReceived thread-{} message:", Thread.currentThread().getName());
//                        }
                    }
                });
                return pipeline;
            }
        };
        ConnectionlessBootstrap udpBootstrap = new ConnectionlessBootstrap(udpFactory);
        udpBootstrap.setPipelineFactory(pipelineFactory);
        return udpBootstrap;
    }
}
