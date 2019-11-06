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

package com.navercorp.pinpoint.plugin.netty;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author jaehong.kim
 */
public class NettyClientRequestWrapper implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpMessage httpMessage;
    private final ChannelHandlerContext channelHandlerContext;

    public NettyClientRequestWrapper(final HttpMessage httpMessage, final ChannelHandlerContext channelHandlerContext) {
        this.httpMessage = Assert.requireNonNull(httpMessage, "httpMessage");
        this.channelHandlerContext = channelHandlerContext;
    }


    @Override
    public String getDestinationId() {
        if (this.channelHandlerContext != null) {
            final Channel channel = this.channelHandlerContext.channel();
            if (channel != null) {
                return NettyUtils.getEndPoint(channel.remoteAddress());
            }
        }
        return "Unknown";
    }

    @Override
    public String getUrl() {
        if (this.httpMessage instanceof HttpRequest) {
            return ((HttpRequest) httpMessage).getUri();
        }
        return null;
    }

}