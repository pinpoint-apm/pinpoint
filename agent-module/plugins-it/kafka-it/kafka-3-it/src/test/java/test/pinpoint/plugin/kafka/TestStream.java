/*
 * Copyright 2023 NAVER Corp.
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
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

import static test.pinpoint.plugin.kafka.KafkaITConstants.INPUT_TOPIC;
import static test.pinpoint.plugin.kafka.KafkaITConstants.OUTPUT_TOPIC;

public class TestStream {
    private final KafkaStreams streams;

    public TestStream(String brokerUrl) {
        final StreamsBuilder builder = new StreamsBuilder();
        createStream(builder);
        final Properties streamsConfig = createStreamsConfig(brokerUrl);
        streams = new KafkaStreams(builder.build(), streamsConfig);
        System.out.println(streams.state());
    }

    private Properties createStreamsConfig(String brokerUrl) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    private void createStream(final StreamsBuilder builder) {
        final KStream<String, String> source = builder.stream(INPUT_TOPIC);
        // need to override value serde to Long type
        source.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

    public void start() {
        streams.start();
        System.out.println("Stream started ");
        System.out.println(streams.state());
    }

    public void shutdown() throws StreamsException {
        System.out.println(streams.state());
        streams.close();
        System.out.println(streams.state());
    }
}
