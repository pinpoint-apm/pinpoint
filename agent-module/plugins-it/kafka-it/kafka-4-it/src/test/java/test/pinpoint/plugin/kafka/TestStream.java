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
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Must be created by the IT class (not by the shared lifecycle class), otherwise the streams application is not
 * instrumented.
 */
public class TestStream {
    private final KafkaStreams streams;
    private final CountDownLatch running = new CountDownLatch(1);

    public TestStream(String brokerUrl, String inputTopic, String outputTopic) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream(inputTopic);
        source.to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        this.streams = new KafkaStreams(builder.build(), createStreamsConfig(brokerUrl));
        this.streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING) {
                running.countDown();
            }
        });
    }

    private Properties createStreamsConfig(String brokerUrl) {
        final Properties props = new Properties();
        // own application id (= consumer group) on own topics, see KafkaITConstants.unique()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, KafkaITConstants.unique("kafka-streams-test"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(StreamsConfig.STATE_DIR_CONFIG, createStateDir());
        // CACHE_MAX_BYTES_BUFFERING_CONFIG used by kafka-3-it was removed in kafka-streams 4.0
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    private String createStateDir() {
        try {
            return Files.createTempDirectory("kafka-streams-it").toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create state dir", e);
        }
    }

    public void start() throws InterruptedException {
        streams.start();
        if (!running.await(60, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Streams not running. state=" + streams.state());
        }
    }

    public void shutdown() {
        streams.close(Duration.ofSeconds(30));
        streams.cleanUp();
    }
}
