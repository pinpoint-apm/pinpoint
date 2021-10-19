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

import java.util.List;

public class KafkaConfig {

    public static final String HEADER_ENABLE = "profiler.kafka.header.enable";

    static final String PRODUCER_ENABLE = "profiler.kafka.producer.enable";

    static final String CONSUMER_ENABLE = "profiler.kafka.consumer.enable";
    static final String CONSUMER_ENTRY_POINT = "profiler.kafka.consumer.entryPoint";

    static final String SPRING_CONSUMER_ENABLE = "profiler.springkafka.consumer.enable";
    private static final String PRODUCER_TAGS = "pinpoint.kafka.producer.tags";
    static final String CONSUMER_FILTER_TAGS = "pinpoint.kafka.consumer.filter.tags";

    private final boolean producerEnable;
    private final boolean consumerEnable;
    private final boolean springConsumerEnable;
    private final boolean headerEnable;
    private final String kafkaEntryPoint;
    private final String producerTags;
    private final List<String> consumerFilterTagList;

    public KafkaConfig(ProfilerConfig config) {
        this.producerEnable = config.readBoolean(PRODUCER_ENABLE, false);
        this.consumerEnable = config.readBoolean(CONSUMER_ENABLE, false);
        this.springConsumerEnable = config.readBoolean(SPRING_CONSUMER_ENABLE, false);
        this.headerEnable = config.readBoolean(KafkaConfig.HEADER_ENABLE, true);
        this.kafkaEntryPoint = config.readString(CONSUMER_ENTRY_POINT, "");
        this.producerTags = config.readString(PRODUCER_TAGS, "");
        this.consumerFilterTagList = config.readList(CONSUMER_FILTER_TAGS);
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

    public String getKafkaEntryPoint() {
        return kafkaEntryPoint;
    }

    public String getProducerTags() {
        return producerTags;
    }

    public List<String> getConsumerFilterTagList() {
        return consumerFilterTagList;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" +
                "producerEnable=" + producerEnable +
                ", consumerEnable=" + consumerEnable +
                ", springConsumerEnable=" + springConsumerEnable +
                ", headerEnable=" + headerEnable +
                ", kafkaEntryPoint='" + kafkaEntryPoint + '\'' +
                ", producerTags='" + producerTags + '\'' +
                ", consumerFilterTagList=" + consumerFilterTagList +
                '}';
    }
}
