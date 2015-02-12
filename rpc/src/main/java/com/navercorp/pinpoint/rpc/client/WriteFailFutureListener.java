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

package com.navercorp.pinpoint.rpc.client;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;

/**
 * @author emeroad
 */
public class WriteFailFutureListener implements ChannelFutureListener {

    private final Logger logger;
    private final String failMessage;
    private final String successMessage;
    
    public WriteFailFutureListener(Logger logger, String failMessage) {
        this (logger, failMessage, null);
    }

    public WriteFailFutureListener(Logger logger, String failMessage, String successMessage) {
        if (logger == null) {
            throw new NullPointerException("logger must not be null");
        }
        this.logger = logger;
        this.failMessage = failMessage;
        this.successMessage = successMessage;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            if (logger.isWarnEnabled()) {
                final Throwable cause = future.getCause();
                logger.warn("{} channel:{} Caused:{}", failMessage, future.getChannel(), cause.getMessage(), cause);
            }
        } else {
            if (successMessage != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} channel:{}", successMessage, future.getChannel());
                }
            }
        }
    }
}
