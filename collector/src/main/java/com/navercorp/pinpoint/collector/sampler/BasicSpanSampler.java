package com.navercorp.pinpoint.collector.sampler;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;

import java.util.function.ToLongFunction;

public class BasicSpanSampler implements ToLongFunction<BasicSpan> {

    private static final HashFunction hashFunction = Hashing.murmur3_128();

    @Override
    public long applyAsLong(BasicSpan span) {
        final ServerTraceId serverTraceId = span.getTransactionId();
        if (serverTraceId instanceof PinpointServerTraceId pinpointServerTraceId) {
            return pinpointServerTraceId.getTransactionSequence();
        } else if (serverTraceId instanceof OtelServerTraceId otelServerTraceId) {
            byte[] id = otelServerTraceId.getId();
            return hashFunction.hashBytes(id).asLong();
        }

        throw new IllegalArgumentException("Unsupported server trace id:" + serverTraceId);
    }
}
