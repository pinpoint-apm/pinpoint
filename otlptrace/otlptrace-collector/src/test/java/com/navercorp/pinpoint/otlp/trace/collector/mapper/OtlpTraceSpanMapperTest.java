package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanMapperTest {

    // =======================================================================
    // extractHostAndPort
    // =======================================================================

    @Test
    void extractHostAndPort_withSchemeAndPort() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com:8080/path"))
                .isEqualTo("example.com:8080");
    }

    @Test
    void extractHostAndPort_withSchemeNoPort() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("https://example.com/path"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_withSchemeNoPath() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com:9090"))
                .isEqualTo("example.com:9090");
    }

    @Test
    void extractHostAndPort_noScheme() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("example.com:8080/path"))
                .isEqualTo("example.com:8080");
    }

    @Test
    void extractHostAndPort_withQuery() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com/path?key=val"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_withFragment() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com/path#section"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_null() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort(null)).isNull();
    }

    @Test
    void extractHostAndPort_empty() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("")).isNull();
    }

    // =======================================================================
    // extractPath
    // =======================================================================

    @Test
    void extractPath_withSchemeAndPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api/users"))
                .isEqualTo("/api/users");
    }

    @Test
    void extractPath_withQuery() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api?key=val"))
                .isEqualTo("/api");
    }

    @Test
    void extractPath_withFragment() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api#section"))
                .isEqualTo("/api");
    }

    @Test
    void extractPath_noPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com"))
                .isEqualTo("/");
    }

    @Test
    void extractPath_rootPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/"))
                .isEqualTo("/");
    }

    @Test
    void extractPath_null() {
        assertThat(OtlpTraceSpanMapper.extractPath(null)).isEqualTo("/");
    }

    @Test
    void extractPath_empty() {
        assertThat(OtlpTraceSpanMapper.extractPath("")).isEqualTo("/");
    }

    @Test
    void extractPath_noScheme() {
        assertThat(OtlpTraceSpanMapper.extractPath("example.com/api/hello"))
                .isEqualTo("/api/hello");
    }

    @Test
    void extractPath_deepPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("https://example.com:443/a/b/c/d"))
                .isEqualTo("/a/b/c/d");
    }

    // =======================================================================
    // map() — Kafka consumer span
    // =======================================================================

    private static final byte[] TRACE_ID = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };
    private static final byte[] SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};

    // Stub registry pre-loaded with the 5 messaging *_CLIENT ServiceTypes so consumer/producer
    // assertions resolve to the agent-side plugin codes (KAFKA_CLIENT=8660, etc.).
    private static final ServiceTypeRegistryService MESSAGING_REGISTRY = messagingRegistry();

    private static ServiceTypeRegistryService messagingRegistry() {
        Map<String, ServiceType> byName = new HashMap<>();
        byName.put("KAFKA_CLIENT",    ServiceTypeFactory.of(8660, "KAFKA_CLIENT",    "KAFKA_CLIENT"));
        byName.put("RABBITMQ_CLIENT", ServiceTypeFactory.of(8300, "RABBITMQ_CLIENT", "RABBITMQ_CLIENT"));
        byName.put("PULSAR_CLIENT",   ServiceTypeFactory.of(8670, "PULSAR_CLIENT",   "PULSAR_CLIENT"));
        byName.put("ROCKETMQ_CLIENT", ServiceTypeFactory.of(8400, "ROCKETMQ_CLIENT", "ROCKETMQ_CLIENT"));
        byName.put("ACTIVEMQ_CLIENT", ServiceTypeFactory.of(8310, "ACTIVEMQ_CLIENT", "ACTIVEMQ_CLIENT"));
        return new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(int code) {
                return byName.values().stream()
                        .filter(t -> t.getCode() == code)
                        .findFirst()
                        .orElse(ServiceType.UNDEFINED);
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                return byName.getOrDefault(typeName, ServiceType.UNDEFINED);
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                return List.of();
            }
        };
    }

    private static OtlpTraceSpanMapper newMapper() {
        ObjectMapper json = new ObjectMapper();
        return new OtlpTraceSpanMapper(
                new OtlpTraceEventMapper(json),
                new OtlpTraceLinkMapper(json),
                new OtlpMessagingTypeResolver(MESSAGING_REGISTRY));
    }

    private static IdAndName id() {
        return new IdAndName("agent-1", "agent-1", "app-1", "default");
    }

    private static Span consumerKafkaSpan(KeyValue... extraAttrs) {
        Span.Builder builder = Span.newBuilder()
                .setName("orders process")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .addAttributes(kv("messaging.system", strVal("kafka")))
                .addAttributes(kv("messaging.destination.name", strVal("orders")));
        for (KeyValue extra : extraAttrs) {
            builder.addAttributes(extra);
        }
        return builder.build();
    }

    private static Object findAnnotation(SpanBo bo, int code) {
        return bo.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == code)
                .map(AnnotationBo::getValue)
                .findFirst()
                .orElse(null);
    }

    @Test
    void map_consumer_kafka_setsKafkaServiceType() {
        Span span = consumerKafkaSpan();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 8660); // KAFKA_CLIENT
    }

    @Test
    void map_consumer_kafka_rpcFormat() {
        Span span = consumerKafkaSpan(
                kv("messaging.kafka.destination.partition", intVal(0)),
                kv("messaging.kafka.message.offset", intVal(12345))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getRpc()).isEqualTo("kafka://topic=orders?partition=0&offset=12345");
    }

    @Test
    void map_consumer_kafka_partitionZeroIsRecorded() {
        // Previous bug: partition was read as String (always null), and offset==0 was skipped.
        Span span = consumerKafkaSpan(
                kv("messaging.kafka.destination.partition", intVal(0))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_PARTITION))
                .isEqualTo(0);
        assertThat(bo.getRpc()).contains("partition=0");
    }

    @Test
    void map_consumer_kafka_offsetZeroIsRecorded() {
        Span span = consumerKafkaSpan(
                kv("messaging.kafka.message.offset", intVal(0))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_OFFSET))
                .isEqualTo(0L);
        assertThat(bo.getRpc()).contains("offset=0");
    }

    @Test
    void map_consumer_kafka_negativeOffsetOmitted() {
        Span span = consumerKafkaSpan(
                kv("messaging.kafka.message.offset", intVal(-1001))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_OFFSET))
                .isNull();
        assertThat(bo.getRpc()).doesNotContain("offset=");
    }

    @Test
    void map_consumer_kafka_partitionStringFallback() {
        // OTel semconv ≥ 1.24 uses messaging.destination.partition.id (string)
        Span span = consumerKafkaSpan(
                kv("messaging.destination.partition.id", strVal("7"))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_PARTITION))
                .isEqualTo(7);
        assertThat(bo.getRpc()).contains("partition=7");
    }

    @Test
    void map_consumer_kafka_annotationsRecorded() {
        Span span = consumerKafkaSpan(
                kv("messaging.kafka.destination.partition", intVal(3)),
                kv("messaging.kafka.message.offset", intVal(42)),
                kv("messaging.kafka.consumer.group", strVal("accounting"))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_TOPIC))
                .isEqualTo("orders");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_PARTITION))
                .isEqualTo(3);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_OFFSET))
                .isEqualTo(42L);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_CONSUMER_GROUP))
                .isEqualTo("accounting");
    }

    @Test
    void map_consumer_kafka_endPointUsesBrokerServerAddress() {
        Span span = consumerKafkaSpan(
                kv("server.address", strVal("broker1.example.com")),
                kv("server.port", intVal(9092)),
                kv("messaging.client_id", strVal("rdkafka#consumer-1"))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getEndPoint()).isEqualTo("broker1.example.com:9092");
        assertThat(bo.getRemoteAddr()).isEqualTo("broker1.example.com:9092");
    }

    @Test
    void map_consumer_kafka_endPointFallsBackToClientId() {
        Span span = consumerKafkaSpan(
                kv("messaging.client_id", strVal("rdkafka#consumer-1"))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getEndPoint()).isEqualTo("rdkafka#consumer-1");
        assertThat(bo.getRemoteAddr()).isNull();
    }

    @Test
    void map_consumer_kafka_acceptorHostUsesBroker() {
        Span span = consumerKafkaSpan(
                kv("server.address", strVal("broker1.example.com")),
                kv("server.port", intVal(9092))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getAcceptorHost()).isEqualTo("broker1.example.com:9092");
    }

    @Test
    void map_consumer_unsupportedSystemKeepsOpenTelemetryServer() {
        // sqs / rocketmq / etc. — no Pinpoint ServiceType registered, falls back to generic server.
        Span span = Span.newBuilder()
                .setName("queue.receive")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .addAttributes(kv("messaging.system", strVal("aws_sqs")))
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        // No system-specific annotations added.
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_TOPIC)).isNull();
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_RABBITMQ_EXCHANGE)).isNull();
    }

    // =======================================================================
    // map() — RabbitMQ consumer span (dispatch via messaging.system)
    // =======================================================================

    private static Span consumerRabbitMQSpan(KeyValue... extraAttrs) {
        Span.Builder builder = Span.newBuilder()
                .setName("orders process")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .addAttributes(kv("messaging.system", strVal("rabbitmq")))
                .addAttributes(kv("messaging.destination.name", strVal("orders.exchange")));
        for (KeyValue extra : extraAttrs) {
            builder.addAttributes(extra);
        }
        return builder.build();
    }

    @Test
    void map_consumer_rabbitmq_setsRabbitMQServiceType() {
        Span span = consumerRabbitMQSpan();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 8300); // RABBITMQ_CLIENT
    }

    @Test
    void map_consumer_rabbitmq_rpcFormat() {
        Span span = consumerRabbitMQSpan(
                kv("messaging.rabbitmq.destination.routing_key", strVal("order.created"))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getRpc()).isEqualTo("rabbitmq://exchange=orders.exchange?routingkey=order.created");
    }

    @Test
    void map_consumer_rabbitmq_rpcWithoutRoutingKey() {
        Span span = consumerRabbitMQSpan();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getRpc()).isEqualTo("rabbitmq://exchange=orders.exchange");
    }

    @Test
    void map_consumer_rabbitmq_annotationsRecorded() {
        Span span = consumerRabbitMQSpan(
                kv("messaging.rabbitmq.destination.routing_key", strVal("order.created"))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_RABBITMQ_EXCHANGE))
                .isEqualTo("orders.exchange");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_RABBITMQ_ROUTING_KEY))
                .isEqualTo("order.created");
        // No kafka annotations added.
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_KAFKA_TOPIC)).isNull();
    }

    @Test
    void map_consumer_rabbitmq_endPointUsesBrokerServerAddress() {
        Span span = consumerRabbitMQSpan(
                kv("server.address", strVal("rabbit1.example.com")),
                kv("server.port", intVal(5672))
        );
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getEndPoint()).isEqualTo("rabbit1.example.com:5672");
        assertThat(bo.getRemoteAddr()).isEqualTo("rabbit1.example.com:5672");
        assertThat(bo.getAcceptorHost()).isEqualTo("rabbit1.example.com:5672");
    }

    // =======================================================================
    // map() — Pulsar / RocketMQ / ActiveMQ consumer dispatch (smoke tests)
    // =======================================================================

    private static Span consumerSpan(String system, String destination, KeyValue... extraAttrs) {
        Span.Builder builder = Span.newBuilder()
                .setName(destination + " process")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .addAttributes(kv("messaging.system", strVal(system)))
                .addAttributes(kv("messaging.destination.name", strVal(destination)));
        for (KeyValue extra : extraAttrs) {
            builder.addAttributes(extra);
        }
        return builder.build();
    }

    @Test
    void map_consumer_pulsar_setsPulsarServiceTypeAndAnnotations() {
        Span span = consumerSpan("pulsar", "persistent://public/default/orders",
                kv("messaging.destination.partition.id", strVal("2")),
                kv("messaging.message.id", strVal("3:42:0")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 8670); // PULSAR_CLIENT
        assertThat(bo.getRpc()).isEqualTo("pulsar://topic=persistent://public/default/orders?partition=2&messageId=3:42:0");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_PULSAR_TOPIC))
                .isEqualTo("persistent://public/default/orders");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_PULSAR_PARTITION_INDEX))
                .isEqualTo(2);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_PULSAR_MESSAGE_ID))
                .isEqualTo("3:42:0");
    }

    @Test
    void map_consumer_rocketmq_setsRocketMQServiceTypeAndAnnotations() {
        Span span = consumerSpan("rocketmq", "orders",
                kv("messaging.destination.partition.id", strVal("4")),
                kv("server.address", strVal("rmq1.example.com")),
                kv("server.port", intVal(10911)));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 8400); // ROCKETMQ_CLIENT
        assertThat(bo.getRpc()).isEqualTo("rocketmq://topic=orders?queue=4");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_ROCKETMQ_TOPIC))
                .isEqualTo("orders");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_ROCKETMQ_MESSAGE_QUEUE))
                .isEqualTo(4);
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_ROCKETMQ_BROKER_SERVER))
                .isEqualTo("rmq1.example.com:10911");
    }

    @Test
    void map_consumer_activemq_setsActiveMQServiceTypeAndAnnotations() {
        Span span = consumerSpan("activemq", "orders.queue",
                kv("server.address", strVal("amq1.example.com")),
                kv("server.port", intVal(61616)));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 8310); // ACTIVEMQ_CLIENT
        assertThat(bo.getRpc()).isEqualTo("activemq://queue=orders.queue");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_MESSAGE_QUEUE_URI))
                .isEqualTo("orders.queue");
        assertThat(findAnnotation(bo, OtlpTraceConstants.ANNOTATION_KEY_ACTIVEMQ_BROKER_ADDRESS))
                .isEqualTo("amq1.example.com:61616");
    }

}