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
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import com.navercorp.pinpoint.io.SpanVersion;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
public class OtlpTraceSpanMapper {
    private final ObjectMapper objectMapper;
    private OtlpTraceSpanEventMapper spanEventMapper;

    public OtlpTraceSpanMapper(ObjectMapper objectMapper, OtlpTraceSpanEventMapper spanEventMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.spanEventMapper = spanEventMapper;
    }

    SpanBo map(List<KeyValue> resourceAttributesList, Span span) {
        SpanBo spanBo = new SpanBo();
        spanBo.setVersion(SpanVersion.TRACE_V2);
        final AgentIdAndName agentIdAndName = OtlpTraceMapperUtils.getAgentId(resourceAttributesList);
        spanBo.setAgentId(agentIdAndName.agentId());
        if (agentIdAndName.agentName() != null) {
            spanBo.setAgentName(agentIdAndName.agentName());
        }
        spanBo.setApplicationName(OtlpTraceMapperUtils.getApplicationName(resourceAttributesList));

        spanBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));

        // record request
        spanBo.setRpc(getServerSpanToRpc(span));
        spanBo.setEndPoint(getServerSpanToEndPoint(span));
        spanBo.setRemoteAddr(getServerSpanToRemoteAddress(span));

        spanBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getSpanId()));
        spanBo.setParentSpanId(OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId()));
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
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.CONSUMER_METHOD_NAME));
        } else {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), OtlpTraceMapper.SERVER_METHOD_NAME));
        }
        // response
        final int responseStatusCode = (int) getServerSpanToResponseStatusCode(span);
        if (responseStatusCode != -1) {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_STATUS_CODE.getCode(), responseStatusCode));
        }
        // attributes
        if (span.getAttributesCount() > 0) {
            OtlpTraceMapperUtils.addAttributesToAnnotation(objectMapper, span.getAttributesList(), spanBo::addAnnotation);
        }

        // event
        for (Span.Event event : span.getEventsList()) {
            OtlpTraceMapperUtils.addEventToAnnotation(objectMapper, event, spanBo::addAnnotation);
        }
        // link
        for (Span.Link link : span.getLinksList()) {
            OtlpTraceMapperUtils.addLinkToAnnotation(objectMapper, link, spanBo::addAnnotation);
        }

        final List<SpanEventBo> spanEventBoList = new ArrayList<>();
        spanBo.addSpanEventBoList(spanEventBoList);
        return spanBo;
    }

    String getServerSpanToEndPoint(Span span) {
        String endPoint = "UNKNOWN";
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
            // HTTP Server
            final String serverAddress = AttributeUtils.getStringValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
            if (serverAddress != null) {
                final long serverPort = AttributeUtils.getIntValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
                endPoint = HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
            }
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            final String clientId = AttributeUtils.getStringValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID, null);
            if (clientId != null) {
                endPoint = clientId;
            }
        } else {
            throw new IllegalArgumentException("not supported span kind=" + span.getKind().getNumber());
        }

        return endPoint;
    }

    String getServerSpanToRpc(Span span) {
        String rpc = "UNKNOWN";
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
            final String urlPath = AttributeUtils.getStringValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, null);
            if (urlPath != null) {
                rpc = urlPath;
            }
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            final String destinationName = AttributeUtils.getStringValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, null);
            if (destinationName != null) {
                rpc = "destination=" + destinationName;
            }
            final String partitionId = AttributeUtils.getStringValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID, null);
            if (partitionId != null) {
                rpc = rpc + ", partition=" + partitionId;
            }
            final long offset = AttributeUtils.getIntValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET, 0L);

            if (offset != 0) {
                rpc = rpc + ",offset=" + offset;
            }
        } else {
            throw new IllegalArgumentException("not supported span kind=" + span.getKind().getNumber());
        }

        return rpc;
    }

    String getServerSpanToRemoteAddress(Span span) {
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
            return AttributeUtils.getStringValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_CLIENT_ADDRESS, "UNKNOWN");
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            return null;
        } else {
            throw new IllegalArgumentException("not supported span kind=" + span.getKind().getNumber());
        }
    }

    long getServerSpanToResponseStatusCode(Span span) {
        return AttributeUtils.getIntValue(span.getAttributesList(), OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE, -1L);
    }
}
