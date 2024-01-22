/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Not thread safe
 *
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanMessageConverter implements MessageConverter<SpanType, GeneratedMessageV3> {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(this.logger, 100);

    private final String agentId;
    private final short applicationServiceType;

    private final SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanProcessor;
    // WARNING not thread safe


    private final SpanMessageMapper mapper;

    public GrpcSpanMessageConverter(String agentId, short applicationServiceType,
                                    SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanProcessor,
                                    SpanMessageMapper spanMessageMapper) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationServiceType = applicationServiceType;
        this.spanProcessor = Objects.requireNonNull(spanProcessor, "spanProcessor");
        this.mapper = Objects.requireNonNull(spanMessageMapper, "spanMessageMapper");
    }

    @Override
    public GeneratedMessageV3 toMessage(SpanType message) {
        if (message instanceof SpanChunk) {
            final SpanChunk spanChunk = (SpanChunk) message;
            return buildPSpanChunk(spanChunk);
        }
        if (message instanceof Span) {
            final Span span = (Span) message;
            return buildPSpan(span);
        }
        return null;
    }

    @VisibleForTesting
    PSpan buildPSpan(Span span) {
        final PSpan.Builder pSpan = PSpan.newBuilder();

        this.spanProcessor.preProcess(span, pSpan);
        mapper.map(span, applicationServiceType, pSpan);
        this.spanProcessor.postProcess(span, pSpan);
        return pSpan.build();

    }

    @VisibleForTesting
    PSpanChunk buildPSpanChunk(SpanChunk spanChunk) {
        final PSpanChunk.Builder pSpanChunk = PSpanChunk.newBuilder();

        this.spanProcessor.preProcess(spanChunk, pSpanChunk);
        mapper.map(spanChunk, applicationServiceType, pSpanChunk);
        this.spanProcessor.postProcess(spanChunk, pSpanChunk);
        return pSpanChunk.build();
    }

    @Override
    public String toString() {
        return "GrpcSpanMessageConverter{" +
                "agentId='" + agentId + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", spanProcessor=" + spanProcessor +
                '}';
    }
}
