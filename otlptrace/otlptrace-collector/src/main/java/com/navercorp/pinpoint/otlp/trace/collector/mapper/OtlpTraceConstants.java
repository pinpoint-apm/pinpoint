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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OtlpTraceConstants {
    public static final String ATTRIBUTE_KEY_CLIENT_ADDRESS = "client.address";
    public static final String ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    public static final String ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET = "messaging.kafka.message.offset";
    public static final String ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID = "messaging.destination.partition.id";
    public static final String ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME = "messaging.destination.name";
    public static final String ATTRIBUTE_KEY_URL_PATH = "url.path";
    public static final String ATTRIBUTE_KEY_MESSAGING_CLIENT_ID = "messaging.client_id";
    public static final String ATTRIBUTE_KEY_SERVER_PORT = "server.port";
    public static final String ATTRIBUTE_KEY_SERVER_ADDRESS = "server.address";
    public static final String ATTRIBUTE_KEY_DB_NAME = "db.name";
    public static final String ATTRIBUTE_KEY_DB_STATEMENT = "db.statement";
    public static final String ATTRIBUTE_KEY_DB_SYSTEM = "db.system";

    public static final Map<String, Boolean> FILTERED_ATTRIBUTE_KEY_MAP = Stream.of(
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
            ATTRIBUTE_KEY_DB_STATEMENT,
            ATTRIBUTE_KEY_DB_SYSTEM).collect(Collectors.toUnmodifiableMap(key -> key, value -> Boolean.TRUE));
}
