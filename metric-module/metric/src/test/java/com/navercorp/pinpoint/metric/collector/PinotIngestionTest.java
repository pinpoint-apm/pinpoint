/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * @author Hyunjoon Cho
 */
public class PinotIngestionTest {
    Random random = new Random();

    @Ignore
    @Test
    public void testFixedMetric() {
        KafkaProducer<String, String> producer = createProducer();

        long time = 0;
        for (int i = 0 ; i < 100000 ; i++) {
            String systemMetric = "{\"applicationName\":\"hyunjoon\", \"tagName\":[\"host\", \"currentTime\"], \"tagValue\":[\"localhost\", \"" + new Date().toString() + "\"], \"eventTime\":" + time + "}";
            producer.send(new ProducerRecord<>("test-topic", systemMetric));
            time += 1000;
        }

        producer.flush();
        producer.close();
    }

    @Ignore
    @Test
    public void testRandomMetric() {

        KafkaProducer<String, String> producer = createProducer();

        long time = 0;
        int num;


        for (int i = 0 ; i < 100000 ; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"applicationName\":\"hyunjoon\", ");
            sb.append("\"metricName\":").append("\"metric").append(getRandomAlphabet()).append("\", ");
            sb.append("\"fieldName\":").append("\"field").append(getRandomAlphabet()).append("\", ");

            sb.append("\"tagName\":[");
            num = random.nextInt(10);
            for (int j = 0; j < num; j++) {
                sb.append("\"name").append(getRandomAlphabet()).append("\", ");
            }
            sb.append("\"currentTime\"], ");

            sb.append("\"tagValue\":[");
            for (int j = 0; j < num; j++) {
                sb.append("\"value").append(getRandomAlphabet()).append("\", ");
            }
            sb.append("\"").append(new Date()).append("\"], ");

            sb.append("\"timestamp\":" + time +"}");

            producer.send(new ProducerRecord<>("random-test-topic", sb.toString()));
            time += 1000;
        }

        producer.flush();
        producer.close();
    }

    private char getRandomAlphabet() {
        if (random.nextBoolean()){
            return (char) (65 + random.nextInt(26));
        } else {
            return (char) (97 + random.nextInt(26));
        }
    }

    private KafkaProducer<String, String> createProducer() {
        Properties configs = new Properties();
        configs.put("bootstrap.servers", "localhost:19092");
        configs.put("acks", "all");
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<>(configs);
    }
}
