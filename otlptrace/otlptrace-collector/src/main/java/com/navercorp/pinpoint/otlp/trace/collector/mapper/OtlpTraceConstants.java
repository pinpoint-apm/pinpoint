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

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class OtlpTraceConstants {
    public static final String ATTRIBUTE_KEY_CLIENT_ADDRESS = "client.address";
    public static final String ATTRIBUTE_KEY_PEER_ADDRESS = "peer.address";
    public static final String ATTRIBUTE_KEY_NET_PEER_IP = "net.peer.ip";
    // Legacy (semconv 1.x) client peer: application-level logical name/port of the remote
    // service. The new-semconv equivalent is server.address/server.port. Emitted by older SDK
    // instrumentations (e.g. otel-js grpc/http) that predate the HTTP semconv stabilization —
    // without this fallback such client spans lose their endPoint/destinationId entirely.
    public static final String ATTRIBUTE_KEY_NET_PEER_NAME = "net.peer.name";
    public static final String ATTRIBUTE_KEY_NET_PEER_PORT = "net.peer.port";
    // Full request URL of an HTTP client span. New semconv: url.full; legacy: http.url
    // (ATTRIBUTE_KEY_HTTP_URL). Host:port is extracted as an endPoint/destinationId fallback;
    // the raw attribute is kept (path/query carry information beyond the extracted host).
    public static final String ATTRIBUTE_KEY_URL_FULL = "url.full";
    public static final String ATTRIBUTE_KEY_NETWORK_PEER_IP = "network.peer.address";
    public static final String ATTRIBUTE_KEY_NETWORK_PEER_PORT = "network.peer.port";
    public static final String ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    public static final String ATTRIBUTE_KEY_HTTP_STATUS_CODE = "http.status_code";
    // HTTP request method. New semconv: http.request.method; legacy 1.x: http.method. Promoted to
    // the HTTP_METHOD annotation on both the root span and SpanEvents; both keys are filtered from
    // the raw attributes once promoted (the two are the same value in different semconv versions).
    public static final String ATTRIBUTE_KEY_HTTP_REQUEST_METHOD = "http.request.method";
    public static final String ATTRIBUTE_KEY_HTTP_METHOD = "http.method";
    // Kafka message offset
    public static final String ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET = "messaging.kafka.message.offset"; // legacy
    public static final String ATTRIBUTE_KEY_MESSAGING_KAFKA_OFFSET = "messaging.kafka.offset";                 // OTel semconv ≥ stable

    // Partition: int variant (legacy) / string variant (generic, stable)
    public static final String ATTRIBUTE_KEY_MESSAGING_KAFKA_DESTINATION_PARTITION = "messaging.kafka.destination.partition"; // legacy int
    public static final String ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID = "messaging.destination.partition.id";       // generic string

    public static final String ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME = "messaging.destination.name";
    public static final String ATTRIBUTE_KEY_MESSAGING_SYSTEM = "messaging.system";
    // Generic OTel message identifier. Pulsar/ActiveMQ/etc. surface it; Kafka uses messaging.kafka.offset instead.
    public static final String ATTRIBUTE_KEY_MESSAGING_MESSAGE_ID = "messaging.message.id";

    // Consumer group: generic (semconv ≥ stable) / legacy kafka-specific
    public static final String ATTRIBUTE_KEY_MESSAGING_CONSUMER_GROUP_NAME = "messaging.consumer.group.name"; // generic
    public static final String ATTRIBUTE_KEY_MESSAGING_KAFKA_CONSUMER_GROUP = "messaging.kafka.consumer.group"; // legacy

    // RabbitMQ-specific OTel attribute. {@code messaging.destination.name} carries the exchange.
    public static final String ATTRIBUTE_KEY_MESSAGING_RABBITMQ_DESTINATION_ROUTING_KEY = "messaging.rabbitmq.destination.routing_key";

    public static final String MESSAGING_SYSTEM_KAFKA = "kafka";
    public static final String MESSAGING_SYSTEM_RABBITMQ = "rabbitmq";
    public static final String MESSAGING_SYSTEM_PULSAR = "pulsar";
    public static final String MESSAGING_SYSTEM_ROCKETMQ = "rocketmq";
    public static final String MESSAGING_SYSTEM_ACTIVEMQ = "activemq";

    // Operation kind on a messaging span. Generic semconv ≥ stable: messaging.operation.type;
    // legacy 1.x: messaging.operation. Consumer-relevant values: "receive" (poll bookkeeping
    // span) vs "process" (per-record processing span — the actual unit of consumer work).
    public static final String ATTRIBUTE_KEY_MESSAGING_OPERATION_TYPE = "messaging.operation.type";
    public static final String ATTRIBUTE_KEY_MESSAGING_OPERATION = "messaging.operation"; // legacy
    public static final String MESSAGING_OPERATION_RECEIVE = "receive";
    public static final String MESSAGING_OPERATION_PROCESS = "process";

    // Messaging *_CLIENT ServiceType codes (KAFKA_CLIENT, RABBITMQ_CLIENT, PULSAR_CLIENT,
    // ROCKETMQ_CLIENT, ACTIVEMQ_CLIENT) are resolved at runtime by name via
    // OtlpMessagingTypeResolver — they no longer need to be duplicated as static int constants
    // here. Plugin code reassignment is followed automatically; missing plugins fall back to
    // OPENTELEMETRY_CLIENT.

    // AnnotationKey codes mirrored from agent-module/plugins/kafka KafkaConstants.
    // Display names registered on collector/web side via KafkaMetadataProvider.
    public static final int ANNOTATION_KEY_KAFKA_TOPIC = 140;
    public static final int ANNOTATION_KEY_KAFKA_PARTITION = 141;
    public static final int ANNOTATION_KEY_KAFKA_OFFSET = 142;
    public static final int ANNOTATION_KEY_KAFKA_CONSUMER_GROUP = 145;

    // AnnotationKey codes mirrored from agent-module/plugins/rabbitmq RabbitMQClientConstants.
    // Display names ("rabbitmq.exchange", "rabbitmq.routingkey") are registered on collector/web via RabbitMQClientTraceMetadataProvider.
    public static final int ANNOTATION_KEY_RABBITMQ_EXCHANGE = 130;
    public static final int ANNOTATION_KEY_RABBITMQ_ROUTING_KEY = 131;

    // AnnotationKey codes mirrored from agent-module/plugins/pulsar PulsarConstants.
    public static final int ANNOTATION_KEY_PULSAR_TOPIC = 898;
    public static final int ANNOTATION_KEY_PULSAR_PARTITION_INDEX = 896;
    public static final int ANNOTATION_KEY_PULSAR_MESSAGE_ID = 897;
    public static final int ANNOTATION_KEY_PULSAR_BROKER_URL = 899;

    // AnnotationKey codes mirrored from agent-module/plugins/rocketmq RocketMQConstants.
    public static final int ANNOTATION_KEY_ROCKETMQ_TOPIC = 800;
    public static final int ANNOTATION_KEY_ROCKETMQ_MESSAGE_QUEUE = 801;
    public static final int ANNOTATION_KEY_ROCKETMQ_BROKER_SERVER = 805;

    // AnnotationKey codes mirrored from agent-module/plugins/activemq-client ActiveMQClientConstants.
    // ACTIVEMQ_BROKER_ADDRESS is plugin-defined; queue name reuses the built-in AnnotationKey.MESSAGE_QUEUE_URI (100).
    public static final int ANNOTATION_KEY_ACTIVEMQ_BROKER_ADDRESS = 101;
    public static final int ANNOTATION_KEY_MESSAGE_QUEUE_URI = 100;

    // AnnotationKey codes mirrored from agent-module/plugins/external envoy-type-provider.yml.
    // Display names ("envoy.operation", "upstream.cluster") are registered there so the web UI
    // resolves them. Attached by OtlpEnvoyRecorder when an Envoy span is detected.
    public static final int ANNOTATION_KEY_ENVOY_OPERATION = 9441;
    public static final int ANNOTATION_KEY_UPSTREAM_CLUSTER = 9442;
    // OTel HTTP server semconv: the matched route template (low-cardinality, e.g. "/users/{id}").
    // Takes precedence over url.path/http.url/http.target so the rpc field groups by endpoint
    // pattern instead of the raw, high-cardinality request path. This is the OTel equivalent of
    // the Pinpoint agent's SpanRecorder.recordUriTemplate. Emitted by framework instrumentations
    // (Spring WebMVC/WebFlux, JAX-RS, etc.); absent for unrouted requests, where url.path is the fallback.
    public static final String ATTRIBUTE_KEY_HTTP_ROUTE = "http.route";
    // Next.js built-in OTel instrumentation emits next.route as the low-cardinality route template
    // (e.g. "/api/products/[productId]/index") on its SERVER span (next.span_type=BaseServer.handleRequest).
    // It is http.route's vendor equivalent — Next.js does NOT emit http.route, so this is the only
    // route-template source for Next.js. Promoted to the rpc field just below http.route in
    // getServerSpanToRpc so the endpoint groups by pattern instead of the raw http.target path.
    public static final String ATTRIBUTE_KEY_NEXT_ROUTE = "next.route";
    public static final String ATTRIBUTE_KEY_URL_PATH = "url.path";
    public static final String ATTRIBUTE_KEY_HTTP_URL = "http.url";
    public static final String ATTRIBUTE_KEY_HTTP_TARGET = "http.target";
    public static final String ATTRIBUTE_KEY_RPC_SERVICE = "rpc.service";
    public static final String ATTRIBUTE_KEY_RPC_METHOD = "rpc.method";
    public static final String ATTRIBUTE_KEY_RPC_SYSTEM = "rpc.system";
    // OTel RPC semconv enum values emitted by the OTel Java agent (verified against
    // grpc-1.6 GrpcRpcAttributesGetter / apache-dubbo-2.7 DubboRpcAttributesGetter).
    public static final String RPC_SYSTEM_GRPC = "grpc";
    public static final String RPC_SYSTEM_APACHE_DUBBO = "apache_dubbo";
    public static final String ATTRIBUTE_KEY_MESSAGING_CLIENT_ID = "messaging.client_id";
    public static final String ATTRIBUTE_KEY_SERVER_PORT = "server.port";
    public static final String ATTRIBUTE_KEY_SERVER_ADDRESS = "server.address";
    public static final String ATTRIBUTE_KEY_UPSTREAM_ADDRESS = "upstream_address";
    public static final String ATTRIBUTE_KEY_DB_NAME = "db.name";
    public static final String ATTRIBUTE_KEY_DB_NAMESPACE = "db.namespace";
    public static final String ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME = "upstream_cluster.name";
    // Envoy-specific span tags. upstream_cluster (no ".name" suffix) is the legacy Zipkin-style
    // tag; response_flags is Envoy's connection/response flag string ("-" when none). Both are
    // emitted only by Envoy, so they serve as the Envoy detection gate in OtlpEnvoyRecorder.
    // component=proxy is intentionally NOT used — it is deprecated in OTel semconv and set by
    // other proxies too, so it has no reliable discriminating power on its own.
    public static final String ATTRIBUTE_KEY_UPSTREAM_CLUSTER = "upstream_cluster";
    public static final String ATTRIBUTE_KEY_RESPONSE_FLAGS = "response_flags";
    public static final String ATTRIBUTE_KEY_DB_STATEMENT = "db.statement";
    public static final String ATTRIBUTE_KEY_DB_QUERY_TEXT = "db.query.text";
    public static final String ATTRIBUTE_KEY_DB_SYSTEM = "db.system";
    public static final String ATTRIBUTE_KEY_DB_SYSTEM_NAME = "db.system.name";
    // OTel Redis semconv uses db.namespace as the numeric DB index (0-15) — semantically
    // different from SQL/Mongo where db.namespace is a logical name. Guarded against in
    // getClientSpanToDestinationId so Redis ServerMap groups by system, not per-index.
    public static final String DB_SYSTEM_REDIS = "redis";
    public static final String ATTRIBUTE_KEY_DB_OPERATION_NAME = "db.operation.name";
    public static final String ATTRIBUTE_KEY_DB_COLLECTION_NAME = "db.collection.name";
    public static final String ATTRIBUTE_KEY_DB_RESPONSE_STATUS_CODE = "db.response.status_code";

    public static final String ATTRIBUTE_KEY_ERROR_TYPE = "error.type";

    public static final String EVENT_NAME_EXCEPTION = "exception";
    public static final String ATTRIBUTE_KEY_EXCEPTION_TYPE = "exception.type";
    public static final String ATTRIBUTE_KEY_EXCEPTION_MESSAGE = "exception.message";
    public static final String ATTRIBUTE_KEY_EXCEPTION_STACKTRACE = "exception.stacktrace";

    // W3C tracestate vendor entry that carries upstream Pinpoint context. Conforms to other
    // APM vendors' 2-letter key convention (dd, nr, dt, ot). Sub-keys inside the value use the
    // OTel/Datadog style: ';' separates sub-keys, ':' separates sub-key name and value.
    // Format: pp=svc:<parentServiceName>;app:<parentApplicationName>[;type:<serviceTypeCode>]
    // The 'type' sub-key is optional; when absent the upstream is assumed to be another
    // OTel-instrumented service and OPENTELEMETRY_SERVER is used as the parent service type.
    public static final String TRACESTATE_KEY_PINPOINT = "pp";
    public static final String TRACESTATE_SUBKEY_PARENT_SERVICE_NAME = "svc";
    public static final String TRACESTATE_SUBKEY_PARENT_APPLICATION_NAME = "app";
    public static final String TRACESTATE_SUBKEY_PARENT_APPLICATION_TYPE = "type";

    public static final String ATTRIBUTE_KEY_HOST_NAME = "host.name";
    public static final String ATTRIBUTE_KEY_PROCESS_PID = "process.pid";
    public static final String ATTRIBUTE_KEY_PROCESS_RUNTIME_DESCRIPTION = "process.runtime.description";
    public static final String ATTRIBUTE_KEY_TELEMETRY_SDK_VERSION = "telemetry.sdk.version";

    // Raw-attribute filtering follows one rule: "filter only what was promoted, on the path
    // that promoted it, only when it actually was promoted". Most keys are therefore excluded
    // DYNAMICALLY — the mapper collects the keys it actually consumed into a per-span
    // consumedKeys set and composes the final filter as FILTERED_ATTRIBUTE_KEY.or(consumedKeys::
    // contains). A key that was present but not consumed (wrong span kind, lost the precedence
    // race, unparsable value) stays in the raw attribute list instead of being silently dropped.
    //
    // This STATIC base set is the documented exception: domain-gated key groups whose
    // consumption is spread across many collaborators (messaging handlers/utils, the DB branch,
    // the exception resolver), where threading the consumedKeys collector through every
    // signature is not worth the churn. They are consumed whenever a span of their domain is
    // mapped, so a blanket filter is a close approximation. Migrating them to the dynamic
    // mechanism is a candidate follow-up.
    //
    // url.full / http.url are neither filtered nor collected: only host:port (or the path) is
    // consumed, and the raw URL retains query/path information beyond the promoted field.
    public static final Set<String> FILTERED_ATTRIBUTE_KEY_SET = Set.of(
            // messaging.* — consumed by OtlpMessagingConsumerResolver handlers (root CONSUMER
            // spans) and recordMessagingProducerAnnotations / MessagingAttributeUtils (PRODUCER
            // SpanEvents).
            ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET,
            ATTRIBUTE_KEY_MESSAGING_KAFKA_OFFSET,
            ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID,
            ATTRIBUTE_KEY_MESSAGING_KAFKA_DESTINATION_PARTITION,
            ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME,
            ATTRIBUTE_KEY_MESSAGING_SYSTEM,
            ATTRIBUTE_KEY_MESSAGING_CONSUMER_GROUP_NAME,
            ATTRIBUTE_KEY_MESSAGING_KAFKA_CONSUMER_GROUP,
            ATTRIBUTE_KEY_MESSAGING_RABBITMQ_DESTINATION_ROUTING_KEY,
            ATTRIBUTE_KEY_MESSAGING_MESSAGE_ID,
            ATTRIBUTE_KEY_MESSAGING_CLIENT_ID,
            // shared network endpoint keys — consumed by the root/SpanEvent endPoint chains AND
            // by MessagingAttributeUtils.resolveEndPoint, so they stay in the static set until
            // the messaging paths join the consumedKeys mechanism.
            ATTRIBUTE_KEY_SERVER_ADDRESS,
            ATTRIBUTE_KEY_SERVER_PORT,
            ATTRIBUTE_KEY_NETWORK_PEER_IP,
            ATTRIBUTE_KEY_NETWORK_PEER_PORT,
            // db.* — consumed by the DB branch of OtlpTraceSpanEventMapper (destinationId,
            // ServiceType dispatch, SQL annotation).
            ATTRIBUTE_KEY_DB_NAME,
            ATTRIBUTE_KEY_DB_NAMESPACE,
            ATTRIBUTE_KEY_DB_STATEMENT,
            ATTRIBUTE_KEY_DB_QUERY_TEXT,
            ATTRIBUTE_KEY_DB_SYSTEM,
            ATTRIBUTE_KEY_DB_SYSTEM_NAME,
            // db.operation.name / db.collection.name / db.response.status_code intentionally
            // NOT filtered — they are not promoted to a SpanEvent/Span 1st-class field, so
            // filtering would drop them entirely. Keep them in the attribute list so the web
            // UI surfaces vendor error codes (ORA-02813, 08P01, WRONGTYPE), table/collection
            // names, and DB operation kind (INSERT/SELECT/findAndModify).
            // error.type — consumed by OtlpExceptionInfoResolver on both paths.
            ATTRIBUTE_KEY_ERROR_TYPE
    );

    public static final Predicate<String> FILTERED_ATTRIBUTE_KEY = FILTERED_ATTRIBUTE_KEY_SET::contains;

    // HTTP status code keys resolve via the consumedKeys mechanism: both paths promote one of
    // them to the HTTP_STATUS_CODE annotation and only the consumed key is excluded from the raw
    // attributes — a non-promoted variant (or a non-numeric value that could not be promoted) is
    // retained instead of blanket-dropped. Precedence order: new semconv before legacy.
    public static final List<String> RESPONSE_STATUS_CODE_KEYS =
            List.of(ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE, ATTRIBUTE_KEY_HTTP_STATUS_CODE);

    // HTTP method resolution order: new semconv (http.request.method) before legacy (http.method).
    // Same consumedKeys handling as the status keys: only the consumed variant is filtered.
    public static final List<String> HTTP_METHOD_KEYS =
            List.of(ATTRIBUTE_KEY_HTTP_REQUEST_METHOD, ATTRIBUTE_KEY_HTTP_METHOD);
}
