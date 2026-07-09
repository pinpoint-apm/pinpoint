package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.ActiveMQMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.KafkaMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingConsumerResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingTypeResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.PulsarMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.RabbitMQMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.RocketMQMessagingConsumerHandler;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanMapperTest {

    static ObjectMapper json = new ObjectMapper();

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
        byName.put("KAFKA_CLIENT",          ServiceTypeFactory.of(8660, "KAFKA_CLIENT",          "KAFKA_CLIENT"));
        byName.put("RABBITMQ_CLIENT",       ServiceTypeFactory.of(8300, "RABBITMQ_CLIENT",       "RABBITMQ_CLIENT"));
        byName.put("PULSAR_CLIENT",         ServiceTypeFactory.of(8670, "PULSAR_CLIENT",         "PULSAR_CLIENT"));
        byName.put("ROCKETMQ_CLIENT",       ServiceTypeFactory.of(8400, "ROCKETMQ_CLIENT",       "ROCKETMQ_CLIENT"));
        byName.put("ACTIVEMQ_CLIENT",       ServiceTypeFactory.of(8310, "ACTIVEMQ_CLIENT",       "ACTIVEMQ_CLIENT"));
        byName.put("GRPC_SERVER",           ServiceTypeFactory.of(1130, "GRPC_SERVER",           "GRPC_SERVER"));
        byName.put("APACHE_DUBBO_PROVIDER", ServiceTypeFactory.of(1999, "APACHE_DUBBO_PROVIDER", "APACHE_DUBBO_PROVIDER"));
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
        OtlpMessagingTypeResolver messagingTypeResolver = new OtlpMessagingTypeResolver(MESSAGING_REGISTRY);
        return new OtlpTraceSpanMapper(
                new OtlpTraceEventMapper(json, 8192),
                new OtlpTraceLinkMapper(json, 8192),
                new OtlpServerTypeResolver(MESSAGING_REGISTRY),
                new OtlpExceptionInfoResolver(),
                new OtlpMessagingConsumerResolver(List.of(
                        new KafkaMessagingConsumerHandler(),
                        new RabbitMQMessagingConsumerHandler(),
                        new PulsarMessagingConsumerHandler(),
                        new RocketMQMessagingConsumerHandler(),
                        new ActiveMQMessagingConsumerHandler()), messagingTypeResolver),
                8192);
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

    // =======================================================================
    // map() — SERVER-kind ServiceType dispatch via rpc.system
    // =======================================================================

    private static Span serverSpan(KeyValue... attrs) {
        Span.Builder builder = Span.newBuilder()
                .setName("HTTP GET /api")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE);
        for (KeyValue attr : attrs) {
            builder.addAttributes(attr);
        }
        return builder.build();
    }

    @Test
    void map_server_grpc_setsGrpcServerServiceType() {
        // OTel grpc-1.6 agent emits rpc.system="grpc" on the server-side instrumenter.
        Span span = serverSpan(
                kv("rpc.system", strVal("grpc")),
                kv("rpc.service", strVal("orders.OrderService")),
                kv("rpc.method", strVal("PlaceOrder")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 1130); // GRPC_SERVER
    }

    @Test
    void map_server_dubbo_setsDubboProviderServiceType() {
        // OTel apache-dubbo-2.7 agent emits rpc.system="apache_dubbo" on the server side.
        Span span = serverSpan(
                kv("rpc.system", strVal("apache_dubbo")),
                kv("rpc.service", strVal("com.example.UserService")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 1999); // APACHE_DUBBO_PROVIDER
    }

    @Test
    void map_server_http_keepsOpenTelemetryServer() {
        // Generic HTTP server (jetty/netty/armeria/akka) — no rpc.system, no framework
        // identifier in OTel attributes. Stays on OPENTELEMETRY_SERVER.
        Span span = serverSpan(
                kv("http.request.method", strVal("GET")),
                kv("url.path", strVal("/api/users/123")),
                kv("network.protocol.name", strVal("http")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void map_server_unsupportedRpcSystem_keepsOpenTelemetryServer() {
        // OTel-spec values without a Pinpoint counterpart (java_rmi, connect_rpc, dotnet_wcf).
        Span span = serverSpan(kv("rpc.system", strVal("java_rmi")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void map_server_grpc_caseInsensitive() {
        Span span = serverSpan(kv("rpc.system", strVal("GRPC")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo((short) 1130); // GRPC_SERVER
    }

    // =======================================================================
    // map() — rpc (uriTemplate) extraction precedence
    // =======================================================================

    @Test
    void map_server_httpRoute_preferredOverUrlPath() {
        // http.route is the low-cardinality template; url.path is the raw request path.
        // The route template must win so the rpc field groups by endpoint pattern.
        Span span = serverSpan(
                kv("http.route", strVal("/api/users/{id}")),
                kv("url.path", strVal("/api/users/123")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getRpc()).isEqualTo("/api/users/{id}");
    }

    @Test
    void map_server_urlPath_usedWhenHttpRouteAbsent() {
        // Unrouted request (no http.route) falls back to url.path.
        Span span = serverSpan(kv("url.path", strVal("/api/users/123")));
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getRpc()).isEqualTo("/api/users/123");
    }

    // =======================================================================
    // map() — SDK-side dropped count annotations
    // =======================================================================

    @Test
    void map_droppedAnnotation_emittedWhenAnyCountNonZero() {
        // All three counts > 0 — composite value includes all three labels in order.
        Span span = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .setDroppedAttributesCount(7)
                .setDroppedEventsCount(3)
                .setDroppedLinksCount(2)
                .build();

        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, AnnotationKey.OPENTELEMETRY_DROPPED.getCode()))
                .isEqualTo("attributes=7 events=3 links=2");
    }

    @Test
    void map_droppedAnnotation_suppressedWhenAllZero() {
        // Default counts are 0 — no annotation noise on well-behaved spans.
        Span span = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .build();

        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, AnnotationKey.OPENTELEMETRY_DROPPED.getCode())).isNull();
    }

    @Test
    void map_droppedAnnotation_omitsZeroComponents() {
        // Only attributes > 0 — composite value contains only "attributes=N".
        Span span = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .setDroppedAttributesCount(5)
                .build();

        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, AnnotationKey.OPENTELEMETRY_DROPPED.getCode()))
                .isEqualTo("attributes=5");
    }

    @Test
    void map_droppedAnnotation_skipsAttributesWhenOnlyEventsAndLinks() {
        // attributes=0, events>0, links>0 — composite drops the attributes= prefix.
        Span span = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .setDroppedEventsCount(4)
                .setDroppedLinksCount(1)
                .build();

        SpanBo bo = newMapper().map(id(), span);
        assertThat(findAnnotation(bo, AnnotationKey.OPENTELEMETRY_DROPPED.getCode()))
                .isEqualTo("events=4 links=1");
    }

    @Test
    void map_internal_rpcSystemIgnored() {
        // INTERNAL kind reuses recordServer but rpc.system dispatch is gated to SERVER kind
        // — even if rpc.system=grpc somehow leaks onto an INTERNAL span, ServiceType stays
        // on OPENTELEMETRY_SERVER.
        Span span = Span.newBuilder()
                .setName("internal-op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_INTERNAL_VALUE)
                .addAttributes(kv("rpc.system", strVal("grpc")))
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    // map() — tracestate "pp" entry → parent application / service
    // =======================================================================

    private static final byte[] PARENT_SPAN_ID = {9, 9, 9, 9, 9, 9, 9, 9};

    private static Span.Builder serverSpanBuilder() {
        return Span.newBuilder()
                .setName("/api/orders")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setParentSpanId(ByteString.copyFrom(PARENT_SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE);
    }

    @Test
    void map_tracestate_bothSubKeys_populatesParentFields() {
        Span span = serverSpanBuilder()
                .setTraceState("pp=svc:upstream-svc;app:upstream-app")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication())
                .isEqualTo(new ParentApplication("upstream-svc", "upstream-app",
                        (short) ServiceType.OPENTELEMETRY_SERVER.getCode()));
    }

    @Test
    void map_tracestate_multipleVendors_pickPinpoint() {
        Span span = serverSpanBuilder()
                .setTraceState("dd=s:1;t.dm:-4,pp=svc:upstream-svc;app:upstream-app,nr=opaque")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication())
                .isEqualTo(new ParentApplication("upstream-svc", "upstream-app",
                        (short) ServiceType.OPENTELEMETRY_SERVER.getCode()));
    }

    @Test
    void map_tracestate_svcOnly_doesNotSetParentApplication() {
        // Without parentApplicationName, ApplicationMap link insertion is gated off
        // in HbaseApplicationMapService; mirror native ServerRequestRecorder by tying
        // parentServiceName to a valid parentApplicationName.
        Span span = serverSpanBuilder()
                .setTraceState("pp=svc:upstream-svc")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication()).isNull();
    }

    @Test
    void map_tracestate_appOnly_setsApplicationWithDefaultService() {
        Span span = serverSpanBuilder()
                .setTraceState("pp=app:upstream-app")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication())
                .isEqualTo(new ParentApplication(ServiceUid.DEFAULT_SERVICE_UID_NAME, "upstream-app",
                        (short) ServiceType.OPENTELEMETRY_SERVER.getCode()));
    }

    @Test
    void map_tracestate_invalidApplicationName_silentlyDropped() {
        // IdValidateUtils rejects non-ASCII; we silently drop rather than throw,
        // to avoid corrupting ApplicationMap row keys.
        Span span = serverSpanBuilder()
                .setTraceState("pp=svc:upstream-svc;app:한글앱")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication()).isNull();
    }

    @Test
    void map_tracestate_emptyHeader_noParentFields() {
        Span span = serverSpanBuilder().build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication()).isNull();
    }

    @Test
    void map_tracestate_trueRoot_skipsParentRecording() {
        // No parentSpanId → genuine trace root, no upstream caller; ignore tracestate
        // even if a stray pp= entry is present.
        Span span = Span.newBuilder()
                .setName("/api/orders")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .setTraceState("pp=svc:upstream-svc;app:upstream-app")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication()).isNull();
    }

    @Test
    void map_tracestate_consumerSpan_alsoApplies() {
        // Messaging consumer dispatch path must also honour the upstream pp entry.
        Span span = Span.newBuilder()
                .setName("orders process")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setParentSpanId(ByteString.copyFrom(PARENT_SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .addAttributes(kv("messaging.system", strVal("kafka")))
                .addAttributes(kv("messaging.destination.name", strVal("orders")))
                .setTraceState("pp=svc:upstream-svc;app:upstream-app")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication())
                .isEqualTo(new ParentApplication("upstream-svc", "upstream-app",
                        (short) ServiceType.OPENTELEMETRY_SERVER.getCode()));
    }

    @Test
    void map_tracestate_typeOverridesOtelServerDefault() {
        // Sender claims it is a Tomcat (1010) service — override the OTel default.
        Span span = serverSpanBuilder()
                .setTraceState("pp=svc:upstream-svc;app:upstream-app;type:1010")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication().applicationServiceType()).isEqualTo((short) 1010);
    }

    @Test
    void map_tracestate_typeMissing_fallsBackToOtelServer() {
        Span span = serverSpanBuilder()
                .setTraceState("pp=svc:upstream-svc;app:upstream-app")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication().applicationServiceType())
                .isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void map_tracestate_typeNonNumeric_fallsBackToOtelServer() {
        Span span = serverSpanBuilder()
                .setTraceState("pp=app:upstream-app;type:tomcat")
                .build();
        SpanBo bo = newMapper().map(id(), span);
        assertThat(bo.getParentApplication())
                .isEqualTo(new ParentApplication(ServiceUid.DEFAULT_SERVICE_UID_NAME, "upstream-app",
                        ServiceType.OPENTELEMETRY_SERVER.getCode()));
    }

    // =======================================================================
    // map() — error status → SpanBo exceptionInfo (className:message encoding)
    // =======================================================================

    private static Span.Event exceptionEvent(KeyValue... attrs) {
        Span.Event.Builder event = Span.Event.newBuilder().setName("exception");
        for (KeyValue attr : attrs) {
            event.addAttributes(attr);
        }
        return event.build();
    }

    private static Span errorServerSpan(Status.StatusCode code, String statusMessage, KeyValue[] attrs, Span.Event... events) {
        Span.Builder builder = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .setStatus(Status.newBuilder().setCode(code).setMessage(statusMessage == null ? "" : statusMessage));
        for (KeyValue attr : attrs) {
            builder.addAttributes(attr);
        }
        for (Span.Event event : events) {
            builder.addEvents(event);
        }
        return builder.build();
    }

    private static long countEventAnnotations(SpanBo bo) {
        return bo.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == AnnotationKey.OPENTELEMETRY_EVENT.getCode())
                .count();
    }

    @Test
    void map_error_exceptionEvent_encodesClassAndMessage_andSkipsEventAnnotation() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "ignored status", new KeyValue[]{},
                exceptionEvent(kv("exception.type", strVal("java.io.IOException")),
                        kv("exception.message", strVal("disk full"))));

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getErrCode()).isEqualTo(1);
        assertThat(bo.getExceptionInfo().id()).isEqualTo(0);
        assertThat(bo.getExceptionInfo().message()).isEqualTo("java.io.IOException:disk full");
        // exception event is captured into exceptionInfo → its annotation is skipped
        assertThat(countEventAnnotations(bo)).isZero();
    }

    @Test
    void map_error_exceptionEvent_typeFromEventPreferredOverErrorType() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "",
                new KeyValue[]{kv("error.type", strVal("500"))},
                exceptionEvent(kv("exception.type", strVal("java.lang.RuntimeException")),
                        kv("exception.message", strVal("boom"))));

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getExceptionInfo().message()).isEqualTo("java.lang.RuntimeException:boom");
    }

    @Test
    void map_error_exceptionEvent_typeFallsBackToErrorType_whenEventTypeMissing() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "",
                new KeyValue[]{kv("error.type", strVal("java.net.SocketTimeoutException"))},
                exceptionEvent(kv("exception.message", strVal("timeout"))));

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getExceptionInfo().message()).isEqualTo("java.net.SocketTimeoutException:timeout");
        assertThat(countEventAnnotations(bo)).isZero();
    }

    @Test
    void map_error_exceptionEvent_messageOnly_keepsMessageWithEmptyClassPrefix() {
        // OTel permits an exception event with only exception.message (no exception.type),
        // and here no error.type either. The event message is the only error signal and must
        // not be dropped; it is encoded with an empty class-name prefix.
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "", new KeyValue[]{},
                exceptionEvent(kv("exception.message", strVal("something broke"))));

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getErrCode()).isEqualTo(1);
        assertThat(bo.getExceptionInfo().id()).isEqualTo(0);
        assertThat(bo.getExceptionInfo().message()).isEqualTo(":something broke");
        // empty class-name prefix → not treated as "captured", so the event annotation is kept
        assertThat(countEventAnnotations(bo)).isEqualTo(1);
    }

    @Test
    void map_error_exceptionEvent_messageOnly_fallsBackToStatusMessageWhenEventMessageEmpty() {
        // Exception event present but carrying neither type nor message: fall through to the
        // free-form status message rather than fabricating an empty exceptionInfo.
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "status fallback",
                new KeyValue[]{}, exceptionEvent());

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getExceptionInfo().message()).isEqualTo(":status fallback");
    }

    @Test
    void map_error_noEvent_statusMessageAndErrorType_emptyClassPrefix() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "Connection refused",
                new KeyValue[]{kv("error.type", strVal("500"))});

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getErrCode()).isEqualTo(1);
        // empty class-name prefix (leading delimiter) → message-only
        assertThat(bo.getExceptionInfo().message()).isEqualTo(":Connection refused (500)");
    }

    @Test
    void map_error_noEvent_statusMessageOnly() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "Connection refused: host", new KeyValue[]{});

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getExceptionInfo().message()).isEqualTo(":Connection refused: host");
    }

    @Test
    void map_error_noSignal_setsErrCodeButNoExceptionInfo() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "", new KeyValue[]{});

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getErrCode()).isEqualTo(1);
        assertThat(bo.getExceptionInfo()).isNull();
    }

    @Test
    void map_okStatus_noErrorRecorded() {
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_OK, "should be ignored",
                new KeyValue[]{kv("error.type", strVal("500"))},
                exceptionEvent(kv("exception.type", strVal("java.lang.RuntimeException"))));

        SpanBo bo = newMapper().map(id(), span);

        assertThat(bo.getErrCode()).isEqualTo(0);
        assertThat(bo.getExceptionInfo()).isNull();
        // not an error → exception event is kept as an annotation
        assertThat(countEventAnnotations(bo)).isEqualTo(1);
    }

    @Test
    void map_error_messageTruncatedTo256() {
        String longMessage = "x".repeat(300);
        Span span = errorServerSpan(Status.StatusCode.STATUS_CODE_ERROR, "",
                new KeyValue[]{},
                exceptionEvent(kv("exception.type", strVal("E")),
                        kv("exception.message", strVal(longMessage))));

        SpanBo bo = newMapper().map(id(), span);

        String message = bo.getExceptionInfo().message();
        assertThat(message).startsWith("E:" + "x".repeat(256));
        assertThat(message).contains("...(300)");
    }
}
