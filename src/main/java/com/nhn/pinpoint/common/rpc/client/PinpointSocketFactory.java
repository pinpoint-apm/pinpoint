package com.nhn.pinpoint.common.rpc.client;


import com.nhn.pinpoint.common.rpc.PinpointSocketException;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 */
public class PinpointSocketFactory {

    public static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private volatile boolean released;
    private ClientBootstrap bootstrap;

    private long reconnectDelay = 3000;
    private ScheduledExecutorService reconnector;


    public PinpointSocketFactory() {
        ClientBootstrap bootstrap = createBootStrap(1, 1);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
        this.reconnector = Executors.newScheduledThreadPool(1, PinpointThreadFactory.createThreadFactory("socket-reconnector", true));
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
        ExecutorService boss = Executors.newFixedThreadPool(bossCount, PinpointThreadFactory.createThreadFactory("socket-boss", true));
        ExecutorService worker = Executors.newFixedThreadPool(workerCount, PinpointThreadFactory.createThreadFactory("socket-worker", true));
        NioClientSocketChannelFactory nioClientSocketChannelFactory = new NioClientSocketChannelFactory(boss, worker);
        return new ClientBootstrap(nioClientSocketChannelFactory);
    }

    public PinpointSocket connect(String host, int port) throws PinpointSocketException {
        SocketAddress address = new InetSocketAddress(host, port);
        SocketHandler pinpoint = connectSocketHandler(address);

        PinpointSocket pinpointSocket = new PinpointSocket(pinpoint);
        return pinpointSocket;
    }


    SocketHandler connectSocketHandler(String host, int port) throws PinpointSocketException {
        if (host == null) {
            throw new NullPointerException("host");
        }
        SocketAddress address = new InetSocketAddress(host, port);
        return connectSocketHandler0(address);
    }

    SocketHandler connectSocketHandler(SocketAddress address) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        return connectSocketHandler0(address);
    }

    public SocketHandler connectSocketHandler0(SocketAddress address) {
        ChannelFuture connectFuture = bootstrap.connect(address);
        connectFuture.awaitUninterruptibly();
        if (!connectFuture.isSuccess()) {
            connectFuture.getChannel().close();
            throw new PinpointSocketException("connect fail.", connectFuture.getCause());
        }
        Channel channel = connectFuture.getChannel();
        SocketHandler socketHandler = getSocketHandler(channel);
        socketHandler.setPinpointSocketFactory(this);
        socketHandler.setSocketAddress(address);
        socketHandler.open();
        return socketHandler;
    }

    private SocketHandler getSocketHandler(Channel channel) {
        return (SocketHandler) channel.getAttachment();
    }

    ChannelFuture connectAsync(SocketAddress address) {
        return bootstrap.connect(address);
    }

    public void reconnect(final PinpointSocket pinpointSocket, final SocketAddress socketAddress) {
        ReconnectEvent reconnectEvent = new ReconnectEvent(pinpointSocket, socketAddress);
        reconnect(reconnectEvent);
        return;
    }

    private void reconnect(ReconnectEvent reconnectEvent) {
        reconnector.schedule(reconnectEvent, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    private class ReconnectEvent implements Runnable {

        private ReconnectEvent(PinpointSocket pinpointSocket, SocketAddress socketAddress) {
            this.pinpointSocket = pinpointSocket;
            this.socketAddress = socketAddress;
        }

        private Logger logger = LoggerFactory.getLogger(getClass());
        private final PinpointSocket pinpointSocket;
        private final SocketAddress socketAddress;

        @Override
        public void run() {

            logger.warn("try reconnect {}", socketAddress);

            final ChannelFuture channelFuture = connectAsync(socketAddress);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.warn("try reconnect {}", socketAddress);
                    if (future.isSuccess()) {
                        Channel channel = future.getChannel();
                        logger.warn("reconnect success {}, {}", socketAddress, channel);
                        SocketHandler socketHandler = getSocketHandler(channel);
                        socketHandler.setPinpointSocketFactory(PinpointSocketFactory.this);
                        pinpointSocket.replaceSocketHandler(socketHandler);
                    } else {
                        ReconnectEvent reconnectEvent = new ReconnectEvent(pinpointSocket, socketAddress);
                        logger.warn("reconnect fail. {} Sleep(3000) Caused:{}", new Object[]{socketAddress, future.getCause().getMessage(), future.getCause()});
                        reconnect(reconnectEvent);
                    }
                }
            });
        }
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
        List<Runnable> reconnecExevent = this.reconnector.shutdownNow();
        // 추가 처리 필요.

    }
}
