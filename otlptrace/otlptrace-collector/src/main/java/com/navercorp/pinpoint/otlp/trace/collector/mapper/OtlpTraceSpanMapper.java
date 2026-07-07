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
import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
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
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class OtlpTraceSpanMapper {
    // Upper bound for the exception message body stored inline on SpanBo/SpanEventBo, mirroring
    // the native agent's 256-char abbreviation. The class-name prefix is not counted (it is
    // naturally bounded) and survives truncation because it precedes the body.
    static final int EXCEPTION_MESSAGE_MAX_LENGTH = 256;

    private final OtlpTraceEventMapper eventMapper;
    private final OtlpTraceLinkMapper linkMapper;
    private final OtlpMessagingTypeResolver messagingTypeResolver;
    private final OtlpServerTypeResolver serverTypeResolver;
    private final int attributeValueMaxBytes;

    public OtlpTraceSpanMapper(OtlpTraceEventMapper eventMapper,
                               OtlpTraceLinkMapper linkMapper,
                               OtlpMessagingTypeResolver messagingTypeResolver,
                               OtlpServerTypeResolver serverTypeResolver,
                               @Value("${pinpoint.collector.otlptrace.attribute.value-max-bytes:8192}") int attributeValueMaxBytes) {
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
        this.linkMapper = Objects.requireNonNull(linkMapper, "linkMapper");
        this.messagingTypeResolver = Objects.requireNonNull(messagingTypeResolver, "messagingTypeResolver");
        this.serverTypeResolver = Objects.requireNonNull(serverTypeResolver, "serverTypeResolver");
        this.attributeValueMaxBytes = attributeValueMaxBytes;
    }

    SpanBo map(IdAndName idAndName, Span span) {
        SpanBo spanBo = new SpanBo(TraceSourceType.OPENTELEMETRY);
        final SpanOwner owner = spanBo.getSpanOwner();
        owner.setAgentId(idAndName.agentId());
        if (idAndName.agentName() != null) {
            owner.setAgentName(idAndName.agentName());
        }
        owner.setApplicationName(idAndName.applicationName());
        owner.setServiceName(idAndName.serviceName());

        spanBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));
        spanBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getSpanId()));
        spanBo.setParentSpanId(OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId()));
        final long startTimeNanos = span.getStartTimeUnixNano();
        final long endTimeNanos = Math.max(span.getEndTimeUnixNano(), startTimeNanos);
        final long elapsedNanos = endTimeNanos - startTimeNanos;
        int elapsed = (int) TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        spanBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos, endTimeNanos, elapsed);
        owner.setAgentStartTime(TimeUnit.NANOSECONDS.toMillis(startTimeNanos));
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
            final ParentApplication parentApplication = parseTraceState(span.getTraceState());
            if (parentApplication != null) {
                spanBo.setParentApplication(parentApplication);
            }
        }

        // errCode is the transaction-level error flag and is set whenever the (root) span status
        // is ERROR, independently of whether an exceptionInfo can be built.
        final ExceptionInfo exceptionInfo = resolveErrorExceptionInfo(span, attributes);
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() == span.getStatus().getCodeValue()) {
            spanBo.setErrCode(1);
            if (exceptionInfo != null) {
                spanBo.setExceptionInfo(exceptionInfo);
            }
        }
        final boolean skipExceptionEvent = isExceptionClassCaptured(exceptionInfo);
        // response
        final int responseStatusCode = (int) getServerSpanToResponseStatusCode(attributes);
        if (responseStatusCode != -1) {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_STATUS_CODE.getCode(), responseStatusCode));
        }
        // attributes
        int truncatedAttributes = 0;
        if (!attributes.isEmpty()) {
            final TransformContext context = new TransformContext(attributeValueMaxBytes);
            List<AttributeBo> attributeBoList = OtlpTraceMapperUtils.toAttributeBoList(
                    attributes, OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY, context);
            truncatedAttributes = context.truncatedCount();
            spanBo.setAttributeBoList(attributeBoList);
        }

        // event
        int truncatedEvents = 0;
        for (Span.Event event : span.getEventsList()) {
            // The exception event's class name is captured into exceptionInfo and its message/
            // stacktrace into exception-trace metadata, so skip its (redundant) annotation.
            if (skipExceptionEvent && OtlpTraceConstants.EVENT_NAME_EXCEPTION.equals(event.getName())) {
                continue;
            }
            truncatedEvents += eventMapper.addEventToAnnotation(event, spanBo::addAnnotation);
        }
        // link
        int truncatedLinks = 0;
        for (Span.Link link : span.getLinksList()) {
            truncatedLinks += linkMapper.addLinkToAnnotation(link, spanBo::addAnnotation);
        }
        addTruncatedAnnotation(spanBo::addAnnotation, truncatedAttributes, 0, truncatedEvents, truncatedLinks);
        // SDK-side data-loss hints (Span proto fields 10/12/14). Only emit when > 0 so
        // well-behaved spans stay annotation-free.
        addDroppedAnnotations(spanBo::addAnnotation,
                span.getDroppedAttributesCount(),
                span.getDroppedEventsCount(),
                span.getDroppedLinksCount());

        return spanBo;
    }

    /**
     * Builds the SpanBo/SpanEventBo {@link ExceptionInfo} for an OTel span whose status is ERROR,
     * or {@code null} when the span is not ERROR (or is ERROR but carries no usable signal).
     *
     * <p>Shared by {@link OtlpTraceSpanMapper} (root → SpanBo) and {@link OtlpTraceSpanEventMapper}
     * (non-root → SpanEventBo) so both apply the identical rule. When an {@code exception} event is
     * present, its type ({@code exception.type} → {@code error.type}) is treated as the exception
     * class name; otherwise the free-form status message (with {@code error.type} appended) is used.
     *
     * <p>OTel has no {@code StringMetaData} for the class name, so both the class name and the
     * message are encoded into the single {@link ExceptionInfo#message()} field as
     * {@code "<className><delimiter><message>"} — the class-name prefix is always present (empty
     * when unknown). See {@link ExceptionInfo#OTEL_MESSAGE_DELIMITER}.
     */
    static ExceptionInfo resolveErrorExceptionInfo(Span span, Map<String, AttributeValue> attributes) {
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() != span.getStatus().getCodeValue()) {
            return null;
        }

        final Span.Event exceptionEvent = ExceptionAttributeUtils.findExceptionEvent(span);
        if (exceptionEvent != null) {
            final Map<String, AttributeValue> eventAttrs = OtlpTraceMapperUtils.getAttributeValueMap(exceptionEvent.getAttributesList());
            final String className = ExceptionAttributeUtils.resolveExceptionType(eventAttrs, attributes);
            if (StringUtils.hasLength(className)) {
                final String message = ExceptionAttributeUtils.getExceptionMessage(eventAttrs);
                return buildOtelExceptionInfo(className, message);
            }
        }

        // No exception event (or unresolved type): use the free-form message. This is a plain
        // message, so the class-name prefix stays empty.
        final String errorType = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE, null);
        final String messageBody = buildExceptionMessageBody(span.getStatus().getMessage(), errorType);
        if (!StringUtils.hasLength(messageBody)) {
            return null;
        }
        return buildOtelExceptionInfo("", messageBody);
    }

    /**
     * Combines the OTel status message and {@code error.type} into a single message body,
     * status message first: {@code "status (error.type)"} when both are present, otherwise
     * whichever is present, or {@code null} when neither is.
     */
    static String buildExceptionMessageBody(String statusMessage, String errorType) {
        final boolean hasMessage = StringUtils.hasLength(statusMessage);
        final boolean hasType = StringUtils.hasLength(errorType);
        if (hasMessage && hasType) {
            return statusMessage + " (" + errorType + ")";
        }
        if (hasMessage) {
            return statusMessage;
        }
        if (hasType) {
            return errorType;
        }
        return null;
    }

    private static ExceptionInfo buildOtelExceptionInfo(String className, String messageBody) {
        final String body = (messageBody == null) ? "" : StringUtils.abbreviate(messageBody, EXCEPTION_MESSAGE_MAX_LENGTH);
        final String message = className + ExceptionInfo.OTEL_MESSAGE_DELIMITER + body;
        return new ExceptionInfo(ExceptionInfo.OTEL_EXCEPTION_ID, message);
    }

    /**
     * True when {@code exceptionInfo} carries a non-empty exception class name prefix — i.e. the
     * span's {@code exception} event was captured. The exception event annotation is then skipped
     * to avoid duplicating what exceptionInfo + exception-trace metadata already hold.
     */
    static boolean isExceptionClassCaptured(ExceptionInfo exceptionInfo) {
        if (exceptionInfo == null || exceptionInfo.message() == null) {
            return false;
        }
        // className is the prefix before the first delimiter; index > 0 ⇒ non-empty class name.
        return exceptionInfo.message().indexOf(ExceptionInfo.OTEL_MESSAGE_DELIMITER) > 0;
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
        String label = new DroppedCounts()
                .attributes(droppedAttributes)
                .events(droppedEvents)
                .links(droppedLinks)
                .toString();
        sink.accept(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_DROPPED.getCode(), label));
    }

    /**
     * Emits a single OPENTELEMETRY_TRUNCATED annotation when the collector truncated over-long
     * attribute values or SQL on this span. Value format mirrors OPENTELEMETRY_DROPPED:
     * space-separated {@code label=count} pairs (e.g. {@code "attributes=3 sql=1"}); zero
     * components are omitted and the annotation is suppressed when nothing was truncated. Shared
     * by {@link OtlpTraceSpanMapper} (root spans) and {@link OtlpTraceSpanEventMapper} (child spans).
     */
    static void addTruncatedAnnotation(Consumer<AnnotationBo> sink, int truncatedAttributes, int truncatedSql, int truncatedEvents, int truncatedLinks) {
        if (truncatedAttributes == 0 && truncatedSql == 0 && truncatedEvents == 0 && truncatedLinks == 0) {
            return;
        }
        String label = new TruncatedCounts()
                .attributes(truncatedAttributes)
                .sql(truncatedSql)
                .events(truncatedEvents)
                .links(truncatedLinks)
                .toString();
        sink.accept(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_TRUNCATED.getCode(), label));
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
    private @Nullable ParentApplication parseTraceState(String traceState) {
        final PinpointTraceStateParser.PinpointHeader header = PinpointTraceStateParser.parse(traceState);
        if (header == null) {
            return null;
        }
        final String parentApplicationName = header.parentApplicationName();
        if (parentApplicationName == null
                || !IdValidateUtils.validateId(parentApplicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3)) {
            return null;
        }
        final Integer parentApplicationType = header.parentApplicationType();
        final int parentServiceType = parentApplicationType != null
                ? parentApplicationType
                : ServiceType.OPENTELEMETRY_SERVER.getCode();
        final String parentServiceName = header.parentServiceName();
        return ParentApplication.of(parentServiceName, parentApplicationName, parentServiceType);
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
            // http.route is the matched route template ("/users/{id}") — prefer it over the raw
            // url.path ("/users/12345") so the rpc field stays low-cardinality, matching the agent's
            // recordUriTemplate behavior. Falls through to url.path when the request is unrouted.
            final String httpRoute = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_ROUTE, null);
            if (httpRoute != null) {
                return httpRoute;
            }
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
