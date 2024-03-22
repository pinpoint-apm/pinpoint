/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static com.pinpoint.test.plugin.KafkaPluginTestConstants.TRACE_TYPE_MULTI_RECORDS;
import static com.pinpoint.test.plugin.KafkaPluginTestConstants.TRACE_TYPE_RECORD;

public class TestConsumer {
    private final Thread consumerThread;
    private final Poller poller;

    public TestConsumer(String brokerUrl) {
        poller = new Poller(brokerUrl);
        consumerThread = new Thread(poller);
        consumerThread.setDaemon(true);
    }

    public void shutdown() throws InterruptedException {
        poller.shutdown();
        consumerThread.join(100L);
    }

    public void start() {
        consumerThread.start();
    }

    private static class Poller implements Runnable {

        private final KafkaConsumer<String, String> consumer;
        private final TestConsumerRecordEntryPoint entryPoint = new TestConsumerRecordEntryPoint();

        private Poller(String brokerUrl) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(true));
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaPluginTestConstants.GROUP_ID);
            consumer = new KafkaConsumer<>(props);
        }

        private void shutdown() {
            consumer.wakeup();
        }

        @Override
        public void run() {
            consumer.subscribe(Collections.singleton(KafkaPluginTestConstants.TOPIC));
            consumer.poll(Duration.ofMillis(0));
            consumer.seekToBeginning(Collections.singleton(new TopicPartition(KafkaPluginTestConstants.TOPIC, KafkaPluginTestConstants.PARTITION)));
            final int count = 1;

            try {
                for(int i = 0; i < count; i++) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    if(records != null && records.count() > 0) {
                        Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
                        if (!iterator.hasNext()) {
                            continue;
                        }
                        ConsumerRecord<String, String> firstRecord = iterator.next();
                        String traceType = firstRecord.key();
                        if (traceType.equals(TRACE_TYPE_MULTI_RECORDS)) {
                            entryPoint.consumeRecord(records);
                        } else if (traceType.equals(TRACE_TYPE_RECORD)) {
                            records.forEach(entryPoint::consumeRecord);
                        }
                    }
                }
            } catch (WakeupException ignore) {
            }
        }
    }
}
