package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.profiler.context.Span;
import com.nhn.pinpoint.profiler.context.SpanChunk;
import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.PinpointSocketReconnectEventListener;
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
    public boolean addReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
        return false;
    }

    @Override
    public boolean removeReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
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

    @Override
    public boolean isNetworkAvailable() {
        return false;
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
