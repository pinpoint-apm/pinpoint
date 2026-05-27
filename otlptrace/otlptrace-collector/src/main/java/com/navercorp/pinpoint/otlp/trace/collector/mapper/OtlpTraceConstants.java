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

import java.util.Set;
import java.util.function.Predicate;

public class OtlpTraceConstants {
    public static final String ATTRIBUTE_KEY_CLIENT_ADDRESS = "client.address";
    public static final String ATTRIBUTE_KEY_PEER_ADDRESS = "peer.address";
    public static final String ATTRIBUTE_KEY_NET_PEER_IP = "net.peer.ip";
    public static final String ATTRIBUTE_KEY_NETWORK_PEER_IP = "network.peer.address";
    public static final String ATTRIBUTE_KEY_NETWORK_PEER_PORT = "network.peer.port";
    public static final String ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    public static final String ATTRIBUTE_KEY_HTTP_STATUS_CODE = "http.status_code";
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

    public static final Set<String> FILTERED_ATTRIBUTE_KEY_SET = Set.of(
            ATTRIBUTE_KEY_CLIENT_ADDRESS,
            ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE,
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
            ATTRIBUTE_KEY_URL_PATH,
            ATTRIBUTE_KEY_MESSAGING_CLIENT_ID,
            ATTRIBUTE_KEY_SERVER_PORT,
            ATTRIBUTE_KEY_SERVER_ADDRESS,
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
            ATTRIBUTE_KEY_ERROR_TYPE,
            ATTRIBUTE_KEY_NETWORK_PEER_IP,
            ATTRIBUTE_KEY_RPC_SYSTEM
    );

    public static final Predicate<String> FILTERED_ATTRIBUTE_KEY = FILTERED_ATTRIBUTE_KEY_SET::contains;
}
