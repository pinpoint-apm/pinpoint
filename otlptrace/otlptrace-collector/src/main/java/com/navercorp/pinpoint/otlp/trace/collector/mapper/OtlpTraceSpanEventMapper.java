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
import com.navercorp.pinpoint.common.server.util.StringTruncator;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class OtlpTraceSpanEventMapper {

    private final OtlpTraceEventMapper eventMapper;
    private final OtlpDbSystemTypeResolver dbSystemTypeResolver;
    private final OtlpMessagingTypeResolver messagingTypeResolver;
    private final OtlpClientTypeResolver clientTypeResolver;
    private final int attributeValueMaxBytes;
    private final int sqlMaxBytes;

    public OtlpTraceSpanEventMapper(OtlpTraceEventMapper eventMapper,
                                    ServiceTypeRegistryService serviceTypeRegistryService,
                                    OtlpMessagingTypeResolver messagingTypeResolver,
                                    OtlpClientTypeResolver clientTypeResolver,
                                    @Value("${pinpoint.collector.otlptrace.attribute.value-max-bytes:8192}") int attributeValueMaxBytes,
                                    @Value("${pinpoint.collector.otlptrace.sql.max-bytes:8192}") int sqlMaxBytes) {
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
        this.dbSystemTypeResolver = new OtlpDbSystemTypeResolver(
                Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService"));
        this.messagingTypeResolver = Objects.requireNonNull(messagingTypeResolver, "messagingTypeResolver");
        this.clientTypeResolver = Objects.requireNonNull(clientTypeResolver, "clientTypeResolver");
        this.attributeValueMaxBytes = attributeValueMaxBytes;
        this.sqlMaxBytes = sqlMaxBytes;
    }

    List<SpanEventBo> map(long spanStartTime, Span span) {
        // Delegate to depth-aware mapper with default depth=1
        List<SpanEventBo> list = new ArrayList<>();
        list.add(map(spanStartTime, span, 1));
        return list;
    }

    SpanEventBo map(long spanStartTime, Span span, int depth) {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setVersion(SpanVersion.TRACE_V2);
        spanEventBo.setSequence((short) 0);
        final long eventStartTime = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());
        final long eventEndTime = TimeUnit.NANOSECONDS.toMillis(span.getEndTimeUnixNano());

        final int startElapsed = (int) (eventStartTime - spanStartTime);
        spanEventBo.setStartElapsed(startElapsed);
        final int endElapsed = (int) (eventEndTime - eventStartTime);
        spanEventBo.setEndElapsed(endElapsed);

        final Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(span.getAttributesList());
        int truncatedAttributes = 0;
        int truncatedSql = 0;
        int truncatedEvents = 0;

        // Keep the order
        if (isDatabase(attributes)) {
            spanEventBo.setEndPoint(getClientSpanToEndPoint(attributes));
            spanEventBo.setDestinationId(getClientSpanToDestinationId(attributes));
            final String dbSystem = getDbSystem(attributes);
            spanEventBo.setServiceType(dbSystemTypeResolver.resolveBaseCode(dbSystem));
            if (isDatabaseExecuteQuery(attributes)) {
                spanEventBo.setServiceType(dbSystemTypeResolver.resolveExecuteQueryCode(dbSystem));
                String statement = getClientSpanDbStatement(attributes);
                final String truncatedStatement = (statement == null) ? null : StringTruncator.truncateUtf8(statement, sqlMaxBytes);
                if (truncatedStatement != null) {
                    statement = truncatedStatement;
                    truncatedSql = 1;
                }
                spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.SQL.getCode(), statement));
            }
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.CLIENT_METHOD_NAME)));
        } else if (isClient(span)) {
            spanEventBo.setEndPoint(getClientSpanToEndPoint(attributes));
            spanEventBo.setDestinationId(getClientSpanToDestinationId(attributes));
            // rpc.system dispatch: grpc → GRPC, apache_dubbo → APACHE_DUBBO_CONSUMER.
            // HTTP clients emit no rpc.system → OPENTELEMETRY_CLIENT fallback.
            final String rpcSystem = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SYSTEM, null);
            spanEventBo.setServiceType(clientTypeResolver.resolveClientServiceType(rpcSystem));
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.CLIENT_METHOD_NAME)));
        } else if (isProducer(span)) {
            final String messagingSystem = MessagingAttributeUtils.getSystem(attributes);
            final String endPoint = MessagingAttributeUtils.resolveEndPoint(attributes);
            spanEventBo.setEndPoint(endPoint);
            spanEventBo.setDestinationId(MessagingAttributeUtils.resolveProducerDestinationId(attributes, endPoint));
            spanEventBo.setServiceType(messagingTypeResolver.resolveClientServiceType(messagingSystem));
            spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), getSpanNameOrDefault(span, OtlpTraceMapper.PRODUCER_METHOD_NAME)));
            recordMessagingProducerAnnotations(messagingSystem, attributes, spanEventBo::addAnnotation);
        } else {
            // Internal span has no outgoing endpoint/destination. OTel-explicit INTERNAL kind
            // keeps the OPENTELEMETRY_INTERNAL namespace; UNSPECIFIED / nested SERVER / nested
            // CONSUMER (rare fallthroughs that don't match any earlier branch) fall back to
            // the generic INTERNAL_METHOD type to signal "kind was not classified".
            final boolean isOtelInternal = span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE;
            spanEventBo.setServiceType(isOtelInternal
                    ? ServiceType.OPENTELEMETRY_INTERNAL.getCode()
                    : ServiceType.INTERNAL_METHOD.getCode());
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
            truncatedAttributes = OtlpTraceMapperUtils.truncateAttributeValues(attributeBoList, attributeValueMaxBytes);
            spanEventBo.setAttributeBoList(attributeBoList);
        }
        // event
        for (Span.Event event : span.getEventsList()) {
            truncatedEvents += eventMapper.addEventToAnnotation(event, spanEventBo::addAnnotation);
        }
        // SDK-side data-loss hints (Span proto fields 10/12/14). Only emit when > 0.
        OtlpTraceSpanMapper.addDroppedAnnotations(spanEventBo::addAnnotation,
                span.getDroppedAttributesCount(),
                span.getDroppedEventsCount(),
                span.getDroppedLinksCount());

        spanEventBo.setDepth(depth);

        if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CLIENT_VALUE || span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_PRODUCER_VALUE) {
            final long nextSpanId = ByteStringUtils.parseLong(span.getSpanId());
            spanEventBo.setNextSpanId(nextSpanId);
        }

        OtlpTraceSpanMapper.addTruncatedAnnotation(spanEventBo::addAnnotation, truncatedAttributes, truncatedSql, truncatedEvents, 0);

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

    String getDbSystem(Map<String, AttributeValue> attributes) {
        // db.system.name (2.x) preferred over db.system (1.x)
        String dbSystemName = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME, null);
        if (dbSystemName != null) {
            return dbSystemName;
        }
        return AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, null);
    }

    boolean isDatabaseExecuteQuery(Map<String, AttributeValue> attributes) {
        return attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT) || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_DB_QUERY_TEXT);
    }

    String getClientSpanDbStatement(Map<String, AttributeValue> attributes) {
        String statement = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_QUERY_TEXT, null);
        if (statement == null) {
            statement = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, null);
        }
        return statement;
    }

    String getClientSpanToEndPoint(Map<String, AttributeValue> attributes) {
        final String serverAddress = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
        if (serverAddress != null) {
            final long serverPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
            return HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
        }
        // socket-level fallback when server.* attributes are not emitted by the SDK
        final String networkPeerIp = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, null);
        if (networkPeerIp != null) {
            final long networkPeerPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, 0L);
            return HostAndPort.toHostAndPortString(networkPeerIp, (int) networkPeerPort, 0);
        }
        // proxy
        return AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_ADDRESS, null);
    }

    String getClientSpanToDestinationId(Map<String, AttributeValue> attributes) {
        // Resolve db.system up front — OTel Redis spec defines db.namespace (and legacy
        // db.name) as the numeric DB index (0-15), not a logical namespace. For Redis we
        // skip those keys so destinationId falls through to db.system ("redis"), keeping
        // ServerMap grouped at the system level instead of fanning out per index.
        final String dbSystem = getDbSystem(attributes);
        final boolean isRedis = OtlpTraceConstants.DB_SYSTEM_REDIS.equalsIgnoreCase(dbSystem);
        if (!isRedis) {
            // 2.x
            final String dbNamespace = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAMESPACE, null);
            if (dbNamespace != null) {
                return dbNamespace;
            }
            // 1.x
            final String dbName = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAME, null);
            if (dbName != null) {
                return dbName;
            }
        }
        // Namespace-less DBs (redis, elasticsearch, memcached, ...) — group ServerMap
        // by system (e.g. "redis") instead of fanning out per-shard host:port.
        if (dbSystem != null) {
            return dbSystem;
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
        final String networkPeerIp = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, null);
        if (networkPeerIp != null) {
            final long networkPeerPort = AttributeUtils.getAttributeIntValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, 0L);
            return HostAndPort.toHostAndPortString(networkPeerIp, (int) networkPeerPort, 0);
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


    /**
     * Dispatch {@code messaging.system} → system-specific annotation emitter.
     * No-op when the system is unrecognized.
     */
    static void recordMessagingProducerAnnotations(String messagingSystem,
                                                   Map<String, AttributeValue> attributes,
                                                   Consumer<AnnotationBo> sink) {
        if (OtlpTraceConstants.MESSAGING_SYSTEM_KAFKA.equalsIgnoreCase(messagingSystem)) {
            KafkaAttributeUtils.recordTopicPartitionOffset(attributes, sink);
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
}