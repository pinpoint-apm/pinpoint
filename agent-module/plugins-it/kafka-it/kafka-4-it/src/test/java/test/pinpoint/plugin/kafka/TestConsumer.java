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

package test.pinpoint.plugin.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static test.pinpoint.plugin.kafka.KafkaITConstants.GROUP_ID;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;

/**
 * Must be created by the IT class (not by the shared lifecycle class), otherwise the consumer is not instrumented.
 *
 * @author Younsung Hwang
 */
public class TestConsumer {

    private final Thread consumerThread;
    private final Poller poller;

    public TestConsumer(String brokerUrl, String topic, String groupProtocol) {
        poller = new Poller(brokerUrl, topic, groupProtocol);
        consumerThread = new Thread(poller, topic + "-test-poller");
        consumerThread.setDaemon(true);
    }

    public void shutdown() throws InterruptedException {
        poller.shutdown();
        consumerThread.join(10000L);
    }

    public void start() {
        consumerThread.start();
    }

    private static class Poller implements Runnable {

        private final Logger logger = LogManager.getLogger(getClass());

        private final KafkaConsumer<String, String> consumer;
        private final String topic;

        private Poller(String brokerUrl, String topic, String groupProtocol) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(true));
            // own group on an own topic: nothing is missed even if a record is sent before the assignment completes
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaITConstants.unique(GROUP_ID));
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.GROUP_PROTOCOL_CONFIG, groupProtocol);
            this.consumer = new KafkaConsumer<>(props);
            this.topic = topic;
        }

        private void shutdown() {
            consumer.wakeup();
        }

        @Override
        public void run() {
            TestConsumerRecordEntryPoint entryPoint = new TestConsumerRecordEntryPoint();
            try {
                consumer.subscribe(Collections.singleton(topic));
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
                    Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
                    if (!iterator.hasNext()) {
                        continue;
                    }
                    String traceType = iterator.next().key();
                    if (TRACE_TYPE_MULTI_RECORDS.equals(traceType)) {
                        entryPoint.consumeRecord(records);
                    } else if (TRACE_TYPE_RECORD.equals(traceType)) {
                        records.forEach(entryPoint::consumeRecord);
                    }
                }
            } catch (WakeupException e) {
                logger.info("shutdown polling...");
            } catch (Throwable th) {
                logger.warn("polling failed", th);
            } finally {
                consumer.close();
            }
        }
    }

}
