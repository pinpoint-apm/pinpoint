package com.nhn.pinpoint.common.rpc.client;


import com.nhn.pinpoint.common.rpc.PinpointSocketException;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class PinpointSocketFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private volatile boolean released;
    private ClientBootstrap bootstrap;

    private long reconnectDelay = 3000;
    private HashedWheelTimer reconnector;


    public PinpointSocketFactory() {
        ClientBootstrap bootstrap = createBootStrap(1, 2);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
        this.reconnector = new HashedWheelTimer(100, TimeUnit.MILLISECONDS);
        this.reconnector.start();
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

    public long getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(long reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    private ClientBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector,
        logger.debug("createBootStrap boss:{}, worker:{}", bossCount, workerCount);
        ExecutorService boss = Executors.newFixedThreadPool(bossCount, PinpointThreadFactory.createThreadFactory("socket-boss", true));
        ExecutorService worker = Executors.newFixedThreadPool(workerCount, PinpointThreadFactory.createThreadFactory("socket-worker", true));
        NioClientSocketChannelFactory nioClientSocketChannelFactory = new NioClientSocketChannelFactory(boss, worker);
        return new ClientBootstrap(nioClientSocketChannelFactory);
    }

    public PinpointSocket connect(String host, int port) throws PinpointSocketException {
        SocketAddress address = new InetSocketAddress(host, port);
        SocketHandler socketHandler = connectSocketHandler(address);

        PinpointSocket pinpointSocket = new PinpointSocket(socketHandler);
        return pinpointSocket;
    }

    public PinpointSocket scheduledConnect(String host, int port) {
        SocketAddress address = new InetSocketAddress(host, port);

        PinpointSocket pinpointSocket = new PinpointSocket();
        reconnect(pinpointSocket, address);

        return pinpointSocket;
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
        ConnectEvent connectEvent = new ConnectEvent(pinpointSocket, socketAddress);
        reconnect(connectEvent);
        return;
    }

    private void reconnect(ConnectEvent connectEvent) {
        reconnector.newTimeout(connectEvent, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    private class ConnectEvent implements TimerTask {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final PinpointSocket pinpointSocket;
        private final SocketAddress socketAddress;

        private ConnectEvent(PinpointSocket pinpointSocket, SocketAddress socketAddress) {
            this.pinpointSocket = pinpointSocket;
            this.socketAddress = socketAddress;
        }

        @Override
        public void run(Timeout timeout) {

            logger.warn("try reconnect {}", socketAddress);

            final ChannelFuture channelFuture = connectAsync(socketAddress);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        Channel channel = future.getChannel();
                        logger.warn("reconnect success {}, {}", socketAddress, channel);
                        SocketHandler socketHandler = getSocketHandler(channel);
                        socketHandler.setPinpointSocketFactory(PinpointSocketFactory.this);
                        socketHandler.open();
                        pinpointSocket.replaceSocketHandler(socketHandler);
                    } else {
                        future.getChannel().close();
                        if (!pinpointSocket.isClosed()) {
                            logger.warn("reconnect fail. {} Caused:{}", new Object[]{socketAddress, future.getCause().getMessage(), future.getCause()});
                            reconnect(pinpointSocket, socketAddress);
                        }
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
        Set<Timeout> stop = this.reconnector.stop();
//        stop 뭔가 취소를 해야 되나??
    }
}
