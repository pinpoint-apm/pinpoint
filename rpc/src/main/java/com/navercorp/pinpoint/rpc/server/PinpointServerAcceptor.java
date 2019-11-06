/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CpuUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.packet.ServerClosePacket;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.rpc.util.LoggerFactorySetup;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class PinpointServerAcceptor implements PinpointServerConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int WORKER_COUNT = CpuUtils.workerCount();

    private volatile boolean released;

    private ServerBootstrap bootstrap;

    private final ChannelFilter channelConnectedFilter;

    private Channel serverChannel;
    private final ChannelGroup channelGroup = new DefaultChannelGroup("PinpointServerFactory");

    private final PinpointServerChannelHandler nettyChannelHandler = new PinpointServerChannelHandler();

    private ServerMessageListenerFactory messageListenerFactory = new LoggingServerMessageListenerFactory();

    private ServerStreamChannelMessageHandler serverStreamChannelMessageHandler = ServerStreamChannelMessageHandler.DISABLED_INSTANCE;
    private List<ServerStateChangeEventHandler> stateChangeEventHandler = new ArrayList<ServerStateChangeEventHandler>();

    private final Timer healthCheckTimer;
    private final HealthCheckManager healthCheckManager;

    private final Timer requestManagerTimer;

    private final ServerOption serverOption;

    private final PipelineFactory pipelineFactory;

    static {
        LoggerFactorySetup.setupSlf4jLoggerFactory();
    }

    public PinpointServerAcceptor() {
        this(ServerOption.getDefaultServerOption(), ChannelFilter.BYPASS);
    }

    public PinpointServerAcceptor(ChannelFilter channelConnectedFilter) {
        this(ServerOption.getDefaultServerOption(), channelConnectedFilter);
    }

    public PinpointServerAcceptor(ChannelFilter channelConnectedFilter, PipelineFactory pipelineFactory) {
        this(ServerOption.getDefaultServerOption(), channelConnectedFilter, pipelineFactory);
    }

    public PinpointServerAcceptor(ServerOption serverOption, ChannelFilter channelConnectedFilter) {
        this(serverOption, channelConnectedFilter, new ServerCodecPipelineFactory());
    }

    public PinpointServerAcceptor(ServerOption serverOption, ChannelFilter channelConnectedFilter, PipelineFactory pipelineFactory) {
        ServerBootstrap bootstrap = createBootStrap(1, WORKER_COUNT);
        setOptions(bootstrap);
        this.bootstrap = bootstrap;

        this.serverOption = Assert.requireNonNull(serverOption, "serverOption");
        logger.info("serverOption : {}", serverOption);

        this.healthCheckTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-HealthCheckTimer", 50, TimeUnit.MILLISECONDS, 512);
        this.healthCheckManager = new HealthCheckManager(healthCheckTimer, serverOption.getHealthCheckPacketWaitTimeMillis(), channelGroup);

        this.requestManagerTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-RequestManager", 50, TimeUnit.MILLISECONDS, 512);

        this.channelConnectedFilter = Assert.requireNonNull(channelConnectedFilter, "channelConnectedFilter");

        this.pipelineFactory = Assert.requireNonNull(pipelineFactory, "pipelineFactory");
        addPipeline(bootstrap, pipelineFactory);
    }

    private ServerBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector
        ExecutorService boss = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Boss", true));
        NioServerBossPool nioServerBossPool = new NioServerBossPool(boss, bossCount, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Worker", true));
        NioWorkerPool nioWorkerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);

        NioServerSocketChannelFactory nioClientSocketChannelFactory = new NioServerSocketChannelFactory(nioServerBossPool, nioWorkerPool);
        return new ServerBootstrap(nioClientSocketChannelFactory);
    }

    private void setOptions(ServerBootstrap bootstrap) {
        // is read/write timeout necessary? don't need it because of NIO?
        // write timeout should be set through additional interceptor. write
        // timeout exists.

        // tcp setting
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // buffer setting
        bootstrap.setOption("child.sendBufferSize", 1024 * 64);
        bootstrap.setOption("child.receiveBufferSize", 1024 * 64);

        // bootstrap.setOption("child.soLinger", 0);
    }

    private void addPipeline(ServerBootstrap bootstrap, final PipelineFactory pipelineFactory) {
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipelineFactory.newPipeline();
                pipeline.addLast("handler", nettyChannelHandler);

                return pipeline;
            }
        });
    }

    @VisibleForTesting
    void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory == null) {
            throw new NullPointerException("channelPipelineFactory");
        }
        bootstrap.setPipelineFactory(channelPipelineFactory);
    }

    @VisibleForTesting
    public void setMessageHandler(final ChannelHandler messageHandler) {
        Assert.requireNonNull(messageHandler, "messageHandler");
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipelineFactory.newPipeline();
                pipeline.addLast("handler", messageHandler);

                return pipeline;
            }
        });
    }

    public void bind(String host, int port) throws PinpointSocketException {
        InetSocketAddress bindAddress = new InetSocketAddress(host, port);
        bind(bindAddress);
    }

    public void bind(InetSocketAddress bindAddress) throws PinpointSocketException {
        if (released) {
            return;
        }

        logger.info("bind() {}", bindAddress);
        this.serverChannel = bootstrap.bind(bindAddress);
        healthCheckManager.start(serverOption.getHealthCheckIntervalTimeMillis());
    }

    private DefaultPinpointServer createPinpointServer(Channel channel) {
        DefaultPinpointServer pinpointServer = new DefaultPinpointServer(channel, this);
        return pinpointServer;
    }

    @Override
    public long getDefaultRequestTimeout() {
        return serverOption.getRequestTimeoutMillis();
    }

    @Override
    public ServerMessageListener getMessageListener() {
        return messageListenerFactory.create();
    }

    public void setMessageListenerFactory(ServerMessageListenerFactory messageListenerFactory) {
        this.messageListenerFactory = Assert.requireNonNull(messageListenerFactory, "messageListenerFactory");
    }

    @Override
    public List<ServerStateChangeEventHandler> getStateChangeEventHandlers() {
        return stateChangeEventHandler;
    }

    public void addStateChangeEventHandler(ServerStateChangeEventHandler stateChangeEventHandler) {
        Assert.requireNonNull(stateChangeEventHandler, "stateChangeEventHandler");

        this.stateChangeEventHandler.add(stateChangeEventHandler);
    }

    @Override
    public ServerStreamChannelMessageHandler getServerStreamMessageHandler() {
        return serverStreamChannelMessageHandler;
    }

    public void setServerStreamChannelMessageHandler(ServerStreamChannelMessageHandler serverStreamChannelMessageHandler) {
        this.serverStreamChannelMessageHandler = Assert.requireNonNull(serverStreamChannelMessageHandler, "serverStreamChannelMessageHandler");
    }

    @Override
    public Timer getRequestManagerTimer() {
        return requestManagerTimer;
    }

    @Override
    public ClusterOption getClusterOption() {
        return serverOption.getClusterOption();
    }

    public void close() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }
        healthCheckManager.stop();
        healthCheckTimer.stop();
        
        closePinpointServer();

        if (serverChannel != null) {
            ChannelFuture close = serverChannel.close();
            close.awaitUninterruptibly(serverOption.getServerCloseWaitTimeoutMillis(), TimeUnit.MILLISECONDS);
            serverChannel = null;
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
            bootstrap = null;
        }

        // clear the request first and remove timer
        requestManagerTimer.stop();
    }
    
    private void closePinpointServer() {
        for (Channel channel : channelGroup) {
            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();

            if (pinpointServer != null) {
                pinpointServer.sendClosePacket();
            }
        }
    }
    
    public List<PinpointSocket> getWritableSocketList() {
        List<PinpointSocket> pinpointServerList = new ArrayList<PinpointSocket>();

        for (Channel channel : channelGroup) {
            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();
            if (pinpointServer != null && pinpointServer.isEnableDuplexCommunication()) {
                pinpointServerList.add(pinpointServer);
            }
        }

        return pinpointServerList;
    }

    class PinpointServerChannelHandler extends SimpleChannelHandler {
        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            final Channel channel = e.getChannel();
            logger.info("channelConnected started. channel:{}", channel);

            if (released) {
                logger.warn("already released. channel:{}", channel);
                channel.write(new ServerClosePacket()).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.getChannel().close();
                    }
                });
                return;
            }

            final boolean accept = channelConnectedFilter.accept(channel);
            if (!accept) {
                logger.debug("channelConnected() channel discard. {}", channel);
                return;
            }

            DefaultPinpointServer pinpointServer = createPinpointServer(channel);
            
            channel.setAttachment(pinpointServer);
            channelGroup.add(channel);

            pinpointServer.start();

            super.channelConnected(ctx, e);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            Channel channel = e.getChannel();
            final boolean accept = channelConnectedFilter.accept(channel);
            if (!accept) {
                return;
            } else {
                super.exceptionCaught(ctx, e);
            }
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            final Channel channel = e.getChannel();

            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();
            if (pinpointServer != null) {
                pinpointServer.stop(released);
            }

            super.channelDisconnected(ctx, e);
        }

        // ChannelClose event may also happen when the other party close socket
        // first and Disconnected occurs
        // Should consider that.
        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            final Channel channel = e.getChannel();

            channelGroup.remove(channel);

            super.channelClosed(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            final Channel channel = e.getChannel();

            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();
            if (pinpointServer != null) {
                Object message = e.getMessage();

                pinpointServer.messageReceived(message);
            }

            super.messageReceived(ctx, e);
        }
    }

}
