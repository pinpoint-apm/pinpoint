package com.nhn.pinpoint.common.io.rpc;


import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class PinpointSocketFactory {

    public static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private volatile boolean released;
    private ClientBootstrap bootstrap;


    public PinpointSocketFactory() {
        ClientBootstrap bootstrap = createBootStrap(1, 1);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
    }

    private void addPipeline(ClientBootstrap bootstrap) {
        SocketClientPipelineFactory socketClientPipelineFactory = new SocketClientPipelineFactory();
        bootstrap.setPipelineFactory(socketClientPipelineFactory);
    }

    private void setOptions(ClientBootstrap bootstrap) {
        // connectTimeout
        bootstrap.setOption(CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT);
        // read write timeout이 있어야 되나? nio라서 없어도 되던가?

        // tcp 세팅
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        // buffer
        bootstrap.setOption("sendBufferSize", 4096 * 2);
        bootstrap.setOption("receiveBufferSize", 4096 * 2);
    }

    public void setConnectTimeout(int connectTimeout) {
        bootstrap.setOption(CONNECT_TIMEOUT_MILLIS, connectTimeout);
    }

    public int getConnectTimeout() {
        return (Integer) bootstrap.getOption(CONNECT_TIMEOUT_MILLIS);
    }

    private ClientBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector,
        ExecutorService boss = Executors.newFixedThreadPool(bossCount);
        ExecutorService worker = Executors.newFixedThreadPool(workerCount);
        NioClientSocketChannelFactory nioClientSocketChannelFactory = new NioClientSocketChannelFactory(boss, worker);
        return new ClientBootstrap(nioClientSocketChannelFactory);
    }


    public PinpointSocket connect(String host, int port) throws PinpointSocketException {
        InetSocketAddress address = new InetSocketAddress(host, port);
        ChannelFuture connectFuture = bootstrap.connect(address);

        final PinpointSocket pinpointSocket = new PinpointSocket();
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = future.getChannel();
                    ChannelPipeline pipeline = channel.getPipeline();
                    pipeline.addLast("requestHandler", pinpointSocket.getSocketRequestHandler());
                }
            }
        });
        // connectTimeout이 있어서 그냥 기다리면됨.
        connectFuture.awaitUninterruptibly();
        if (!connectFuture.isSuccess()) {
            throw new PinpointSocketException("connect fail.", connectFuture.getCause());
        }
        Channel channel = connectFuture.getChannel();
        pinpointSocket.setChannel(channel);
        pinpointSocket.open();
        return pinpointSocket;
    }


    public void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }

        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
    }
}
