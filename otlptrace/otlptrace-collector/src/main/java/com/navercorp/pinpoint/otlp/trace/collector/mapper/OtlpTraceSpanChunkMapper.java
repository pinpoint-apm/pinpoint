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
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OtlpTraceSpanChunkMapper {

    private OtlpTraceSpanEventMapper spanEventMapper;

    public OtlpTraceSpanChunkMapper(OtlpTraceSpanEventMapper spanEventMapper) {
        this.spanEventMapper = spanEventMapper;
    }

    SpanChunkBo map(List<KeyValue> resourceAttributesList, Span span) {
        SpanChunkBo spanChunkBo = new SpanChunkBo();
        spanChunkBo.setVersion((byte) 1); // TODO
        final AgentIdAndName agentIdAndName = OtlpTraceMapperUtils.getAgentId(resourceAttributesList);
        spanChunkBo.setAgentId(agentIdAndName.agentId());
        if (agentIdAndName.agentName() != null) {
            spanChunkBo.setAgentName(agentIdAndName.agentName());
        }
        spanChunkBo.setApplicationName(OtlpTraceMapperUtils.getApplicationName(resourceAttributesList));

        final long startTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        spanChunkBo.setAgentStartTime(startTime);
        spanChunkBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));
        spanChunkBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getParentSpanId().toByteArray()));
        // spanChunkBo.setEndPoint();
        spanChunkBo.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        final List<SpanEventBo> spanEventBoList = spanEventMapper.map(startTime, span);
        spanChunkBo.addSpanEventBoList(spanEventBoList);
        spanChunkBo.setCollectorAcceptTime(System.currentTimeMillis());
        // spanChunkBo.setLocalAsyncId();
        spanChunkBo.setKeyTime(startTime);

        return spanChunkBo;
    }
}
