/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.PSpanMessageBatch;
import com.navercorp.pinpoint.profiler.context.SpanType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * Reusable protobuf builder for {@link PSpanMessageBatch}.
 * <p>
 * This class is NOT thread-safe. It is designed to be used by a single flush thread
 * to avoid per-batch allocation of protobuf builders.
 *
 * @author emeroad
 */
class SpanMessageBatchBuilder {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MessageConverter<SpanType, GeneratedMessageV3> messageConverter;
    // @NotThreadSafe WARNING
    private final PSpanMessageBatch.Builder batchBuilder = PSpanMessageBatch.newBuilder();
    // @NotThreadSafe WARNING
    private final PSpanMessage.Builder spanMessageBuilder = PSpanMessage.newBuilder();

    SpanMessageBatchBuilder(MessageConverter<SpanType, GeneratedMessageV3> messageConverter) {
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
    }

    PSpanMessageBatch buildBatch(List<SpanType> batch) {
        if (batch.isEmpty()) {
            return PSpanMessageBatch.getDefaultInstance();
        }
        final PSpanMessageBatch.Builder builder = this.batchBuilder;
        for (SpanType spanType : batch) {
            final GeneratedMessageV3 message = messageConverter.toMessage(spanType);
            if (message == null) {
                continue;
            }
            final PSpanMessage spanMessage = toSpanMessage(message);
            if (spanMessage != null) {
                builder.addSpan(spanMessage);
            }
        }

        PSpanMessageBatch spanMessageBatch = builder.build();
        builder.clear();
        return spanMessageBatch;
    }

    private PSpanMessage toSpanMessage(GeneratedMessageV3 message) {
        final PSpanMessage.Builder builder = this.spanMessageBuilder;
        if (message instanceof PSpan) {
            final PSpan pSpan = (PSpan) message;
            if (isDebug) {
                logger.debug("toSpanMessage PSpan={}", debugLog(pSpan));
            }

            PSpanMessage spanMessage = builder.setSpan(pSpan).build();
            builder.clear();
            return spanMessage;
        }
        if (message instanceof PSpanChunk) {
            final PSpanChunk pSpanChunk = (PSpanChunk) message;
            if (isDebug) {
                logger.debug("toSpanMessage PSpanChunk={}", debugLog(pSpanChunk));
            }

            PSpanMessage spanMessage = builder.setSpanChunk(pSpanChunk).build();
            builder.clear();
            return spanMessage;
        }
        logger.warn("Unsupported message type: {}", message.getClass());
        return null;
    }
}
