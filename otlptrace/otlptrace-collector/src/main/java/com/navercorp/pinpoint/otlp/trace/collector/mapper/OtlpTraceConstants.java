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
    public static final String ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET = "messaging.kafka.message.offset";
    public static final String ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID = "messaging.destination.partition.id";
    public static final String ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME = "messaging.destination.name";
    public static final String ATTRIBUTE_KEY_URL_PATH = "url.path";
    public static final String ATTRIBUTE_KEY_HTTP_URL = "http.url";
    public static final String ATTRIBUTE_KEY_HTTP_TARGET = "http.target";
    public static final String ATTRIBUTE_KEY_RPC_SERVICE = "rpc.service";
    public static final String ATTRIBUTE_KEY_RPC_METHOD = "rpc.method";
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

    public static final String ATTRIBUTE_KEY_ERROR_TYPE = "error.type";

    public static final String EVENT_NAME_EXCEPTION = "exception";
    public static final String ATTRIBUTE_KEY_EXCEPTION_TYPE = "exception.type";
    public static final String ATTRIBUTE_KEY_EXCEPTION_MESSAGE = "exception.message";
    public static final String ATTRIBUTE_KEY_EXCEPTION_STACKTRACE = "exception.stacktrace";

    public static final String ATTRIBUTE_KEY_HOST_NAME = "host.name";
    public static final String ATTRIBUTE_KEY_PROCESS_PID = "process.pid";
    public static final String ATTRIBUTE_KEY_PROCESS_RUNTIME_DESCRIPTION = "process.runtime.description";
    public static final String ATTRIBUTE_KEY_TELEMETRY_SDK_VERSION = "telemetry.sdk.version";

    public static final Set<String> FILTERED_ATTRIBUTE_KEY_SET = Set.of(
            ATTRIBUTE_KEY_CLIENT_ADDRESS,
            ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE,
            ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET,
            ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID,
            ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME,
            ATTRIBUTE_KEY_URL_PATH,
            ATTRIBUTE_KEY_MESSAGING_CLIENT_ID,
            ATTRIBUTE_KEY_SERVER_PORT,
            ATTRIBUTE_KEY_SERVER_ADDRESS,
            ATTRIBUTE_KEY_DB_NAME,
            ATTRIBUTE_KEY_DB_NAMESPACE,
            ATTRIBUTE_KEY_DB_STATEMENT,
            ATTRIBUTE_KEY_DB_SYSTEM
    );

    public static final Predicate<String> FILTERED_ATTRIBUTE_KEY = FILTERED_ATTRIBUTE_KEY_SET::contains;

}
