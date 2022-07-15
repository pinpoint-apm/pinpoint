/*
 * Copyright 2020 NAVER Corp.
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

package test.pinpoint.plugin.kafka;

import java.util.UUID;

/**
 * @author Younsung Hwang
 */
public class KafkaITConstants {
    public static final String BROKER_URL = "localhost:9092";
    public static final String TOPIC = "kafka-it-topic";
    public static final String MESSAGE = "message-" + UUID.randomUUID().toString();
    public static final String GROUP_ID = "kafka-it-group";
    public static final String KAFKA_CLIENT_SERVICE_TYPE = "KAFKA_CLIENT";
    public static final String KAFKA_CLIENT_INTERNAL_SERVICE_TYPE = "KAFKA_CLIENT_INTERNAL";
    public static final int PARTITION = 0;
    public static final String TRACE_TYPE_RECORD = "RECORD";
    public static final String TRACE_TYPE_MULTI_RECORDS = "MULTI_RECORDS";
    public static final long MAX_TRACE_WAIT_TIME = 10000;
}
