/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author minwoo-jung
 */
@Component
public class OtlpMetricCollectorProperties {

    @Value("${kafka.otlpmetric.topic.metadata}")
    private String metadataTopicName;
    @Value("${kafka.otlpmetric.topic.double.count}")
    private int doubleTopicCount;
    @Value("${kafka.otlpmetric.topic.double.prefix}")
    private String doubleTopicPrefix;
    @Value("${kafka.otlpmetric.topic.double.padding.length}")
    private int doubleTopicPaddingLength;
    @Value("${kafka.otlpmetric.topic.long.count}")
    private int longTopicCount;
    @Value("${kafka.otlpmetric.topic.long.prefix}")
    private String longTopicPrefix;
    @Value("${kafka.otlpmetric.topic.long.padding.length}")
    private int longTopicPaddingLength;

    public String getMetadataTopicName() {
        return metadataTopicName;
    }

    public int getDoubleTopicCount() {
        return doubleTopicCount;
    }

    public String getDoubleTopicPrefix() {
        return doubleTopicPrefix;
    }

    public int getDoubleTopicPaddingLength() {
        return doubleTopicPaddingLength;
    }

    public int getLongTopicCount() {
        return longTopicCount;
    }

    public String getLongTopicPrefix() {
        return longTopicPrefix;
    }

    public int getLongTopicPaddingLength() {
        return longTopicPaddingLength;
    }
}
