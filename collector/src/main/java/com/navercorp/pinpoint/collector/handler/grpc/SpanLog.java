package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.TextFormat;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;

import java.util.List;

public class SpanLog {

    public static String debugLog(PTransactionId transactionId, long spanId, List<PSpanEvent> spanEventList) {
        StringBuilder log = new StringBuilder(64);
        log.append(" transactionId:");
        log.append(TextFormat.shortDebugString(transactionId));

        log.append(" spanId:").append(spanId);

        if (CollectionUtils.hasLength(spanEventList)) {
            log.append(" spanEventSequence:");
            for (PSpanEvent pSpanEvent : spanEventList) {
                if (pSpanEvent == null) {
                    continue;
                }
                log.append(pSpanEvent.getSequence()).append(" ");
            }
        }
        return log.toString();
    }
}
