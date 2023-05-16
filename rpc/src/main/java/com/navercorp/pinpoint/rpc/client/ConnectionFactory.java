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

import java.util.Objects;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConnectionFactory {
    private final Timer connectTimer;
    private final Closed closed;
    private final ChannelFactory channelFactory;
    private final ClientHandlerFactory clientHandlerFactory;
    private final SocketOption socketOption;
    private final ClientOption clientOption;
    private final PipelineFactory pipelineFactory;

    ConnectionFactory(Timer connectTimer, Closed closed, ChannelFactory channelFactory,
                             SocketOption socketOption, ClientOption clientOption, ClientHandlerFactory clientHandlerFactory, PipelineFactory pipelineFactory) {

        this.connectTimer = Objects.requireNonNull(connectTimer, "connectTimer");
        this.closed = Objects.requireNonNull(closed, "release");

        this.channelFactory = Objects.requireNonNull(channelFactory, "channelFactory");

        this.socketOption = Objects.requireNonNull(socketOption, "option");
        this.clientOption = Objects.requireNonNull(clientOption, "connectTimer");
        this.clientHandlerFactory = Objects.requireNonNull(clientHandlerFactory, "clientHandlerFactory");
        this.pipelineFactory = Objects.requireNonNull(pipelineFactory, "pipelineFactory");
    }

    public boolean isClosed() {
        return closed.isClosed();
    }

    public Connection connect(SocketAddressProvider remoteAddressProvider, boolean reconnect) {
        Connection connection = new Connection(this, this.socketOption, this.channelFactory, clientHandlerFactory);
        connection.connect(remoteAddressProvider, reconnect, pipelineFactory);
        return connection;
    }

    public void reconnect(final PinpointClient pinpointClient, final SocketAddressProvider socketAddressProvider) {
        ConnectEvent connectEvent = new ConnectEvent(this, socketAddressProvider, pinpointClient);
        this.connectTimer.newTimeout(connectEvent, clientOption.getReconnectDelay(), TimeUnit.MILLISECONDS);
    }

    private static class ConnectEvent implements TimerTask {

        private final Logger logger = LogManager.getLogger(getClass());

        private final ConnectionFactory connectionFactory;
        private final SocketAddressProvider socketAddressProvider;

        private final PinpointClient pinpointClient;


        private ConnectEvent(ConnectionFactory connectionFactory, SocketAddressProvider socketAddressProvider, PinpointClient pinpointClient) {
            this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory");
            this.socketAddressProvider = Objects.requireNonNull(socketAddressProvider, "socketAddressProvider");
            this.pinpointClient = Objects.requireNonNull(pinpointClient, "pinpointClient");
        }

        @Override
        public void run(Timeout timeout) {
            if (timeout.isCancelled()) {
                return;
            }

            // Just return not to try reconnection when event has been fired but pinpointClient already closed.
            if (pinpointClient.isClosed()) {
                logger.debug("pinpointClient is already closed.");
                return;
            }
            logger.warn("try reconnect. connectAddress:{}", socketAddressProvider);

            final Connection connection = connectionFactory.connect(socketAddressProvider, true);

            final PinpointClientHandler pinpointClientHandler = connection.getPinpointClientHandler();
            pinpointClientHandler.setPinpointClient(pinpointClient);

            ChannelFuture channelFuture = connection.getConnectFuture();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        Channel channel = future.getChannel();
                        logger.info("reconnect success {}, {}", socketAddressProvider, channel);
                        pinpointClient.reconnectSocketHandler(pinpointClientHandler);
                    } else {
                        if (!pinpointClient.isClosed()) {

                         /*
                            // comment out because exception message can be taken at exceptionCaught
                            if (logger.isWarnEnabled()) {
                                Throwable cause = future.getCause();
                                logger.warn("reconnect fail. {} Caused:{}", socketAddress, cause.getMessage());
                            }
                          */
                            connectionFactory.reconnect(pinpointClient, socketAddressProvider);
                        } else {
                            logger.info("pinpointClient is closed. stop reconnect.");
                        }
                    }
                }
            });


        }
    }
}
