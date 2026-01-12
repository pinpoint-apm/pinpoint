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

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OtlpTraceSpanEventMapper {

    public OtlpTraceSpanEventMapper() {
    }

    List<SpanEventBo> map(long spanStartTime, Span span) {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setVersion((byte) 1); // TODO
        spanEventBo.setSequence((short) 0);
        final long eventStartTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        final long eventEndTime = TimeUnit.NANOSECONDS.toMillis(span.getEndTimeUnixNano());

        final int startElapsed = (int) (eventStartTime - spanStartTime);
        spanEventBo.setStartElapsed(startElapsed);
        final int endElapsed = (int) (eventEndTime - eventStartTime);
        spanEventBo.setEndElapsed(endElapsed); // TODO ?

        final List<AnnotationBo> annotationBoList = new ArrayList<>();

        spanEventBo.setEndPoint(getClientSpanToEndPoint(span));
        spanEventBo.setDestinationId(getClientSpanToDestinationId(span));

        // Keep the order
        if (isDatabase(span)) {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_DB.getCode());
            if (isDatabaseExecuteQuery(span)) {
                spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_DB_EXECUTE_QUERY.getCode());
                // TODO bind ?
                annotationBoList.add(AnnotationBo.of(AnnotationKey.SQL.getCode(), getClientSpanDbStatement(span)));
            }
            annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.CLIENT_METHOD_NAME));
        } else if (isClient(span)) {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_CLIENT.getCode());
            annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.CLIENT_METHOD_NAME));
        } else if (isProducer(span)) {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_CLIENT.getCode());
            annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.PRODUCER_METHOD_NAME));
        } else {
            spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_INTERNAL.getCode());
            annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.INTERNAL_METHOD_NAME));
        }
        // api
        spanEventBo.setApiId(0);
        annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_START_TIME.getCode(), span.getStartTimeUnixNano()));
        // attributes
        if (span.getAttributesCount() > 0) {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), OtlpTraceMapperUtils.getAttributeAnnotationValue(span.getAttributesList())));
        }
        // argument
        annotationBoList.add(AnnotationBo.of(AnnotationKey.ARGS0.getCode(), span.getName()));

        spanEventBo.setAnnotationBoList(annotationBoList);
        spanEventBo.setDepth(1);

        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CLIENT_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_PRODUCER_VALUE) {
            final long nextSpanId = ByteArrayUtils.bytesToLong(span.getSpanId().toByteArray(), 0);
            spanEventBo.setNextSpanId(nextSpanId);
        }

        List<SpanEventBo> spanEventBoList = new ArrayList<>();
        spanEventBoList.add(spanEventBo);

        for (Span.Event event : span.getEventsList()) {
            SpanEventBo eventBo = map(spanStartTime, event);
            spanEventBoList.add(eventBo);
        }

        return spanEventBoList;
    }

    public SpanEventBo map(long startTime, Span.Event event) {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setVersion((byte) 1); // TODO
        spanEventBo.setSequence((short) 0);
        final long eventStartTime = TimeUnit.NANOSECONDS.toMillis(event.getTimeUnixNano());
        final int startElapsed = (int) (eventStartTime - startTime);
        spanEventBo.setStartElapsed(startElapsed);
        spanEventBo.setEndElapsed(0); // TODO ?
        spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_INTERNAL.getCode()); // TODO internal

        final List<AnnotationBo> annotationBoList = new ArrayList<>();
        annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_START_TIME.getCode(), event.getTimeUnixNano()));
        // api
        spanEventBo.setApiId(0);
        annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.INTERNAL_METHOD_NAME));
        // attributes
        if (event.getAttributesCount() > 0) {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), OtlpTraceMapperUtils.getAttributeAnnotationValue(event.getAttributesList())));
        }
        // argument
        annotationBoList.add(AnnotationBo.of(AnnotationKey.ARGS0.getCode(), event.getName()));

        spanEventBo.setAnnotationBoList(annotationBoList);
        spanEventBo.setDepth(2);

        return spanEventBo;
    }

    public SpanEventBo map(long startTime, Span.Link link) {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setVersion((byte) 1); // TODO
        spanEventBo.setSequence((short) 0);
        spanEventBo.setStartElapsed(0);
        spanEventBo.setEndElapsed(0); // TODO ?

        final List<AnnotationBo> annotationBoList = new ArrayList<>();
        spanEventBo.setServiceType(ServiceType.OPENTELEMETRY_INTERNAL.getCode());
        // api
        spanEventBo.setApiId(0);
        annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.LINK_METHOD_NAME));

        annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_START_TIME.getCode(), TimeUnit.MILLISECONDS.toNanos(startTime)));
        // attributes
        if (link.getAttributesCount() > 0) {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), OtlpTraceMapperUtils.getAttributeAnnotationValue(link.getAttributesList())));
        }
        spanEventBo.setAnnotationBoList(annotationBoList);
        spanEventBo.setDepth(2);

        return spanEventBo;
    }

    boolean isClient(Span span) {
        return span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CLIENT_VALUE;
    }

    boolean isProducer(Span span) {
        return span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_PRODUCER_VALUE;
    }

    boolean isDatabase(Span span) {
        return span.getAttributesList().stream().anyMatch(kv -> kv.getKey().equals("db.system"));
    }

    boolean isDatabaseExecuteQuery(Span span) {
        return span.getAttributesList().stream().anyMatch(kv -> kv.getKey().equals("db.statement"));
    }

    String getClientSpanDbStatement(Span span) {
        return span.getAttributesList().stream().filter(kv -> kv.getKey().equals("db.statement")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
    }

    String getClientSpanToEndPoint(Span span) {
        final String serverAddress = span.getAttributesList().stream().filter(kv -> kv.getKey().equals("server.address")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (serverAddress != null) {
            final Long serverPort = span.getAttributesList().stream().filter(kv -> kv.getKey().equals("server.port")).findFirst().map(kv -> kv.getValue().getIntValue()).orElse(0L);
            return HostAndPort.toHostAndPortString(serverAddress, serverPort.intValue(), 0);
        }

        return null;
    }

    String getClientSpanToDestinationId(Span span) {
        final String dbName = span.getAttributesList().stream().filter(kv -> kv.getKey().equals("db.name")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (dbName != null) {
            return dbName;
        }

        final String serverAddress = span.getAttributesList().stream().filter(kv -> kv.getKey().equals("server.address")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (serverAddress != null) {
            final Long serverPort = span.getAttributesList().stream().filter(kv -> kv.getKey().equals("server.port")).findFirst().map(kv -> kv.getValue().getIntValue()).orElse(0L);
            return HostAndPort.toHostAndPortString(serverAddress, serverPort.intValue(), 0);
        }

        return null;
    }
}