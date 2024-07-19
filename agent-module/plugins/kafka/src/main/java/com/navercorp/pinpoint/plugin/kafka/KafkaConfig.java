/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class KafkaConfig {

    // whether this plugin intercepts org.apache.kafka.common.header.Headers
    public static final String HEADER_ENABLE = "profiler.kafka.header.enable";
    public static final String HEADER_REQUEST_ID_ENABLE = "profiler.kafka.requestId.enable";

    static final String ENABLE = "profiler.kafka.enable";
    static final String STREAMS_ENABLE = "profiler.kafka-streams.enable";

    // whether this plugin records kafka headers contents to Pinpoint
    static final String HEADER_RECORD = "profiler.kafka.header.record";

    static final String PRODUCER_ENABLE = "profiler.kafka.producer.enable";

    static final String CONSUMER_ENABLE = "profiler.kafka.consumer.enable";
    static final String CONSUMER_ENTRY_POINT = "profiler.kafka.consumer.entryPoint";

    static final String SPRING_CONSUMER_ENABLE = "profiler.springkafka.consumer.enable";

    private final boolean enable;
    private final boolean streamsEnable;
    private final boolean producerEnable;
    private final boolean consumerEnable;
    private final boolean springConsumerEnable;
    private final boolean headerEnable;
    private final boolean headerRecorded;
    private final boolean headerRequestIdEnable;
    private final String kafkaEntryPoint;

    public KafkaConfig(ProfilerConfig config) {
        this.enable = config.readBoolean(ENABLE, true);
        this.streamsEnable = config.readBoolean(STREAMS_ENABLE, false);
        this.producerEnable = config.readBoolean(PRODUCER_ENABLE, false);
        this.consumerEnable = config.readBoolean(CONSUMER_ENABLE, false);
        this.springConsumerEnable = config.readBoolean(SPRING_CONSUMER_ENABLE, false);
        this.headerEnable = config.readBoolean(HEADER_ENABLE, true);
        this.headerRecorded = config.readBoolean(HEADER_RECORD, true);
        this.headerRequestIdEnable = config.readBoolean(HEADER_REQUEST_ID_ENABLE, false);
        this.kafkaEntryPoint = config.readString(CONSUMER_ENTRY_POINT, "");
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isStreamsEnable() {
        return streamsEnable;
    }

    public boolean isProducerEnable() {
        return producerEnable;
    }

    public boolean isConsumerEnable() {
        return consumerEnable;
    }

    public boolean isSpringConsumerEnable() {
        return springConsumerEnable;
    }

    public boolean isHeaderEnable() {
        return headerEnable;
    }

    public boolean isHeaderRecorded() {
        return headerRecorded;
    }

    public boolean isHeaderRequestIdEnable() {
        return headerRequestIdEnable;
    }

    public String getKafkaEntryPoint() {
        return kafkaEntryPoint;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" +
                "enable=" + enable +
                ", streamsEnable=" + streamsEnable +
                ", producerEnable=" + producerEnable +
                ", consumerEnable=" + consumerEnable +
                ", springConsumerEnable=" + springConsumerEnable +
                ", headerEnable=" + headerEnable +
                ", headerRecorded=" + headerRecorded +
                ", headerRequestIdEnable=" + headerRequestIdEnable +
                ", kafkaEntryPoint='" + kafkaEntryPoint + '\'' +
                '}';
    }
}
