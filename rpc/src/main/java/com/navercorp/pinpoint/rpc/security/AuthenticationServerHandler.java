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

import com.navercorp.pinpoint.rpc.packet.ControlConnectionHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlConnectionHandshakeResponsePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationServerHandler should be created for each channel and never be shared.
 *
 * @author Taejin Koo
 */
public class AuthenticationServerHandler extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationStateContext state = new AuthenticationStateContext();

    private final AuthenticationManager authenticationManager;

    public AuthenticationServerHandler(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (state.isCompleted()) {
            handleInCompleted(ctx, event);
        } else {
            if (state.changeStateProgress()) {
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

    private void handleInCompleted(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (state.isSucceeded()) {
            super.messageReceived(ctx, e);
        } else {
            throw new IllegalArgumentException("illegal state");
        }
    }

    private void handle(ChannelHandlerContext ctx, MessageEvent event) {
        Channel channel = ctx.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("handle() AuthenticationServerHandler started. channel:{}", channel);
        }

        Object message = event.getMessage();
        if (message instanceof ControlConnectionHandshakePacket) {
            ControlConnectionHandshakePacket handshakeMessage = (ControlConnectionHandshakePacket) message;

            Authentication authentication = new Authentication(handshakeMessage.getPayload());

            AuthenticationResult result = authenticationManager.authenticate(authentication, ctx);
            if (result.isSuccess()) {
                state.changeStateSuccess();
                ctx.getPipeline().remove(this);
            } else {
                state.changeStateFail();
            }

            ChannelFuture future = Channels.future(channel);
            ControlConnectionHandshakeResponsePacket responsePacket = new ControlConnectionHandshakeResponsePacket(result.getResult());
            ctx.sendDownstream(new DownstreamMessageEvent(channel, future, responsePacket, channel.getRemoteAddress()));

            if (state.isFailed()) {
                logger.warn("connection will be disabled. cause:authentication failed. channel:{}", channel);
                channel.close();
            }
        } else {
            logger.warn("connection will be disabled. cause:authentication failed. channel:{}", channel);
            channel.close();
        }
    }

    public boolean isAuthenticated() {
        return state.isSucceeded();
    }

}
