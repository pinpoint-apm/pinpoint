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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;

import org.apache.thrift.TBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class CountingDataSender implements EnhancedDataSender {

    private final AtomicInteger requestCounter = new AtomicInteger();
    private final AtomicInteger requestRetryCounter = new AtomicInteger();
    private final AtomicInteger requestResponseListenerCounter = new AtomicInteger();
    private final AtomicInteger senderCounter = new AtomicInteger();

    private final AtomicInteger spanCounter = new AtomicInteger();
    private final AtomicInteger spanChunkCounter = new AtomicInteger();


    @Override
    public boolean request(TBase<?, ?> data) {
        requestCounter.incrementAndGet();
        return false;
    }

    @Override
    public boolean request(TBase<?, ?> data, int retry) {
        requestRetryCounter.incrementAndGet();
        return false;
    }

    @Override
    public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
        return false;
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return false;
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return false;
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        senderCounter.incrementAndGet();
        if (data instanceof Span) {
            this.spanCounter.incrementAndGet();
        } else if (data instanceof SpanChunk) {
            this.spanChunkCounter.incrementAndGet();
        }
        return false;
    }

    @Override
    public void stop() {
        this.requestCounter.set(0);
        this.requestRetryCounter.set(0);
        this.requestResponseListenerCounter.set(0);
        this.senderCounter.set(0);
        this.spanCounter.set(0);
        this.spanChunkCounter.set(0);
    }

    public int getRequestCounter() {
        return requestCounter.get();
    }

    public int getRequestRetryCounter() {
        return requestRetryCounter.get();
    }

    public int getRequestResponseListenerCounter() {
        return requestResponseListenerCounter.get();
    }

    public int getSenderCounter() {
        return senderCounter.get();
    }

    public int getSpanChunkCounter() {
        return spanChunkCounter.get();
    }

    public int getSpanCounter() {
        return spanCounter.get();
    }

    public int getTotalCount() {
        return requestCounter.get() + requestRetryCounter.get() + requestResponseListenerCounter.get() + senderCounter.get();
    }

    @Override
    public String toString() {
        return "CountingDataSender{" +
                "requestCounter=" + requestCounter +
                ", requestRetryCounter=" + requestRetryCounter +
                ", requestResponseListenerCounter=" + requestResponseListenerCounter +
                ", senderCounter=" + senderCounter +
                ", spanCounter=" + spanCounter +
                ", spanChunkCounter=" + spanChunkCounter +
                '}';
    }
}
