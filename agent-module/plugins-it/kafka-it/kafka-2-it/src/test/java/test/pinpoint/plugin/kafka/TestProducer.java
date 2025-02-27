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

package test.pinpoint.plugin.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static test.pinpoint.plugin.kafka.KafkaITConstants.MESSAGE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TOPIC;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;

/**
 * @author Younsung Hwang
 */
public class TestProducer {

    private final Properties props;

    public TestProducer(String brokerUrl) {
        this.props = getProducerConfig(brokerUrl);
    }

    private Properties getProducerConfig(String brokerUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    public void sendMessage(int messageCount) {
        sendMessage(messageCount, TRACE_TYPE_RECORD);
    }

    public void sendMessage(int messageCount, String traceType) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < messageCount; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, traceType, MESSAGE);
            producer.send(record);
        }
        producer.flush();
        producer.close();
    }
}
