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

package com.pinpoint.test.plugin;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * The consumer entry point registered with profiler.kafka.consumer.entryPoint (see the module pom).
 * Prints the trace headers of the records it receives, so that the propagation from the producer can be checked
 * from the app output without a collector.
 */
public class TestConsumerRecordEntryPoint {

    private static final String PINPOINT_HEADER_PREFIX = "Pinpoint-";

    public void consumeRecord(ConsumerRecord<String, String> record) {
        System.out.println("Received record: " + record.value() + ", headers=" + pinpointHeaders(record));
    }

    public void consumeRecord(ConsumerRecords<String, String> records) {
        System.out.println("Received records: " + records.count());
        for (ConsumerRecord<String, String> record : records) {
            System.out.println("  " + record.value() + ", headers=" + pinpointHeaders(record));
        }
    }

    private static Map<String, String> pinpointHeaders(ConsumerRecord<String, String> record) {
        Map<String, String> headers = new TreeMap<>();
        for (Header header : record.headers()) {
            if (header.key().startsWith(PINPOINT_HEADER_PREFIX)) {
                headers.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
            }
        }
        return headers;
    }
}
