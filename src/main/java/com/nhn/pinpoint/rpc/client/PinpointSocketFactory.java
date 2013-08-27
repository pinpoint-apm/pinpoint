package com.nhn.pinpoint.rpc.client;


import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
    private Timer timer;
    // ping은 5분 주기
    private long pingDelay = 1000*60*5;


    public PinpointSocketFactory() {
        this(1, 1);
    }

    public PinpointSocketFactory(int bossCount, int workerCount) {
        if (bossCount < 1) {
            throw new IllegalArgumentException("bossCount is negative: " + bossCount);
        }
        // timer를 connect timeout으로 쓰므로 먼저 만들어야 됨.
        Timer timer = createTimer();
        ClientBootstrap bootstrap = createBootStrap(bossCount, workerCount, timer);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
    }

    private Timer createTimer() {
        ThreadFactory threadFactory = new PinpointThreadFactory("Pinpoint-SocketFactory-Timer", true);
        HashedWheelTimer timer = new HashedWheelTimer(threadFactory, ThreadNameDeterminer.CURRENT, 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
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
        bootstrap.setOption("sendBufferSize", 1024 * 64);
        bootstrap.setOption("receiveBufferSize", 1024 * 64);

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

    public long getPingDelay() {
        return pingDelay;
    }

    public void setPingDelay(long pingDelay) {
        this.pingDelay = pingDelay;
    }

    private ClientBootstrap createBootStrap(int bossCount, int workerCount, Timer timer) {
        // profiler, collector,
        logger.debug("createBootStrap boss:{}, worker:{}", bossCount, workerCount);
        NioClientSocketChannelFactory nioClientSocketChannelFactory = createChannelFactory(bossCount, workerCount, timer);
        return new ClientBootstrap(nioClientSocketChannelFactory);
    }

    private NioClientSocketChannelFactory createChannelFactory(int bossCount, int workerCount, Timer timer) {

        ExecutorService boss = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Client-Boss", true));
        NioClientBossPool bossPool = new NioClientBossPool(boss, bossCount, timer, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Client-Worker", true));
        NioWorkerPool workerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);
        return new NioClientSocketChannelFactory(bossPool, workerPool);
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

    void reconnect(final PinpointSocket pinpointSocket, final SocketAddress socketAddress) {
        ConnectEvent connectEvent = new ConnectEvent(pinpointSocket, socketAddress);
        reconnect(connectEvent);
        return;
    }

    private void reconnect(ConnectEvent connectEvent) {
        timer.newTimeout(connectEvent, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    private class ConnectEvent implements TimerTask {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final PinpointSocket pinpointSocket;
        private final SocketAddress socketAddress;

        private ConnectEvent(PinpointSocket pinpointSocket, SocketAddress socketAddress) {
            if (pinpointSocket == null) {
                throw new NullPointerException("pinpointSocket must not be null");
            }
            if (socketAddress == null) {
                throw new NullPointerException("socketAddress must not be null");
            }

            this.pinpointSocket = pinpointSocket;
            this.socketAddress = socketAddress;
        }

        @Override
        public void run(Timeout timeout) {

            logger.warn("try reconnect {}", socketAddress);
            final ChannelFuture channelFuture = connectAsync(socketAddress);
            Channel channel = channelFuture.getChannel();
            final SocketHandler socketHandler = getSocketHandler(channel);
            socketHandler.setPinpointSocketFactory(PinpointSocketFactory.this);
            socketHandler.setSocketAddress(socketAddress);
            socketHandler.setPinpointSocket(pinpointSocket);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        Channel channel = future.getChannel();
                        logger.warn("reconnect success {}, {}", socketAddress, channel);
                        socketHandler.open();
                        pinpointSocket.replaceSocketHandler(socketHandler);
                    } else {
                        if (!pinpointSocket.isClosed()) {
                            Throwable cause = future.getCause();
                            logger.warn("reconnect fail. {} Caused:{}", socketAddress, cause.getMessage(), cause);
                            reconnect(pinpointSocket, socketAddress);
                        } else {
                            logger.info("pinpointSocket is closed");
                        }
                    }
                }
            });
        }
    }

    void registerPing(final PinpointSocket socket) {
        if (socket.isClosed()) {
            return;
        }
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (socket.isClosed()) {
                    return;
                }
                socket.sendPing();
            }
        };
        timer.newTimeout(pingTask, pingDelay, TimeUnit.MILLISECONDS);
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
        Set<Timeout> stop = this.timer.stop();
//        stop 뭔가 취소를 해야 되나??
    }
}
