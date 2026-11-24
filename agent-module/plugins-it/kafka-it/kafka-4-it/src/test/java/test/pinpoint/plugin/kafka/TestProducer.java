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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static test.pinpoint.plugin.kafka.KafkaITConstants.MESSAGE;

/**
 * @author Younsung Hwang
 */
public class TestProducer {

    private final Properties props;

    public TestProducer(String brokerUrl) {
        props = getProducerConfig(brokerUrl);
    }

    private Properties getProducerConfig(String brokerUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // keep the records of one sendMessage() call in a single batch (sent by flush()), so that they are fetched together
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5000");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    /**
     * @return offsets of the sent records, in send order
     */
    public List<Long> sendMessage(String topic, int messageCount, String traceType) {
        List<Future<RecordMetadata>> futures = new ArrayList<>();
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < messageCount; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, traceType, MESSAGE);
                futures.add(producer.send(record));
            }
            producer.flush();

            List<Long> offsets = new ArrayList<>();
            for (Future<RecordMetadata> future : futures) {
                offsets.add(future.get(30, TimeUnit.SECONDS).offset());
            }
            return offsets;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send to " + topic, e);
        }
    }

}
