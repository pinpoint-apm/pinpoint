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
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class OtlpTraceSpanMapper {
    private final OtlpTraceEventMapper eventMapper;
    private final OtlpTraceLinkMapper linkMapper;

    public OtlpTraceSpanMapper(OtlpTraceEventMapper eventMapper,
                               OtlpTraceLinkMapper linkMapper) {
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
        this.linkMapper = Objects.requireNonNull(linkMapper, "linkMapper");
    }

    SpanBo map(IdAndName idAndName, Span span) {
        SpanBo spanBo = new SpanBo();
        spanBo.setVersion(SpanVersion.TRACE_V2);
        spanBo.setAgentId(idAndName.agentId());
        if (idAndName.agentName() != null) {
            spanBo.setAgentName(idAndName.agentName());
        }
        spanBo.setApplicationName(idAndName.applicationName());

        spanBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));

        final Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(span.getAttributesList());
        // record request
        spanBo.setRpc(getServerSpanToRpc(span, attributes));
        spanBo.setEndPoint(getServerSpanToEndPoint(span, attributes));
        spanBo.setRemoteAddr(getServerSpanToRemoteAddress(span, attributes));

        spanBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getSpanId()));
        spanBo.setParentSpanId(OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId()));
        if (spanBo.getParentSpanId() != -1) {
            spanBo.setAcceptorHost(spanBo.getEndPoint());
        }

        final long startTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        final long endTime = TimeUnit.NANOSECONDS.toMillis(span.getEndTimeUnixNano());
        // TODO save nano ?
        spanBo.setStartTime(startTime);
        int elapsed = Math.max((int) (endTime - startTime), 0);
        spanBo.setElapsed(elapsed);
        spanBo.setAgentStartTime(startTime);
        spanBo.setServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());

        spanBo.setFlag((short) 0); // TODO ?
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() == span.getStatus().getCodeValue()) {
            spanBo.setErrCode(1);
            final String errorType = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE, null);
            if (StringUtils.hasLength(errorType)) {
                spanBo.setExceptionInfo(new ExceptionInfo(1, errorType));
            } else if (StringUtils.hasLength(span.getStatus().getMessage())) {
                spanBo.setExceptionInfo(new ExceptionInfo(1, span.getStatus().getMessage()));
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
        final int responseStatusCode = (int) getServerSpanToResponseStatusCode(attributes);
        if (responseStatusCode != -1) {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_STATUS_CODE.getCode(), responseStatusCode));
        }
        // attributes
        if (!attributes.isEmpty()) {
            List<AttributeBo> attributeBoList = OtlpTraceMapperUtils.toAttributeBoList(
                    attributes, OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY);
            spanBo.setAttributeBoList(attributeBoList);
        }

        // event
        for (Span.Event event : span.getEventsList()) {
            eventMapper.addEventToAnnotation(event, spanBo::addAnnotation);
        }
        // link
        for (Span.Link link : span.getLinksList()) {
            linkMapper.addLinkToAnnotation(link, spanBo::addAnnotation);
        }

        return spanBo;
    }

    String getServerSpanToEndPoint(Span span, Map<String, AttributeValue> attributes) {
        String endPoint = null;
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE) {
            // HTTP Server
            final String serverAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
            if (serverAddress != null) {
                final long serverPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
                return HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
            }
            final String httpUrl = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, null);
            if (httpUrl != null) {
                return extractHostAndPort(httpUrl);
            }
            final String rpcService = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE, null);
            if (rpcService != null) {
                return rpcService;
            }
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            final String clientId = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID, null);
            if (clientId != null) {
                endPoint = clientId;
            }
        }

        return endPoint;
    }

    public static String extractHostAndPort(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        int start = 0;

        int schemeEnd = url.indexOf("://");
        if (schemeEnd >= 0) {
            start = schemeEnd + 3;
        }

        int end = url.length();
        for (int i = start; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '/' || c == '?' || c == '#') {
                end = i;
                break;
            }
        }

        return url.substring(start, end); // "host" or "host:port"
    }

    String getServerSpanToRpc(Span span, Map<String, AttributeValue> attributes) {
        String rpc = span.getName();
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE) {
            final String urlPath = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, null);
            if (urlPath != null) {
                return urlPath;
            }
            final String httpUrl = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, null);
            if (httpUrl != null) {
                return extractPath(httpUrl);
            }
            final String httpTarget = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_TARGET, null);
            if (httpTarget != null) {
                return httpTarget;
            }
            final String rpcMethod = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_METHOD, null);
            if (rpcMethod != null) {
                return rpcMethod;
            }
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            final String destinationName = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, null);
            if (destinationName != null) {
                rpc = "destination=" + destinationName;
            }
            final String partitionId = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID, null);
            if (partitionId != null) {
                rpc = rpc + ", partition=" + partitionId;
            }
            final long offset = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET, 0L);

            if (offset != 0) {
                rpc = rpc + ",offset=" + offset;
            }
        }

        return rpc;
    }

    public static String extractPath(String url) {
        if (url == null || url.isEmpty()) {
            return "/";
        }

        int start = 0;
        int schemeEnd = url.indexOf("://");
        if (schemeEnd >= 0) {
            start = schemeEnd + 3;
        }

        int pathStart = url.indexOf('/', start);
        if (pathStart < 0) {
            return "/"; // path 없음
        }

        int pathEnd = url.length();
        for (int i = pathStart; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '?' || c == '#') {
                pathEnd = i;
                break;
            }
        }

        return url.substring(pathStart, pathEnd);
    }

    String getServerSpanToRemoteAddress(Span span, Map<String, AttributeValue> attributes) {
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE) {
            String remoteAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_CLIENT_ADDRESS, null);
            if (remoteAddress != null) {
                return remoteAddress;
            }
            remoteAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_PEER_ADDRESS, null);
            if (remoteAddress != null) {
                return remoteAddress;
            }
            remoteAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_IP, null);
            if (remoteAddress != null) {
                return remoteAddress;
            }
            final String networkPeerIp = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, null);
            if (networkPeerIp != null) {
                final long networkPeerPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, 0L);
                return HostAndPort.toHostAndPortString(networkPeerIp, (int) networkPeerPort, 0);
            }
            return null;
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            return null;
        }
        return null;
    }

    long getServerSpanToResponseStatusCode(Map<String, AttributeValue> attributes) {
        long httpStatusCode = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE, -1L);
        if (httpStatusCode != -1) {
            return httpStatusCode;
        }
        return AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_STATUS_CODE, -1L);
    }
}
