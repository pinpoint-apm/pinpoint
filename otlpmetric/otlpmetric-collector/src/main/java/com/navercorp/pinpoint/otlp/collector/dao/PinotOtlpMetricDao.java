/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.dao;

import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.otlp.collector.config.OtlpMetricCollectorProperties;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricDoubleData;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricLongData;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricMetadata;
import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Repository
public class PinotOtlpMetricDao implements OtlpMetricDao {
    private final Logger logger = LogManager.getLogger(getClass());

    @NonNull
    private final KafkaTemplate<String, PinotOtlpMetricMetadata> kafkaOtlpMetadataTemplate;
    @NonNull
    private final KafkaTemplate<String, PinotOtlpMetricLongData> kafkaOtlpLongMetricTemplate;
    @NonNull
    private final KafkaTemplate<String, PinotOtlpMetricDoubleData> kafkaOtlpDoubleMetricTemplate;

    @NonNull
    private final String metadataTopic;
    private final TopicNameManager doubleTopicNameManager;
    private final TopicNameManager longTopicNameManager;

    private final BiConsumer<SendResult<String, PinotOtlpMetricMetadata>, Throwable> metadataResultCallback
            = KafkaCallbacks.loggingCallback("Kafka(OtlpMetric-metadata)", logger);
    private final BiConsumer<SendResult<String, PinotOtlpMetricLongData>, Throwable> longResultCallback
            = KafkaCallbacks.loggingCallback("Kafka(OtlpMetric-long)", logger);
    private final BiConsumer<SendResult<String, PinotOtlpMetricDoubleData>, Throwable> doubleResultCallback
            = KafkaCallbacks.loggingCallback("Kafka(OtlpMetric-double)", logger);

    public PinotOtlpMetricDao(@Qualifier("kafkaOtlpMetadataTemplate") KafkaTemplate<String, PinotOtlpMetricMetadata> kafkaOtlpMetadataTemplate,
                              @Qualifier("kafkaOtlpLongMetricTemplate") KafkaTemplate<String, PinotOtlpMetricLongData> kafkaOtlpLongMetricTemplate,
                              @Qualifier("kafkaOtlpDoubleMetricTemplate") KafkaTemplate<String, PinotOtlpMetricDoubleData> kafkaOtlpDoubleMetricTemplate,
                              OtlpMetricCollectorProperties otlpMetricCollectorProperties) {
        this.kafkaOtlpMetadataTemplate = Objects.requireNonNull(kafkaOtlpMetadataTemplate, "kafkaOtlpMetadataTemplate");
        this.kafkaOtlpLongMetricTemplate = Objects.requireNonNull(kafkaOtlpLongMetricTemplate, "kafkaOtlpLongMetricTemplate");
        this.kafkaOtlpDoubleMetricTemplate = Objects.requireNonNull(kafkaOtlpDoubleMetricTemplate, "kafkaOtlpDoubleMetricTemplate");

        Objects.requireNonNull(otlpMetricCollectorProperties, "otlpMetricCollectorProperties");
        this.metadataTopic = StringPrecondition.requireHasLength(otlpMetricCollectorProperties.getMetadataTopicName(), "metadataTopic");
        this.doubleTopicNameManager = new TopicNameManager(otlpMetricCollectorProperties.getDoubleTopicPrefix(), otlpMetricCollectorProperties.getDoubleTopicPaddingLength(), otlpMetricCollectorProperties.getDoubleTopicCount());
        this.longTopicNameManager = new TopicNameManager(otlpMetricCollectorProperties.getLongTopicPrefix(), otlpMetricCollectorProperties.getLongTopicPaddingLength(), otlpMetricCollectorProperties.getLongTopicCount());
    }

    @Override
    public void updateMetadata(PinotOtlpMetricMetadata metadata) {
        Objects.requireNonNull(metadata);
        CompletableFuture<SendResult<String, PinotOtlpMetricMetadata>> response = this.kafkaOtlpMetadataTemplate.send(metadataTopic, metadata.applicationName(), metadata);
        response.whenComplete(metadataResultCallback);
    }

    @Override
    public void insertDouble(PinotOtlpMetricDoubleData data) {
        Objects.requireNonNull(data);
        String doubleTopic = doubleTopicNameManager.getTopicName(data.getApplicationName());
        CompletableFuture<SendResult<String, PinotOtlpMetricDoubleData>> response = this.kafkaOtlpDoubleMetricTemplate.send(doubleTopic, data.getSortKey(), data);
        response.whenComplete(doubleResultCallback);
    }

    @Override
    public void insertLong(PinotOtlpMetricLongData data) {
        Objects.requireNonNull(data);
        String longTopic = longTopicNameManager.getTopicName(data.getApplicationName());
        CompletableFuture<SendResult<String, PinotOtlpMetricLongData>> response = this.kafkaOtlpLongMetricTemplate.send(longTopic, data.getSortKey(), data);
        response.whenComplete(longResultCallback);
    }
}
