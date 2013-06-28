package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.message.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class MessageFuture {

    private CountDownLatch latch = new CountDownLatch(1);

    private long timeOut = 3000;
    private long createTime;

    private volatile boolean ready = false;

    private Message message;


    public MessageFuture() {
    }

    public void markTime() {
        createTime = System.currentTimeMillis();
    }

    public Message getMessage() {
        return message;
    }

    public void readyMessage(Message message) {
        if (ready) {
            throw new IllegalStateException("already ready state");
        }
        this.ready = true;
        this.message = message;
        this.latch.countDown();
    }

    public void awaitTimeout() {
        if (ready) {
            return;
        }
        long timeout = getTimeout();
        if (timeout <= 0 ) {
            // 이미 시간지남.
            this.ready = true;
            return;
        }
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    long getTimeout() {
        long waitTime = this.createTime + timeOut;
        long currentTime = System.currentTimeMillis();
        return waitTime - currentTime;
    }
}
