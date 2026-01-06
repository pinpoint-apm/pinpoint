package com.navercorp.pinpoint.collector.sampler;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;

public class BasicSpanSampler implements SamplingFunction<BasicSpan> {

    private static final HashFunction hashFunction = Hashing.murmur3_128();

    @Override
    public long sample(BasicSpan span) {
        final ServerTraceId serverTraceId = span.getTransactionId();
        if (serverTraceId instanceof PinpointServerTraceId pinpointServerTraceId) {
            return pinpointServerTraceId.getTransactionSequence();
        }
        if (serverTraceId instanceof OtelServerTraceId otelServerTraceId) {
            byte[] id = otelServerTraceId.getId();
            return hashFunction.hashBytes(id).asLong();
        }

        throw new IllegalArgumentException("Unsupported ServerTraceId:" + serverTraceId);
    }
}
