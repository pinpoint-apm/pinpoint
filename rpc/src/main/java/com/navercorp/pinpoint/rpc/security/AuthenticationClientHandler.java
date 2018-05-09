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

package com.navercorp.pinpoint.rpc.security;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.packet.ControlConnectionHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlConnectionHandshakeResponsePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationClientHandler should be created for each channel and never be shared.
 *
 * @author Taejin Koo
 */
public class AuthenticationClientHandler extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationStateContext state = new AuthenticationStateContext();

    private final AuthenticationManager authenticationManager;

    private final byte[] handshakePayload;

    private final AuthenticationAwaitTaskManager expiredManager;

    public AuthenticationClientHandler(AuthenticationManager authenticationManager, byte[] handshakePayload, AuthenticationAwaitTaskManager expiredManager) {
        this.authenticationManager = Assert.requireNonNull(authenticationManager, "authenticationManager must not be null");
        this.handshakePayload = Assert.requireNonNull(handshakePayload, "handshakePayload must not be null");
        this.expiredManager = Assert.requireNonNull(expiredManager, "expiredManager must not be null");
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        final Channel channel = ctx.getChannel();
        if (state.changeStateProgress()) {
            ChannelFuture future = Channels.future(channel);

            ControlConnectionHandshakePacket handshakePacket = new ControlConnectionHandshakePacket(handshakePayload);
            ctx.sendDownstream(new DownstreamMessageEvent(channel, future, handshakePacket, channel.getRemoteAddress()));

            expiredManager.registerAwaitTask(ctx, event);
        } else {
            throw new IllegalArgumentException("illegal state");
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (state.isCompleted()) {
            handleInCompleted(ctx, event);
        } else {
            if (state.isInProgress()) {
                handle(ctx, event);
            } else {
                if (state.isCompleted()) {
                    handleInCompleted(ctx, event);
                } else {
                    // abnormal situation : is unable to send data before authentication completed.
                    logger.warn("message will be discarded. cause:authentication is in progress.channel:{}, message:{}", event.getChannel(), event);
                }
            }
        }
    }

    private void handleInCompleted(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (state.isSucceeded()) {
            ctx.sendUpstream(event);
        } else {
            throw new IllegalArgumentException("illegal state");
        }
    }

    private void handle(ChannelHandlerContext ctx, MessageEvent event) {
        Channel channel = ctx.getChannel();
        Object message = event.getMessage();

        if (message instanceof ControlConnectionHandshakeResponsePacket) {
            ControlConnectionHandshakeResponsePacket responsePacket = (ControlConnectionHandshakeResponsePacket) message;

            Authentication authentication = new Authentication(responsePacket.getPayload());

            AuthenticationResult result = authenticationManager.authenticate(authentication, ctx);
            if (result.isSuccess()) {
                logger.info("success authentication. channel:{}", channel);
                state.changeStateSuccess();

                ctx.getPipeline().remove(this);
            } else {
                state.changeStateFail();
                logger.warn("connection will be disabled. cause:authentication failed. channel:{}", channel);
            }
            expiredManager.done(channel, result.isSuccess());
        } else {
            // abnormal situation : is unable to send data before authentication completed.
            logger.warn("message will be discarded. cause:authentication is in progress.channel:{}, message:{}", event.getChannel(), event);
        }
    }

    public boolean isAuthenticated() {
        return state.isSucceeded();
    }

}
