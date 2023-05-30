package com.navercorp.pinpoint.collector.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

public class RequestFailListener<RES> implements BiConsumer<RES, Throwable> {
    private final int requestId;
    private final IntConsumer consumer;

    public RequestFailListener(int requestId, IntConsumer consumer) {
        this.requestId = requestId;
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public void accept(RES res, Throwable throwable) {
        if (throwable != null) {
            consumer.accept(requestId);
        }
    }
}
