package com.nhn.pinpoint.collector.receiver.udp;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final int PORT = 30011;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    CountDownLatch latch = new CountDownLatch(1);
    /*
    * netty io thread는 udp에서도 single뿐이 안됨.
    * worker를 multi로 돌려서 하라고 되있음.
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
                } catch (InterruptedException e) {
                }
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
        int count = 100000;
        for (int i = 0 ; i< count; i++) {
            byte[] bytes = new byte[100];
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
            so.send(datagramPacket);
            Thread.sleep(100);
        }
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
                        logger.debug("sleep-------------------{}", name);
                        Thread.sleep(10000);
//                        if (!name.equals("New I/O worker #1")) {
                            logger.info("messageReceived thread-{} message:", Thread.currentThread().getName());
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
