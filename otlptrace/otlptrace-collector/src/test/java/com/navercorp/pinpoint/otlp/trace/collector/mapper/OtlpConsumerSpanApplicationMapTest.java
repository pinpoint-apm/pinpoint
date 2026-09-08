/*
 * Copyright 2026 NAVER Corp.
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
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.collector.applicationmap.model.AcceptorHostRow;
import com.navercorp.pinpoint.collector.applicationmap.model.ApplicationMapBuilder;
import com.navercorp.pinpoint.collector.applicationmap.model.ApplicationMapModel;
import com.navercorp.pinpoint.collector.applicationmap.model.InLinkRow;
import com.navercorp.pinpoint.collector.uid.service.StaticServiceLookupService;
import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.ActiveMQMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.KafkaMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingConsumerResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingTypeResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.PulsarMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.RabbitMQMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.RocketMQMessagingConsumerHandler;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Cross-module regression: a mapped messaging CONSUMER span must survive the collector's
 * {@link ApplicationMapBuilder}. The builder names the virtual queue node after
 * {@code acceptorHost} (falling back to {@code remoteAddr}) for a root consumer span, and writes
 * {@code remoteAddr} as the host of the queue link for a child consumer span. Both are
 * {@code requireNonNull}-ed downstream, so a consumer span without a broker address used to
 * fail the whole export batch with a NullPointerException.
 */
class OtlpConsumerSpanApplicationMapTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final byte[] TRACE_ID = {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10
    };
    private static final byte[] SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final byte[] PARENT_SPAN_ID = {9, 9, 9, 9, 9, 9, 9, 9};

    // Same codes/properties as the agent plugin constants (KafkaConstants.KAFKA_CLIENT etc.):
    // QUEUE is what routes the span through ApplicationMapBuilder's virtual-queue branch.
    private static final Map<String, ServiceType> QUEUE_TYPES = Map.of(
            "KAFKA_CLIENT",    ServiceTypeFactory.of(8660, "KAFKA_CLIENT",    "KAFKA_CLIENT",    ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS),
            "RABBITMQ_CLIENT", ServiceTypeFactory.of(8300, "RABBITMQ_CLIENT", "RABBITMQ_CLIENT", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS),
            "PULSAR_CLIENT",   ServiceTypeFactory.of(8670, "PULSAR_CLIENT",   "PULSAR_CLIENT",   ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS),
            "ROCKETMQ_CLIENT", ServiceTypeFactory.of(8400, "ROCKETMQ_CLIENT", "ROCKETMQ_CLIENT", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS),
            "ACTIVEMQ_CLIENT", ServiceTypeFactory.of(8310, "ACTIVEMQ_CLIENT", "ACTIVEMQ_CLIENT", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS)
    );

    private static final ServiceTypeRegistryService REGISTRY = new ServiceTypeRegistryService() {
        @Override
        public ServiceType findServiceType(int code) {
            for (ServiceType type : QUEUE_TYPES.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            if (code == ServiceType.OPENTELEMETRY_SERVER.getCode()) {
                return ServiceType.OPENTELEMETRY_SERVER;
            }
            if (code == ServiceType.USER.getCode()) {
                return ServiceType.USER;
            }
            if (code == ServiceType.STAND_ALONE.getCode()) {
                return ServiceType.STAND_ALONE;
            }
            return ServiceType.UNDEFINED;
        }

        @Override
        public ServiceType findServiceTypeByName(String typeName) {
            return QUEUE_TYPES.getOrDefault(typeName, ServiceType.UNDEFINED);
        }

        @Override
        public List<ServiceType> findDesc(String desc) {
            return List.of();
        }
    };

    private static final InstrumentationScope NO_SCOPE = InstrumentationScope.getDefaultInstance();

    private final OtlpTraceSpanMapper mapper = newMapper();
    private final ApplicationMapBuilder builder = new ApplicationMapBuilder(REGISTRY, new StaticServiceLookupService());

    // -----------------------------------------------------------------------------------------
    // root consumer span (no parent): virtual queue node is named after acceptorHost / remoteAddr
    // -----------------------------------------------------------------------------------------

    @Test
    void rootConsumer_withoutBrokerOrClientId_buildsQueueLinkFromDestination() {
        SpanBo spanBo = map(consumerSpan("kafka",
                kv("messaging.destination.name", strVal("orders"))));

        ApplicationMapModel model = builder.build(spanBo);

        // root-queue branch: InLinkRow(inVertex=app, selfVertex=queue) + OutLinkRow(selfVertex=queue, outVertex=app)
        assertThat(model.getInLinks()).hasSize(1);
        InLinkRow inLink = model.getInLinks().get(0);
        assertThat(inLink.selfVertex().applicationName()).isEqualTo("orders");
        assertThat(inLink.selfVertex().serviceType().getCode()).isEqualTo((short) 8660);
        assertThat(model.getOutLinks()).hasSize(1);
        assertThat(model.getOutLinks().get(0).selfVertex().applicationName()).isEqualTo("orders");
    }

    @Test
    void rootConsumer_withoutAnyAddressOrDestination_buildsQueueLinkFromUnknown() {
        SpanBo spanBo = map(consumerSpan("kafka"));

        ApplicationMapModel model = builder.build(spanBo);

        assertThat(model.getInLinks()).hasSize(1);
        assertThat(model.getInLinks().get(0).selfVertex().applicationName())
                .isEqualTo(OtlpTraceConstants.UNKNOWN_ADDRESS);
    }

    @Test
    void rootConsumer_withBroker_keepsBrokerAsQueueNode() {
        SpanBo spanBo = map(consumerSpan("kafka",
                kv("server.address", strVal("broker1.example.com")),
                kv("server.port", intVal(9092)),
                kv("messaging.destination.name", strVal("orders"))));

        ApplicationMapModel model = builder.build(spanBo);

        assertThat(model.getInLinks().get(0).selfVertex().applicationName()).isEqualTo("broker1.example.com:9092");
    }

    @Test
    void rootConsumer_everySupportedSystem_buildsWithoutAddressAttributes() {
        for (String system : List.of("kafka", "rabbitmq", "pulsar", "rocketmq", "activemq")) {
            SpanBo spanBo = map(consumerSpan(system));
            assertThat(spanBo.getAcceptorHost()).as(system + " acceptorHost").isNotNull();
            assertThat(spanBo.getRemoteAddr()).as(system + " remoteAddr").isNotNull();
            assertThat(spanBo.getEndPoint()).as(system + " endPoint").isNotNull();
            assertThatCode(() -> builder.build(spanBo)).as(system).doesNotThrowAnyException();
        }
    }

    // -----------------------------------------------------------------------------------------
    // child consumer span (Pinpoint parent context present): the queue link host is remoteAddr
    // and HbaseHostApplicationMapDao.insert requires it to be non-null
    // -----------------------------------------------------------------------------------------

    @Test
    void childConsumer_withoutBroker_bindsQueueLinkWithNonNullHost() {
        SpanBo spanBo = map(consumerSpan("kafka",
                kv("messaging.destination.name", strVal("orders"))));
        spanBo.setParentSpanId(bytesToLong(PARENT_SPAN_ID));
        spanBo.setParentApplication(ParentApplication.of("default", "producer-app", ServiceType.OPENTELEMETRY_SERVER.getCode()));

        ApplicationMapModel model = builder.build(spanBo);

        // Two host rows: buildAcceptorHost (parent -> consumer app / endPoint) and the child-queue
        // branch (parent -> queueAcceptVertex / remoteAddr). HbaseHostApplicationMapDao requires
        // a non-null host for both.
        assertThat(model.getAcceptorHosts()).hasSize(2);
        for (AcceptorHostRow row : model.getAcceptorHosts()) {
            assertThat(row.host()).as("host of " + row).isNotNull();
        }
        AcceptorHostRow queueRow = model.getAcceptorHosts().stream()
                .filter(row -> row.vertex().serviceType().getCode() == (short) 8660)
                .findFirst()
                .orElseThrow();
        assertThat(queueRow.vertex().applicationName()).isEqualTo("orders");
        assertThat(queueRow.host()).isEqualTo(OtlpTraceConstants.UNKNOWN_ADDRESS);
    }

    // -----------------------------------------------------------------------------------------
    // unsupported messaging.system stays on the server-style mapping (not a queue type), so the
    // queue branch is never entered and a null acceptorHost on a root span is harmless
    // -----------------------------------------------------------------------------------------

    @Test
    void rootConsumer_unsupportedSystem_isNotQueueAndBuilds() {
        SpanBo spanBo = map(consumerSpan("nats",
                kv("messaging.destination.name", strVal("jobs"))));

        assertThat(spanBo.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(spanBo.getAcceptorHost()).isNull();

        ApplicationMapModel model = builder.build(spanBo);
        assertThat(model.getInLinks()).hasSize(1);
        assertThat(model.getInLinks().get(0).selfVertex().serviceType()).isEqualTo(ServiceType.USER);
    }

    // -----------------------------------------------------------------------------------------

    private SpanBo map(Span span) {
        IdAndName id = new IdAndName("agent-1", "agent-1", "consumer-app", "default");
        return mapper.map(id, span, NO_SCOPE);
    }

    private static Span consumerSpan(String system, KeyValue... extraAttrs) {
        Span.Builder builder = Span.newBuilder()
                .setName("orders process")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .addAttributes(kv("messaging.system", strVal(system)));
        for (KeyValue extra : extraAttrs) {
            builder.addAttributes(extra);
        }
        return builder.build();
    }

    private static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (byte b : bytes) {
            value = (value << 8) | (b & 0xff);
        }
        return value;
    }

    private static OtlpTraceSpanMapper newMapper() {
        OtlpMessagingTypeResolver messagingTypeResolver = new OtlpMessagingTypeResolver(REGISTRY);
        return new OtlpTraceSpanMapper(
                new OtlpTraceEventMapper(JSON, 8192),
                new OtlpTraceLinkMapper(JSON, 8192),
                new OtlpServerTypeResolver(REGISTRY),
                new OtlpEnvoyRecorder(),
                new OtlpExceptionInfoResolver(),
                new OtlpMessagingConsumerResolver(List.of(
                        new KafkaMessagingConsumerHandler(),
                        new RabbitMQMessagingConsumerHandler(),
                        new PulsarMessagingConsumerHandler(),
                        new RocketMQMessagingConsumerHandler(),
                        new ActiveMQMessagingConsumerHandler()), messagingTypeResolver),
                new OtlpAttributeBoMapper(8192));
    }
}
