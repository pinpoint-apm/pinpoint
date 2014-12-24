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

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * @author emeroad
 */
public class ChannelWriteCompleteListenableFuture<T> extends DefaultFuture<T> implements ChannelFutureListener {

    private T result;

    public ChannelWriteCompleteListenableFuture() {
        this(null, 3000);
    }

    public ChannelWriteCompleteListenableFuture(T result) {
        this(result, 3000);
    }
    public ChannelWriteCompleteListenableFuture(T result, long timeoutMillis) {
        super(timeoutMillis);
        this.result = result;
    }

    public ChannelWriteCompleteListenableFuture(long timeoutMillis) {
        super(timeoutMillis);
    }


    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            this.setResult(result);
        } else {
            this.setFailure(future.getCause());
        }
    }
}
