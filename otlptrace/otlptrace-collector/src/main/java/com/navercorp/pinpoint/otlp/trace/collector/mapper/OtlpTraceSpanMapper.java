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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
public class OtlpTraceSpanMapper {
    private ObjectMapper objectMapper;
    private OtlpTraceSpanEventMapper spanEventMapper;

    public OtlpTraceSpanMapper(ObjectMapper objectMapper, OtlpTraceSpanEventMapper spanEventMapper) {
        this.objectMapper = objectMapper;
        this.spanEventMapper = spanEventMapper;
    }

    SpanBo map(List<KeyValue> resourceAttributesList, Span span) {
        // annotations
        final List<AnnotationBo> annotationBoList = new ArrayList<>();
        SpanBo spanBo = new SpanBo();

        spanBo.setVersion(1); // TODO ?
        spanBo.setAgentId(OtlpTraceMapperUtils.getAgentId(resourceAttributesList));
        spanBo.setAgentName(null); // TODO use agentName ?
        spanBo.setApplicationName(OtlpTraceMapperUtils.getApplicationName(resourceAttributesList));

        spanBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));

        // record request
        spanBo.setRpc(getServerSpanToRpc(span));
        spanBo.setEndPoint(getServerSpanToEndPoint(span));
        spanBo.setRemoteAddr(getServerSpanToRemoteAddress(span));

        spanBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getSpanId().toByteArray()));
        spanBo.setParentSpanId(OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId().toByteArray()));
        if (spanBo.getParentSpanId() != -1) {
            spanBo.setAcceptorHost(spanBo.getEndPoint());
        }

        final long startTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        final long endTime = TimeUnit.NANOSECONDS.toMillis(span.getEndTimeUnixNano());
        // TODO save nano ?
        spanBo.setStartTime(startTime);
        int elapsed = (int) (endTime - startTime);
        spanBo.setElapsed(elapsed);
        spanBo.setAgentStartTime(startTime);

        // OPENTELEMETRY
        spanBo.setServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        spanBo.setFlag((short) 0); // TODO ?
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() == span.getStatus().getCodeValue()) {
            spanBo.setErrCode(1);
            if (StringUtils.hasLength(span.getStatus().getMessage())) {
//                ExceptionInfo exceptionInfo = new ExceptionInfo(1, span.getStatus().getMessage());
//                spanBo.setExceptionInfo(exceptionInfo);
            }
        }
        spanBo.setCollectorAcceptTime(System.currentTimeMillis());
        spanBo.setExceptionClass(null); // TODO ?

        // api
        spanBo.setApiId(0);
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.CONSUMER_METHOD_NAME));
        } else {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.SERVER_METHOD_NAME));
        }
        // response
        final int responseStatusCode = (int) getServerSpanToResponseStatusCode(span);
        if (responseStatusCode != -1) {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.HTTP_STATUS_CODE.getCode(), responseStatusCode));
        }
        // attributes
        if (span.getAttributesCount() > 0) {
            OtlpTraceMapperUtils.addAttributesToAnnotation(objectMapper, span.getAttributesList(), annotationBoList);
        }
        // event
        for (Span.Event event : span.getEventsList()) {
            OtlpTraceMapperUtils.addEventToAnnotation(objectMapper, event, annotationBoList);
        }
        // link
        for (Span.Link link : span.getLinksList()) {
            OtlpTraceMapperUtils.addLinkToAnnotation(objectMapper, link, annotationBoList);
        }
        spanBo.setAnnotationBoList(annotationBoList);

        final List<SpanEventBo> spanEventBoList = new ArrayList<>();
        spanBo.addSpanEventBoList(spanEventBoList);
        return spanBo;
    }

    String getServerSpanToEndPoint(Span span) {
        String endPoint = "UNKNOWN";
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
            // HTTP Server
            final String serverAddress = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (serverAddress != null) {
                final Long serverPort = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT)).findFirst().map(kv -> kv.getValue().getIntValue()).orElse(0L);
                endPoint = HostAndPort.toHostAndPortString(serverAddress, serverPort.intValue(), 0);
            }
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            final String clientId = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (clientId != null) {
                endPoint = clientId;
            }
        } else {
            throw new IllegalStateException("not supported span kind=" + span.getKind().getNumber());
        }

        return endPoint;
    }

    String getServerSpanToRpc(Span span) {
        String rpc = "UNKNOWN";
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
            final String urlPath = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (urlPath != null) {
                rpc = urlPath;
            }
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            final String destinationName = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (destinationName != null) {
                rpc = "destination=" + destinationName;
            }
            final String partitionId = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (partitionId != null) {
                rpc = rpc + ", partition=" + partitionId;
            }
            final long offset = span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET)).findFirst().map(kv -> kv.getValue().getIntValue()).orElse(0L);
            if (offset != 0) {
                rpc = rpc + ",offset=" + offset;
            }
        } else {
            throw new IllegalStateException("not supported span kind=" + span.getKind().getNumber());
        }

        return rpc;
    }

    String getServerSpanToRemoteAddress(Span span) {
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
            return span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_CLIENT_ADDRESS)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse("UNKNOWN");
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            return null;
        } else {
            throw new IllegalStateException("not supported span kind=" + span.getKind().getNumber());
        }
    }

    long getServerSpanToResponseStatusCode(Span span) {
        return span.getAttributesList().stream().filter(kv -> kv.getKey().equals(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE)).findFirst().map(kv -> kv.getValue().getIntValue()).orElse(-1L);
    }
}
