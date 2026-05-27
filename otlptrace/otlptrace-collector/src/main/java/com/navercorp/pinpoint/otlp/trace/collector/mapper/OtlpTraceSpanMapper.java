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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
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
import java.util.function.Consumer;

@Component
public class OtlpTraceSpanMapper {
    private final OtlpTraceEventMapper eventMapper;
    private final OtlpTraceLinkMapper linkMapper;
    private final OtlpMessagingTypeResolver messagingTypeResolver;
    private final OtlpServerTypeResolver serverTypeResolver;

    public OtlpTraceSpanMapper(OtlpTraceEventMapper eventMapper,
                               OtlpTraceLinkMapper linkMapper,
                               OtlpMessagingTypeResolver messagingTypeResolver,
                               OtlpServerTypeResolver serverTypeResolver) {
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
        this.linkMapper = Objects.requireNonNull(linkMapper, "linkMapper");
        this.messagingTypeResolver = Objects.requireNonNull(messagingTypeResolver, "messagingTypeResolver");
        this.serverTypeResolver = Objects.requireNonNull(serverTypeResolver, "serverTypeResolver");
    }

    SpanBo map(IdAndName idAndName, Span span) {
        SpanBo spanBo = new SpanBo();
        spanBo.setVersion(SpanVersion.TRACE_V2);
        spanBo.setAgentId(idAndName.agentId());
        if (idAndName.agentName() != null) {
            spanBo.setAgentName(idAndName.agentName());
        }
        spanBo.setApplicationName(idAndName.applicationName());
        spanBo.setServiceName(idAndName.serviceName());

        spanBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));
        spanBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getSpanId()));
        spanBo.setParentSpanId(OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId()));
        final long startTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        final long endTime = TimeUnit.NANOSECONDS.toMillis(span.getEndTimeUnixNano());
        spanBo.setStartTime(startTime);
        int elapsed = Math.max((int) (endTime - startTime), 0);
        spanBo.setElapsed(elapsed);
        spanBo.setAgentStartTime(startTime);
        spanBo.setCollectorAcceptTime(System.currentTimeMillis());
        spanBo.setFlag((short) 0);
        spanBo.setExceptionClass(null);
        spanBo.setApiId(0);

        final Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(span.getAttributesList());
        final String messagingSystem = isConsumer(span) ? MessagingAttributeUtils.getSystem(attributes) : null;
        final boolean isMessagingConsumer = isSupportedMessagingSystem(messagingSystem);
        if (isMessagingConsumer) {
            recordMessagingConsumer(spanBo, attributes, messagingSystem);
        } else {
            recordServer(spanBo, span, attributes);
        }

        // Apply Pinpoint context propagated via tracestate from an upstream OTel-traced service.
        // Skip true trace roots (no parent span) — there is no upstream caller to record.
        if (!span.getParentSpanId().isEmpty()) {
            applyPinpointTraceState(spanBo, span.getTraceState());
        }

        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() == span.getStatus().getCodeValue()) {
            spanBo.setErrCode(1);
            final String errorType = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE, null);
            if (StringUtils.hasLength(errorType)) {
                spanBo.setExceptionInfo(new ExceptionInfo(1, errorType));
            } else if (StringUtils.hasLength(span.getStatus().getMessage())) {
                spanBo.setExceptionInfo(new ExceptionInfo(1, span.getStatus().getMessage()));
            }
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
        // SDK-side data-loss hints (Span proto fields 10/12/14). Only emit when > 0 so
        // well-behaved spans stay annotation-free.
        addDroppedAnnotations(spanBo::addAnnotation,
                span.getDroppedAttributesCount(),
                span.getDroppedEventsCount(),
                span.getDroppedLinksCount());

        return spanBo;
    }

    /**
     * Emits a single OPENTELEMETRY_DROPPED annotation summarizing SDK-side drops when at
     * least one count is non-zero. Value format: space-separated {@code label=count} pairs
     * (e.g. {@code "attributes=12 events=5 links=3"}); zero components are omitted. Shared
     * between {@link OtlpTraceSpanMapper} (root spans) and {@link OtlpTraceSpanEventMapper}
     * (child spans) — both expose {@code addAnnotation(AnnotationBo)} as a method reference.
     */
    static void addDroppedAnnotations(Consumer<AnnotationBo> sink,
                                      int droppedAttributes,
                                      int droppedEvents,
                                      int droppedLinks) {
        if (droppedAttributes == 0 && droppedEvents == 0 && droppedLinks == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendDroppedCount(sb, "attributes", droppedAttributes);
        appendDroppedCount(sb, "events", droppedEvents);
        appendDroppedCount(sb, "links", droppedLinks);
        sink.accept(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_DROPPED.getCode(), sb.toString()));
    }

    private static void appendDroppedCount(StringBuilder sb, String label, int count) {
        if (count > 0) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(label).append('=').append(count);
        }
    }

    /**
     * Applies an upstream Pinpoint context entry from {@code tracestate}.
     * Mirrors the native {@code ServerRequestRecorder} behavior: parentServiceName is
     * only meaningful when accompanied by a valid parentApplicationName, so the two
     * fields are tied. The parent service type uses the {@code type} sub-key when
     * present, otherwise falls back to {@link ServiceType#OPENTELEMETRY_SERVER} on the
     * assumption that the upstream is another OTel-instrumented service. Invalid
     * applicationName (length / pattern) is silently dropped to avoid corrupting
     * ApplicationMap row keys.
     */
    private void applyPinpointTraceState(SpanBo spanBo, String traceState) {
        final PinpointTraceStateParser.PinpointHeader header = PinpointTraceStateParser.parse(traceState);
        if (header == null) {
            return;
        }
        final String parentApplicationName = header.parentApplicationName();
        if (parentApplicationName == null
                || !IdValidateUtils.validateId(parentApplicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3)) {
            return;
        }
        spanBo.setParentApplicationName(parentApplicationName);
        final Short parentApplicationType = header.parentApplicationType();
        spanBo.setParentApplicationServiceType(parentApplicationType != null
                ? parentApplicationType
                : ServiceType.OPENTELEMETRY_SERVER.getCode());
        final String parentServiceName = header.parentServiceName();
        if (parentServiceName != null) {
            spanBo.setParentServiceName(parentServiceName);
        }
    }

    /**
     * Records request-side fields and annotations for a CONSUMER span whose
     * {@code messaging.system} maps to a Pinpoint ServiceType. Mirrors the agent's
     * {@code ConsumerRecordEntryPointInterceptor.recordRootSpan}: acceptorHost is set
     * even when the span is a trace root, because every consumer has an upstream broker.
     */
    private void recordMessagingConsumer(SpanBo spanBo,
                                         Map<String, AttributeValue> attributes,
                                         String messagingSystem) {
        final String broker = MessagingAttributeUtils.getBrokerAddress(attributes);
        spanBo.setRpc(buildMessagingConsumerRpc(messagingSystem, attributes));
        spanBo.setEndPoint(MessagingAttributeUtils.resolveEndPoint(attributes));
        spanBo.setRemoteAddr(broker);

        final String acceptor = spanBo.getRemoteAddr() != null ? spanBo.getRemoteAddr() : spanBo.getEndPoint();
        if (acceptor != null) {
            spanBo.setAcceptorHost(acceptor);
        }

        spanBo.setServiceType(messagingTypeResolver.resolveClientServiceType(messagingSystem));
        spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), resolveMessagingConsumerEntryPointName(messagingSystem)));
        addMessagingConsumerAnnotations(messagingSystem, attributes, spanBo::addAnnotation);
    }

    /**
     * Records request-side fields and annotations for SERVER / INTERNAL spans and for
     * CONSUMER spans whose {@code messaging.system} is unsupported (those fall through
     * to the server-style mapping). acceptorHost is only set when the span has a parent
     * (i.e. is not a trace root), since a root server span has no upstream caller.
     */
    private void recordServer(SpanBo spanBo, Span span, Map<String, AttributeValue> attributes) {
        spanBo.setRpc(getServerSpanToRpc(span, attributes));
        spanBo.setEndPoint(getServerSpanToEndPoint(span, attributes));
        spanBo.setRemoteAddr(getServerSpanToRemoteAddress(span, attributes));

        if (spanBo.getParentSpanId() != -1) {
            spanBo.setAcceptorHost(spanBo.getEndPoint());
        }

        // SERVER kind only: dispatch ServiceType via rpc.system (grpc → GRPC_SERVER,
        // apache_dubbo → APACHE_DUBBO_PROVIDER). INTERNAL and unsupported-messaging consumer
        // fallthrough stay on OPENTELEMETRY_SERVER. HTTP server framework cannot be derived
        // from OTel attributes — verified against OTel Java agent source.
        final String rpcSystem = (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                ? AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SYSTEM, null)
                : null;
        spanBo.setServiceType(serverTypeResolver.resolveServerServiceType(rpcSystem));

        final String apiName = isConsumer(span) ? OtlpTraceMapper.CONSUMER_METHOD_NAME : OtlpTraceMapper.SERVER_METHOD_NAME;
        spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), apiName));
    }

    String getServerSpanToEndPoint(Span span, Map<String, AttributeValue> attributes) {
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
            // Consumer for an unsupported messaging.system: fall back to client_id.
            // Known systems (kafka, rabbitmq) are handled in map() via the messaging dispatch.
            return AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID, null);
        }
        return null;
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
        }
        // Known messaging consumer rpc is built in map() via buildMessagingConsumerRpc.
        // Consumer for unsupported messaging.system / unknown kind: fall back to the OTel span name.
        return span.getName();
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

    static boolean isConsumer(Span span) {
        return span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE;
    }

    /**
     * True when the consumer span belongs to a messaging system Pinpoint has a ServiceType for.
     */
    static boolean isSupportedMessagingSystem(String messagingSystem) {
        return OtlpTraceConstants.MESSAGING_SYSTEM_KAFKA.equalsIgnoreCase(messagingSystem)
                || OtlpTraceConstants.MESSAGING_SYSTEM_RABBITMQ.equalsIgnoreCase(messagingSystem)
                || OtlpTraceConstants.MESSAGING_SYSTEM_PULSAR.equalsIgnoreCase(messagingSystem)
                || OtlpTraceConstants.MESSAGING_SYSTEM_ROCKETMQ.equalsIgnoreCase(messagingSystem)
                || OtlpTraceConstants.MESSAGING_SYSTEM_ACTIVEMQ.equalsIgnoreCase(messagingSystem);
    }

    /**
     * Dispatch {@code messaging.system} → system-specific consumer rpc string.
     */
    static String buildMessagingConsumerRpc(String messagingSystem, Map<String, AttributeValue> attributes) {
        if (OtlpTraceConstants.MESSAGING_SYSTEM_KAFKA.equalsIgnoreCase(messagingSystem)) {
            return KafkaAttributeUtils.buildConsumerRpc(attributes);
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_RABBITMQ.equalsIgnoreCase(messagingSystem)) {
            return RabbitMQAttributeUtils.buildConsumerRpc(attributes);
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_PULSAR.equalsIgnoreCase(messagingSystem)) {
            return PulsarAttributeUtils.buildConsumerRpc(attributes);
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_ROCKETMQ.equalsIgnoreCase(messagingSystem)) {
            return RocketMQAttributeUtils.buildConsumerRpc(attributes);
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_ACTIVEMQ.equalsIgnoreCase(messagingSystem)) {
            return ActiveMQAttributeUtils.buildConsumerRpc(attributes);
        }
        return null;
    }

    /**
     * Dispatch {@code messaging.system} → entry-point display name shown on the Call Tree.
     * Strings mirror the agent-side {@code *EntryMethodDescriptor.getApiDescriptor()} values
     * ("Kafka Consumer Invocation", "RabbitMQ Consumer Invocation", ...), so OTel-sourced
     * consumer spans render identically to agent-instrumented ones once the web side classifies
     * the span's ServiceType as a messaging consumer (via {@code ServiceTypeCategory.MESSAGE_BROKER}).
     * Falls back to the generic "Consumer" label for unknown systems.
     */
    static String resolveMessagingConsumerEntryPointName(String messagingSystem) {
        if (OtlpTraceConstants.MESSAGING_SYSTEM_KAFKA.equalsIgnoreCase(messagingSystem)) {
            return "Kafka Consumer Invocation";
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_RABBITMQ.equalsIgnoreCase(messagingSystem)) {
            return "RabbitMQ Consumer Invocation";
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_PULSAR.equalsIgnoreCase(messagingSystem)) {
            return "Pulsar Consumer Invocation";
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_ROCKETMQ.equalsIgnoreCase(messagingSystem)) {
            return "RocketMQ Consumer Invocation";
        }
        if (OtlpTraceConstants.MESSAGING_SYSTEM_ACTIVEMQ.equalsIgnoreCase(messagingSystem)) {
            return "ActiveMQ Consumer Invocation";
        }
        return OtlpTraceMapper.CONSUMER_METHOD_NAME;
    }

    /**
     * Dispatch {@code messaging.system} → system-specific annotation emitter for consumer spans.
     * Kafka additionally emits its consumer-group annotation; the others have no analogue.
     */
    static void addMessagingConsumerAnnotations(String messagingSystem,
                                                Map<String, AttributeValue> attributes,
                                                Consumer<AnnotationBo> sink) {
        if (OtlpTraceConstants.MESSAGING_SYSTEM_KAFKA.equalsIgnoreCase(messagingSystem)) {
            KafkaAttributeUtils.recordTopicPartitionOffset(attributes, sink);
            final String group = MessagingAttributeUtils.getConsumerGroup(attributes);
            if (group != null) {
                sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_KAFKA_CONSUMER_GROUP, group));
            }
        } else if (OtlpTraceConstants.MESSAGING_SYSTEM_RABBITMQ.equalsIgnoreCase(messagingSystem)) {
            RabbitMQAttributeUtils.recordExchangeRoutingKey(attributes, sink);
        } else if (OtlpTraceConstants.MESSAGING_SYSTEM_PULSAR.equalsIgnoreCase(messagingSystem)) {
            PulsarAttributeUtils.recordTopicPartitionMessage(attributes, sink);
        } else if (OtlpTraceConstants.MESSAGING_SYSTEM_ROCKETMQ.equalsIgnoreCase(messagingSystem)) {
            RocketMQAttributeUtils.recordTopicQueueBroker(attributes, sink);
        } else if (OtlpTraceConstants.MESSAGING_SYSTEM_ACTIVEMQ.equalsIgnoreCase(messagingSystem)) {
            ActiveMQAttributeUtils.recordQueueBroker(attributes, sink);
        }
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
