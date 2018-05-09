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
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class AuthenticationAwaitTaskManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Timer timer;
    private final long awaitTimeout;

    private final ConcurrentHashMap<Channel, DefaultFuture<Boolean>> futureMap = new ConcurrentHashMap<Channel, DefaultFuture<Boolean>>();

    public AuthenticationAwaitTaskManager(Timer timer, long awaitTimeout) {
        this.timer = Assert.requireNonNull(timer, "timer must not be null");
        Assert.isTrue(awaitTimeout > 0, "awaitTimeout must be greater than 0");
        this.awaitTimeout = awaitTimeout;
    }

    boolean registerAwaitTask(final ChannelHandlerContext ctx, final ChannelStateEvent event) {
        Assert.requireNonNull(ctx, "ctx must not be null");
        Assert.requireNonNull(event, "event must not be null");

        Channel channel = ctx.getChannel();
        if (channel == null) {
            logger.warn("channel must not be null");
            return false;
        }

        DefaultFuture<Boolean> future = new DefaultFuture<Boolean>(awaitTimeout);
        DefaultFuture<Boolean> old = futureMap.putIfAbsent(channel, future);
        if (old != null) {
            logger.warn("future already exist(channel:{})", channel);
            return false;
        }

        future.setListener(new FutureListener<Boolean>() {
            @Override
            public void onComplete(Future<Boolean> future) {
                if (future.isSuccess()) {
                    if (future.getResult()) {
                        ctx.sendUpstream(event);
                        return;
                    }
                }
                logger.warn("authentication failed. cause: timeout expired");
                ctx.getChannel().close();
            }
        });

        Timeout timeout = timer.newTimeout(future, awaitTimeout, TimeUnit.MILLISECONDS);
        future.setTimeout(timeout);
        return true;
    }

    boolean done(Channel channel, boolean success) {
        DefaultFuture<Boolean> future = futureMap.remove(channel);
        if (future != null) {
            future.setResult(success);
            return true;
        }
        logger.warn("can't find future(channel:{})", channel);
        return false;
    }

}
