/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc;


import org.jboss.netty.channel.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author emeroad
 */
public class DiscardServerHandler extends SimpleChannelUpstreamHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private int messageReceivedCount = 0;

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent event) throws Exception {
        if (event instanceof ChannelStateEvent) {
            logger.debug("event:{}", event);
        } else if (event instanceof MessageEvent) {
            messageReceived(ctx, (MessageEvent) event);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
        messageReceivedCount++;

        try {
            logger.debug("messageReceived. meg:{} channel:{}", event.getMessage(), event.getChannel());
        } catch (Exception e) {
            logger.warn("catch exception. message:{}", e.getMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from downstream. Caused:{}", e, e.getCause());
    }

    public int getMessageReceivedCount() {
        return messageReceivedCount;
    }

}
