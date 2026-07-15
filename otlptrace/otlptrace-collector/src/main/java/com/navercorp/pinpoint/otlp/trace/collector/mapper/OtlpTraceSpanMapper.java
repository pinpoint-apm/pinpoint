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
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.MessageConsumerRecorder;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.MessagingAttributeUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingConsumerResolver;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Component
public class OtlpTraceSpanMapper {
    private final OtlpTraceEventMapper eventMapper;
    private final OtlpTraceLinkMapper linkMapper;
    private final OtlpServerTypeResolver serverTypeResolver;
    private final OtlpEnvoyRecorder envoyRecorder;
    private final OtlpExceptionInfoResolver exceptionInfoResolver;
    private final OtlpMessagingConsumerResolver messagingConsumerResolver;
    private final OtlpAttributeBoMapper attributeBoMapper;

    public OtlpTraceSpanMapper(OtlpTraceEventMapper eventMapper,
                               OtlpTraceLinkMapper linkMapper,
                               OtlpServerTypeResolver serverTypeResolver,
                               OtlpEnvoyRecorder envoyRecorder,
                               OtlpExceptionInfoResolver exceptionInfoResolver,
                               OtlpMessagingConsumerResolver messagingConsumerResolver,
                               OtlpAttributeBoMapper attributeBoMapper) {
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
        this.linkMapper = Objects.requireNonNull(linkMapper, "linkMapper");
        this.serverTypeResolver = Objects.requireNonNull(serverTypeResolver, "serverTypeResolver");
        this.envoyRecorder = Objects.requireNonNull(envoyRecorder, "envoyRecorder");
        this.exceptionInfoResolver = Objects.requireNonNull(exceptionInfoResolver, "exceptionInfoResolver");
        this.messagingConsumerResolver = Objects.requireNonNull(messagingConsumerResolver, "messagingConsumerResolver");
        this.attributeBoMapper = Objects.requireNonNull(attributeBoMapper, "attributeBoMapper");
    }

    SpanBo map(IdAndName idAndName, Span span, InstrumentationScope scope) {
        final long startTimeNanos = span.getStartTimeUnixNano();

        final SpanOwner owner = new SpanOwner();
        owner.setAgentId(idAndName.agentId());
        if (idAndName.agentName() != null) {
            owner.setAgentName(idAndName.agentName());
        }
        owner.setApplicationName(idAndName.applicationName());
        owner.setServiceName(idAndName.serviceName());
        owner.setAgentStartTime(TimeUnit.NANOSECONDS.toMillis(startTimeNanos));

        SpanBo spanBo = new SpanBo(TraceSourceType.OPENTELEMETRY, owner);
        spanBo.setTransactionId(new OtelServerTraceId(span.getTraceId().toByteArray()));
        spanBo.setSpanId(OtlpTraceMapperUtils.getSpanId(span.getSpanId()));
        spanBo.setParentSpanId(OtlpTraceMapperUtils.getParentSpanId(span.getParentSpanId()));
        final long endTimeNanos = Math.max(span.getEndTimeUnixNano(), startTimeNanos);
        final long elapsedNanos = endTimeNanos - startTimeNanos;
        int elapsed = (int) TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        spanBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos, endTimeNanos, elapsed);
        spanBo.setCollectorAcceptTime(System.currentTimeMillis());
        spanBo.setFlag((short) 0);
        spanBo.setExceptionClass(null);
        spanBo.setApiId(0);

        final Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(span.getAttributesList());
        // Keys promoted to a 1st-class field/annotation are collected here and excluded from the
        // raw attribute list below — "filter only what was actually consumed". Keys consumed by
        // the messaging/db collaborators are covered by the static FILTERED_ATTRIBUTE_KEY_SET.
        final Set<String> consumedKeys = new HashSet<>();
        final String messagingSystem = isConsumer(span) ? MessagingAttributeUtils.getSystem(attributes) : null;
        final MessageConsumerRecorder messageConsumerRecorder = messagingConsumerResolver.resolve(messagingSystem);
        if (messageConsumerRecorder != null) {
            messageConsumerRecorder.recordMessagingConsumer(spanBo, attributes);
        } else {
            recordServer(spanBo, span, attributes, consumedKeys);
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
        final ExceptionInfo exceptionInfo = exceptionInfoResolver.resolveErrorExceptionInfo(span, attributes);
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() == span.getStatus().getCodeValue()) {
            spanBo.setErrCode(1);
            if (exceptionInfo != null) {
                spanBo.setExceptionInfo(exceptionInfo);
            }
        }
        final boolean skipExceptionEvent = exceptionInfoResolver.isExceptionClassCaptured(exceptionInfo);
        // response — promote the HTTP status code (int, or Envoy's numeric string) to an annotation.
        // Only the raw attribute key actually consumed here is excluded from the attribute list below,
        // so a non-promoted status variant (or a non-numeric value that could not be promoted) survives.
        final OtlpHttpStatusResolver.ResponseStatus responseStatus = OtlpHttpStatusResolver.resolve(attributes);
        if (responseStatus != null) {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_STATUS_CODE.getCode(), responseStatus.code()));
            consumedKeys.add(responseStatus.sourceKey());
        }
        // http method → HTTP_METHOD annotation (surfaced as a 1st-class field in the web UI)
        final String httpMethod = getHttpMethod(attributes, consumedKeys);
        if (httpMethod != null) {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_METHOD.getCode(), httpMethod));
        }
        // gRPC status → grpc.status (160) annotation. The native agent has no gRPC server
        // plugin, so this root-span promotion is OTel-only added value (the value form mirrors
        // the native client plugin's status-name representation).
        final OtlpGrpcStatusResolver.GrpcStatus grpcStatus = OtlpGrpcStatusResolver.resolve(attributes);
        if (grpcStatus != null) {
            spanBo.addAnnotation(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_GRPC_STATUS, grpcStatus.name()));
            consumedKeys.add(grpcStatus.sourceKey());
        }
        final Predicate<String> attributeFilter = OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY.or(consumedKeys::contains);
        final TruncatedCounts truncatedCounts = new TruncatedCounts();
        // attributes
        if (!attributes.isEmpty()) {
            List<AttributeBo> attributeBoList = attributeBoMapper.toAttributeBoList(
                    attributes, attributeFilter, truncatedCounts::attribute);
            spanBo.setAttributeBoList(attributeBoList);
        }

        // event
        for (Span.Event event : span.getEventsList()) {
            // The exception event's class name is captured into exceptionInfo and its message/
            // stacktrace into exception-trace metadata, so skip its (redundant) annotation.
            if (skipExceptionEvent && OtlpTraceConstants.EVENT_NAME_EXCEPTION.equals(event.getName())) {
                continue;
            }
            final AnnotationBo eventAnnotation = eventMapper.toAnnotation(event, truncatedCounts::event);
            if (eventAnnotation != null) {
                spanBo.addAnnotation(eventAnnotation);
            }
        }
        // link
        for (Span.Link link : span.getLinksList()) {
            final AnnotationBo linkAnnotation = linkMapper.toAnnotation(link, truncatedCounts::link);
            if (linkAnnotation != null) {
                spanBo.addAnnotation(linkAnnotation);
            }
        }
        final AnnotationBo truncatedAnnotation = toTruncatedAnnotation(truncatedCounts);
        if (truncatedAnnotation != null) {
            spanBo.addAnnotation(truncatedAnnotation);
        }
        // SDK-side data-loss hints (Span proto fields 10/12/14). Only emit when > 0 so
        // well-behaved spans stay annotation-free.
        final AnnotationBo droppedAnnotation = toDroppedAnnotation(
                span.getDroppedAttributesCount(),
                span.getDroppedEventsCount(),
                span.getDroppedLinksCount());
        if (droppedAnnotation != null) {
            spanBo.addAnnotation(droppedAnnotation);
        }
        // instrumentation scope identity ("name@version") — omitted when the SDK left the
        // scope name unset, so scope-less spans stay annotation-free.
        final String scopeValue = OtlpTraceMapperUtils.formatScope(scope);
        if (scopeValue != null) {
            spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_SCOPE.getCode(), scopeValue));
        }

        return spanBo;
    }

    /**
     * Builds the single OPENTELEMETRY_DROPPED annotation summarizing SDK-side drops, or
     * {@code null} when every count is zero. Value format: space-separated {@code label=count}
     * pairs (e.g. {@code "attributes=12 events=5 links=3"}); zero components are omitted.
     * Shared between {@link OtlpTraceSpanMapper} (root spans) and
     * {@link OtlpTraceSpanEventMapper} (child spans).
     */
    static @Nullable AnnotationBo toDroppedAnnotation(int droppedAttributes,
                                                      int droppedEvents,
                                                      int droppedLinks) {
        if (droppedAttributes == 0 && droppedEvents == 0 && droppedLinks == 0) {
            return null;
        }
        String label = new DroppedCounts()
                .attributes(droppedAttributes)
                .events(droppedEvents)
                .links(droppedLinks)
                .toString();
        return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_DROPPED.getCode(), label);
    }

    /**
     * Builds the single OPENTELEMETRY_TRUNCATED annotation when the collector truncated over-long
     * attribute values or SQL on this span. Value format mirrors OPENTELEMETRY_DROPPED:
     * space-separated {@code label=count} pairs (e.g. {@code "attributes=3 sql=1"}); zero
     * components are omitted and {@code null} is returned when nothing was truncated. Shared
     * by {@link OtlpTraceSpanMapper} (root spans) and {@link OtlpTraceSpanEventMapper} (child spans).
     */
    static @Nullable AnnotationBo toTruncatedAnnotation(TruncatedCounts truncatedCounts) {
        if (truncatedCounts.isEmpty()) {
            return null;
        }
        return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_TRUNCATED.getCode(), truncatedCounts.toString());
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
     * Records request-side fields and annotations for SERVER / INTERNAL spans and for
     * CONSUMER spans whose {@code messaging.system} is unsupported (those fall through
     * to the server-style mapping). acceptorHost is only set when the span has a parent
     * (i.e. is not a trace root), since a root server span has no upstream caller.
     */
    private void recordServer(SpanBo spanBo, Span span, Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        spanBo.setRpc(getServerSpanToRpc(span, attributes, consumedKeys));
        spanBo.setEndPoint(getServerSpanToEndPoint(span, attributes, consumedKeys));
        spanBo.setRemoteAddr(getServerSpanToRemoteAddress(span, attributes, consumedKeys));

        if (spanBo.getParentSpanId() != -1) {
            spanBo.setAcceptorHost(spanBo.getEndPoint());
        }

        // SERVER kind only: dispatch ServiceType via rpc.system (grpc → GRPC_SERVER,
        // apache_dubbo → APACHE_DUBBO_PROVIDER). INTERNAL and unsupported-messaging consumer
        // fallthrough stay on OPENTELEMETRY_SERVER. HTTP server framework cannot be derived
        // from OTel attributes.
        final boolean isServerKind = span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE;
        final String rpcSystem = isServerKind
                ? AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SYSTEM, null)
                : null;
        if (rpcSystem != null) {
            consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SYSTEM);
        }
        spanBo.setServiceType(serverTypeResolver.resolveServerServiceType(rpcSystem));
        // Envoy ingress detection → identification annotations only. The ServiceType is NOT
        // overridden (see OtlpEnvoyRecorder Javadoc: mixing ENVOY(1550) with
        // OPENTELEMETRY_SERVER under one applicationName caused node-type conflicts).
        if (isServerKind && envoyRecorder.isEnvoy(attributes)) {
            envoyRecorder.recordAnnotations(spanBo::addAnnotation, attributes, true, consumedKeys);
        }

        final String apiName = isConsumer(span) ? OtlpTraceMapper.CONSUMER_METHOD_NAME : OtlpTraceMapper.SERVER_METHOD_NAME;
        spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), apiName));
    }

    String getServerSpanToEndPoint(Span span, Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE) {
            // HTTP Server
            final String serverAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
            if (serverAddress != null) {
                final long serverPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS);
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT);
                return HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
            }
            // http.url is deliberately NOT collected: only host:port is consumed and the raw URL
            // retains path/query information beyond the promoted field.
            final String httpUrl = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, null);
            if (httpUrl != null) {
                return extractHostAndPort(httpUrl);
            }
            final String rpcService = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE, null);
            if (rpcService != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE);
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

    /**
     * Consumption-free variant for callers that only need the value (e.g. the exception-trace
     * uriTemplate in {@link OtlpTraceMapper}) — consumed keys are collected into a throwaway set.
     */
    String getServerSpanToRpc(Span span, Map<String, AttributeValue> attributes) {
        return getServerSpanToRpc(span, attributes, new HashSet<>());
    }

    String getServerSpanToRpc(Span span, Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE) {
            // http.route is the matched route template ("/users/{id}") — prefer it over the raw
            // url.path ("/users/12345") so the rpc field stays low-cardinality, matching the agent's
            // recordUriTemplate behavior. Falls through to url.path when the request is unrouted.
            final String httpRoute = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_ROUTE, null);
            if (httpRoute != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_ROUTE);
                return httpRoute;
            }
            // next.route is Next.js's route template (http.route's vendor equivalent). Next.js does
            // not emit http.route, so prefer next.route over the raw url.path/http.url/http.target
            // to keep the rpc field low-cardinality (e.g. "/api/products/[productId]/index").
            final String nextRoute = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NEXT_ROUTE, null);
            if (nextRoute != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_NEXT_ROUTE);
                return nextRoute;
            }
            final String urlPath = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, null);
            if (urlPath != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH);
                return urlPath;
            }
            // http.url is deliberately NOT collected — only the path is consumed here (and the
            // host by getServerSpanToEndPoint); the raw URL retains the rest.
            final String httpUrl = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, null);
            if (httpUrl != null) {
                return extractPath(httpUrl);
            }
            final String httpTarget = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_TARGET, null);
            if (httpTarget != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_TARGET);
                return httpTarget;
            }
            final String rpcMethod = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_METHOD, null);
            if (rpcMethod != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_RPC_METHOD);
                return rpcMethod;
            }
        }
        // Known messaging consumer rpc is built via OtlpMessagingConsumerResolver#buildConsumerRpc.
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

    String getServerSpanToRemoteAddress(Span span, Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE) {
            String remoteAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_CLIENT_ADDRESS, null);
            if (remoteAddress != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_CLIENT_ADDRESS);
                return remoteAddress;
            }
            remoteAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_PEER_ADDRESS, null);
            if (remoteAddress != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_PEER_ADDRESS);
                return remoteAddress;
            }
            remoteAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_IP, null);
            if (remoteAddress != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_IP);
                return remoteAddress;
            }
            final String networkPeerIp = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, null);
            if (networkPeerIp != null) {
                final long networkPeerPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, 0L);
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP);
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT);
                return HostAndPort.toHostAndPortString(networkPeerIp, (int) networkPeerPort, 0);
            }
            return null;
        } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
            return null;
        }
        return null;
    }

    /**
     * Resolves the HTTP request method (new semconv {@code http.request.method} before legacy
     * {@code http.method}) for promotion to the HTTP_METHOD annotation, or {@code null} when
     * absent. Shared by the root-span and SpanEvent paths. Only the consumed variant is
     * collected, so a non-consumed legacy/new twin stays in the raw attribute list.
     */
    static @Nullable String getHttpMethod(Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        for (String key : OtlpTraceConstants.HTTP_METHOD_KEYS) {
            final String method = AttributeUtils.getAttributeStringValue(attributes, key, null);
            if (method != null) {
                consumedKeys.add(key);
                return method;
            }
        }
        return null;
    }
}
