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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Connection {
    private final ConnectionFactory connectionFactory;
    private final SocketAddress remoteAddress;
    private final SocketOption socketOption;

    private final ChannelFactory channelFactory;

    private final ClientHandlerFactory clientHandlerFactory;

    private PinpointClientHandler pinpointClientHandler;
    private ChannelFuture connectFuture;

    public Connection(ConnectionFactory connectionFactory, SocketAddress remoteAddress, SocketOption socketOption, ChannelFactory channelFactory, ClientHandlerFactory clientHandlerFactory) {
        this.connectionFactory = Assert.requireNonNull(connectionFactory, "connectionFactory must not be null");

        this.remoteAddress = Assert.requireNonNull(remoteAddress, "remoteAddress must not be null");
        this.socketOption = Assert.requireNonNull(socketOption, "socketOption must not be null");


        this.channelFactory = Assert.requireNonNull(channelFactory, "channelFactory must not be null");
        this.clientHandlerFactory = Assert.requireNonNull(clientHandlerFactory, "clientHandlerFactory must not be null");
    }

    public void connect(boolean reconnect) {
        Assert.requireNonNull(remoteAddress, "remoteAddress must not be null");

        final CodecPipelineFactory pipelineFactory = new CodecPipelineFactory();
        final ChannelPipeline pipeline = pipelineFactory.newPipeline();

        Timer channelTimer = createTimer("Pinpoint-PinpointClientHandler-Timer");
        final ChannelHandler writeTimeout = new WriteTimeoutHandler(channelTimer, 3000, TimeUnit.MILLISECONDS);
        pipeline.addLast("writeTimeout", writeTimeout);

        this.pinpointClientHandler = this.clientHandlerFactory.newClientHandler(connectionFactory, channelTimer, reconnect);
        if (pinpointClientHandler instanceof SimpleChannelHandler) {
            pipeline.addLast("socketHandler", (SimpleChannelHandler)this.pinpointClientHandler);
        } else {
            throw new IllegalArgumentException("invalid pinpointClientHandler");
        }


        this.connectFuture  = connect0(pipeline);
    }

    private ChannelFuture connect0(ChannelPipeline pipeline) {
        // Set the options.
        final Channel ch = this.channelFactory.newChannel(pipeline);
        boolean success = false;
        try {
            ch.getConfig().setOptions(socketOption.toMap());
            success = true;
        } finally {
            if (!success) {
                ch.close();
            }
        }

        // Connect.
        return ch.connect(remoteAddress);
    }


    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public ChannelFuture getConnectFuture() {
        return connectFuture;
    }

    public PinpointClientHandler getPinpointClientHandler() {
        return pinpointClientHandler;
    }

    private static Timer createTimer(String timerName) {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer(timerName, 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }


    private PinpointClientHandler awaitConnected0() {
        ConnectFuture handlerConnectFuture = pinpointClientHandler.getConnectFuture();
        handlerConnectFuture.awaitUninterruptibly();

        if (ConnectFuture.Result.FAIL == handlerConnectFuture.getResult()) {
            throw new PinpointSocketException("connect fail to " + getRemoteAddress() + ".", connectFuture.getCause());
        }

        return pinpointClientHandler;
    }

    public PinpointClient awaitConnected() {
        PinpointClientHandler pinpointClientHandler = awaitConnected0();
        PinpointClient pinpointClient = new DefaultPinpointClient(pinpointClientHandler);
        return pinpointClient;
    }
}
