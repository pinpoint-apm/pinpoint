package com.nhn.pinpoint.profiler.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author emeroad
 */
public class RetryQueue {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // 우선순위 큐로 재전송 횟수가 적은것 순으로 남기고 싶지만. 갯수제한이 안되니 그냥 만듬.
    private final BlockingQueue<RetryMessage> queue;
    private final int capacity;
    private final int maxRetry;
    private final int halfCapacity;


    public RetryQueue(int capacity, int maxRetry) {
        this.queue = new LinkedBlockingQueue<RetryMessage>();
        this.capacity = capacity;
        this.halfCapacity = capacity / 2;
        this.maxRetry = maxRetry;
    }

    public RetryQueue() {
        this(1024, 3);
    }

    public void add(RetryMessage retryMessage) {
        if (retryMessage == null) {
            throw new NullPointerException("retryMessage must not be null");
        }

        final int retryCount = retryMessage.getRetryCount();
        if (retryCount >= this.maxRetry) {
            logger.warn("discard retry message. retryCount:{}", retryCount);
            return;
        }
        final int queueSize = queue.size();
        if (queueSize >= capacity) {
            logger.warn("discard retry message. queueSize:{}", queueSize);
            return;
        }
        if (queueSize >= halfCapacity && retryCount >= 1) {
            logger.warn("discard retry message. retryCount:{}", retryCount);
            return;
        }
        final boolean offer = this.queue.offer(retryMessage);
        if (!offer) {
            logger.warn("offer() fail. discard retry message. retryCount:{}", retryCount);
        }
    }

    public RetryMessage get() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }
}
