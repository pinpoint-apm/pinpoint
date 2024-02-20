/*
 * Copyright 2024 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricDataRow;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricDoubleData;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricLongData;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricMetadata;
import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Repository
public class PinotOtlpMetricDao implements OtlpMetricDao {
    private final Logger logger = LogManager.getLogger(getClass());

    @NotNull private final KafkaTemplate<String, PinotOtlpMetricMetadata> kafkaOtlpMetadataTemplate;
    @NotNull private final KafkaTemplate<String, PinotOtlpMetricLongData> kafkaOtlpLongMetricTemplate;
    @NotNull private final KafkaTemplate<String, PinotOtlpMetricDoubleData> kafkaOtlpDoubleMetricTemplate;

    @NotBlank private final String metadataTopic;
    @NotBlank private final String doubleTopic;
    @NotBlank private final String longTopic;

    private final BiConsumer<SendResult<String, PinotOtlpMetricMetadata>, Throwable> metadataResultCallback
            = KafkaCallbacks.loggingCallback("Kafka(OtlpMetric-metadata)", logger);
    private final BiConsumer<SendResult<String, PinotOtlpMetricLongData>, Throwable> longResultCallback
            = KafkaCallbacks.loggingCallback("Kafka(OtlpMetric-long)", logger);
    private final BiConsumer<SendResult<String, PinotOtlpMetricDoubleData>, Throwable> doubleResultCallback
            = KafkaCallbacks.loggingCallback("Kafka(OtlpMetric-double)", logger);

    public PinotOtlpMetricDao(@Qualifier("kafkaOtlpMetadataTemplate") KafkaTemplate<String, PinotOtlpMetricMetadata> kafkaOtlpMetadataTemplate,
                              @Qualifier("kafkaOtlpLongMetricTemplate") KafkaTemplate<String, PinotOtlpMetricLongData> kafkaOtlpLongMetricTemplate,
                              @Qualifier("kafkaOtlpDoubleMetricTemplate") KafkaTemplate<String, PinotOtlpMetricDoubleData> kafkaOtlpDoubleMetricTemplate,
                              @Value("${kafka.otlpmetric.topic.metadata}") String metadataTopic,
                              @Value("${kafka.otlpmetric.topic.double}") String doubleTopic,
                              @Value("${kafka.otlpmetric.topic.long}") String longTopic) {
        this.kafkaOtlpMetadataTemplate = Objects.requireNonNull(kafkaOtlpMetadataTemplate, "kafkaOtlpMetadataTemplate");
        this.kafkaOtlpLongMetricTemplate = Objects.requireNonNull(kafkaOtlpLongMetricTemplate, "kafkaOtlpLongMetricTemplate");
        this.kafkaOtlpDoubleMetricTemplate = Objects.requireNonNull(kafkaOtlpDoubleMetricTemplate, "kafkaOtlpDoubleMetricTemplate");
        this.metadataTopic = StringPrecondition.requireHasLength(metadataTopic, "metadataTopic");
        this.doubleTopic = StringPrecondition.requireHasLength(doubleTopic, "doubleTopic");
        this.longTopic = StringPrecondition.requireHasLength(longTopic, "longTopic");
    }

    @Override
    public void updateMetadata(PinotOtlpMetricMetadata metadata) {
        Objects.requireNonNull(metadata);
        CompletableFuture<SendResult<String, PinotOtlpMetricMetadata>> response = this.kafkaOtlpMetadataTemplate.send(metadataTopic, metadata);
        response.whenComplete(metadataResultCallback);
    }

    @Override
    public void insertDouble(PinotOtlpMetricDoubleData data) {
        Objects.requireNonNull(data);
        CompletableFuture<SendResult<String, PinotOtlpMetricDoubleData>> response = this.kafkaOtlpDoubleMetricTemplate.send(doubleTopic, data);
        response.whenComplete(doubleResultCallback);
    }

    @Override
    public void insertLong(PinotOtlpMetricLongData data) {
        Objects.requireNonNull(data);
        CompletableFuture<SendResult<String, PinotOtlpMetricLongData>> response = this.kafkaOtlpLongMetricTemplate.send(longTopic, data);
        response.whenComplete(longResultCallback);
    }

}
