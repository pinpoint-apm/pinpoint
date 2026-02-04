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

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OtlpTraceMapper {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String SERVER_METHOD_NAME = "Server";
    public static final String CLIENT_METHOD_NAME = "Client";
    public static final String PRODUCER_METHOD_NAME = "Producer";
    public static final String CONSUMER_METHOD_NAME = "Consumer";
    public static final String INTERNAL_METHOD_NAME = "Internal";
    public static final String LINK_METHOD_NAME = "Link";

    private final OtlpTraceSpanMapper spanMapper;
    private final OtlpTraceSpanChunkMapper spanChunkMapper;
    private final OtlpAgentInfoMapper agentInfoMapper;

    public OtlpTraceMapper(OtlpTraceSpanMapper spanMapper, OtlpTraceSpanChunkMapper spanChunkMapper, OtlpAgentInfoMapper agentInfoMapper) {
        this.spanMapper = spanMapper;
        this.spanChunkMapper = spanChunkMapper;
        this.agentInfoMapper = agentInfoMapper;
    }

    public OtlpTraceMapperData map(List<ResourceSpans> resourceSpanList) {
        final OtlpTraceMapperData mapperData = new OtlpTraceMapperData();

        for (ResourceSpans resourceSpan : resourceSpanList) {
            final List<KeyValue> attributesList = resourceSpan.getResource().getAttributesList();
            final List<ScopeSpans> scopeSpanList = resourceSpan.getScopeSpansList();
            for (ScopeSpans scopeSpan : scopeSpanList) {
                List<Span> spansList = scopeSpan.getSpansList();
                for (Span span : spansList) {
                    try {
                        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
                            final SpanBo spanBo = spanMapper.map(attributesList, span);
                            mapperData.addSpanBo(spanBo);
                            final AgentInfoBo agentInfoBo = agentInfoMapper.map(spanBo, attributesList);
                            mapperData.addAgentInfoBo(agentInfoBo);
                        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
                            final SpanBo spanBo = spanMapper.map(attributesList, span);
                            mapperData.addSpanBo(spanBo);
                            final AgentInfoBo agentInfoBo = agentInfoMapper.map(spanBo, attributesList);
                            mapperData.addAgentInfoBo(agentInfoBo);
                        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CLIENT_VALUE) {
                            final SpanChunkBo spanChunkBo = spanChunkMapper.map(attributesList, span);
                            mapperData.addSpanChunkBo(spanChunkBo);
                        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_PRODUCER_VALUE) {
                            final SpanChunkBo spanChunkBo = spanChunkMapper.map(attributesList, span);
                            mapperData.addSpanChunkBo(spanChunkBo);
                        } else {
                            final SpanChunkBo spanChunkBo = spanChunkMapper.map(attributesList, span);
                            mapperData.addSpanChunkBo(spanChunkBo);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to map", e);
                    }
                }
            }
        }

        return mapperData;
    }
}
