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

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class OtlpTraceSpanEventMapper {

    private final OtlpTraceEventMapper eventMapper;

    public OtlpTraceSpanEventMapper(OtlpTraceEventMapper eventMapper) {
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
    }

    List<SpanEventBo> map(long spanStartTime, Span span) {
        // Delegate to depth-aware mapper with default depth=1
        List<SpanEventBo> list = new ArrayList<>();
        list.add(map(spanStartTime, span, 1));
        return list;
    }

    SpanEventBo map(long spanStartTime, Span span, int depth) {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setVersion(SpanVersion.TRACE_V2); // TODO
        spanEventBo.setSequence((short) 0);
        final long eventStartTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        final long eventEndTime = TimeUnit.NANOSECONDS.toMillis(span.getEndTimeUnixNano());

        final int startElapsed = (int) (eventStartTime - spanStartTime);
        spanEventBo.setStartElapsed(startElapsed);
        final int endElapsed = (int) (eventEndTime - eventStartTime);
        spanEventBo.setEndElapsed(endElapsed); // TODO ?

        final Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(span.getAttributesList());
        spanEventBo.setEndPoint(getClientSpanToEndPoint(attributes));
        spanEventBo.setDestinationId(getClientSpanToDestinationId(attributes));

        // Keep the order
        if (isDatabase(attributes)) {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_DB.getCode());
            if (isDatabaseExecuteQuery(attributes)) {
                spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_DB_EXECUTE_QUERY.getCode());
                // TODO bind ?
                spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.SQL.getCode(), getClientSpanDbStatement(attributes)));
            }
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.CLIENT_METHOD_NAME)));
        } else if (isClient(span)) {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_CLIENT.getCode());
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.CLIENT_METHOD_NAME)));
        } else if (isProducer(span)) {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_CLIENT.getCode());
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.PRODUCER_METHOD_NAME)));
        } else {
            // TODO move span
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_INTERNAL.getCode());
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.INTERNAL_METHOD_NAME)));
        }
        // api
        spanEventBo.setApiId(0);
        spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_START_TIME.getCode(), span.getStartTimeUnixNano()));
        spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_SPAN_ID.getCode(), OtlpTraceMapperUtils.getSpanId(span.getSpanId())));
        spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_PARENT_SPAN_ID.getCode(), OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId())));
        // attributes
        if (!attributes.isEmpty()) {
            List<AttributeBo> attributeBoList = OtlpTraceMapperUtils.toAttributeBoList(
                    attributes, OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY);
            spanEventBo.setAttributeBoList(attributeBoList);
        }
        // event
        for (Span.Event event : span.getEventsList()) {
            eventMapper.addEventToAnnotation(event, spanEventBo::addAnnotation);
        }

        spanEventBo.setDepth(depth);

        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CLIENT_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_PRODUCER_VALUE) {
            final long nextSpanId = ByteStringUtils.parseLong(span.getSpanId());
            spanEventBo.setNextSpanId(nextSpanId);
        }

        return spanEventBo;
    }

    boolean isClient(Span span) {
        return span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CLIENT_VALUE;
    }

    boolean isProducer(Span span) {
        return span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_PRODUCER_VALUE;
    }

    boolean isDatabase(Map<String, AttributeValue> attributes) {
        return attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM) || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME);
    }

    boolean isDatabaseExecuteQuery(Map<String, AttributeValue> attributes) {
        return attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT) || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_DB_QUERY_TEXT);
    }

    String getClientSpanDbStatement(Map<String, AttributeValue> attributes) {
        String statement = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, null);
        if (statement == null) {
            statement = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_QUERY_TEXT, null);
        }
        return statement;
    }

    String getClientSpanToEndPoint(Map<String, AttributeValue> attributes) {
        final String serverAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
        if (serverAddress != null) {
            final long serverPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
            return HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
        }
        // proxy
        return AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_ADDRESS, null);
    }

    String getClientSpanToDestinationId(Map<String, AttributeValue> attributes) {
        // 1.x
        final String dbName = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAME, null);
        if (dbName != null) {
            return dbName;
        }
        // 2.x
        final String dbNamespace = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAMESPACE, null);
        if (dbNamespace != null) {
            return dbNamespace;
        }

        final String upstreamClusterName = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME, null);
        if (upstreamClusterName != null) {
            return upstreamClusterName;
        }

        final String serverAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
        if (serverAddress != null) {
            final long serverPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
            return HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
        }

        // proxy
        return AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_ADDRESS, null);
    }

    String getSpanNameOrDefault(Span span, String defaultName) {
        if (span.getName().isEmpty()) {
            return defaultName;
        }
        return span.getName();
    }
}