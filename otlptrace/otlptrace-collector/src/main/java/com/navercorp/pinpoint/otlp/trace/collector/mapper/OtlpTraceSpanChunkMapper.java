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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.io.SpanVersion;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class OtlpTraceSpanChunkMapper {
    private static final Supplier<Random> RANDOM = platformDefault();

    private final OtlpTraceSpanEventMapper spanEventMapper;

    public OtlpTraceSpanChunkMapper(OtlpTraceSpanEventMapper spanEventMapper) {
        this.spanEventMapper = Objects.requireNonNull(spanEventMapper, "spanEventMapper");
    }

    SpanChunkBo map(IdAndName idAndName, Span span) {
        SpanChunkBo spanChunkBo = new SpanChunkBo();
        spanChunkBo.setVersion(SpanVersion.TRACE_V2);

        spanChunkBo.setAgentId(idAndName.agentId());
        if (idAndName.agentName() != null) {
            spanChunkBo.setAgentName(idAndName.agentName());
        }
        spanChunkBo.setApplicationName(idAndName.applicationName());

        final long startTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        // The sequence value is 0, so make a difference with the agentStartTime value.
        spanChunkBo.setAgentStartTime(generateAgentStartTime());
        spanChunkBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));
        spanChunkBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getParentSpanId()));
        // spanChunkBo.setEndPoint();
        spanChunkBo.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        final List<SpanEventBo> spanEventBoList = spanEventMapper.map(startTime, span);
        spanChunkBo.addSpanEventBoList(spanEventBoList);
        spanChunkBo.setCollectorAcceptTime(System.currentTimeMillis());
        // spanChunkBo.setLocalAsyncId();
        spanChunkBo.setKeyTime(startTime);

        return spanChunkBo;
    }

    public long generateAgentStartTime() {
        long id;
        Random random = RANDOM.get();
        do {
            id = random.nextLong();
        } while (id <= 0);
        return id;
    }

    public static Supplier<Random> platformDefault() {
        return ThreadLocalRandom::current;
    }
}