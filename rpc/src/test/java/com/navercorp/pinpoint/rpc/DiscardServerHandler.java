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


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DiscardServerHandler extends SimpleChannelUpstreamHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private long transferredBytes;

    public long getTransferredBytes() {
        return transferredBytes;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.debug("event:{}", e);
        }

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

        transferredBytes += ((ChannelBuffer) e.getMessage()).readableBytes();
        logger.debug("messageReceived. meg:{} channel:{}", e.getMessage(), e.getChannel());
        logger.debug("transferredBytes. transferredBytes:{}", transferredBytes);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from downstream. Caused:{}", e, e.getCause());
    }
}
