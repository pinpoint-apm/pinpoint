/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.exceptiontrace.collector.dao;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.exceptiontrace.collector.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.collector.mapper.ExceptionMetaDataMapper;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
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

/**
 * @author intr3p1d
 */
@Repository
public class PinotExceptionTraceDao implements ExceptionTraceDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final KafkaTemplate<String, ExceptionMetaDataEntity> kafkaExceptionMetaDataTemplate;

    private final ExceptionMetaDataMapper mapper;

    private final String topic;

    private final BiConsumer<SendResult<String, ExceptionMetaDataEntity>, Throwable> resultCallback
            = KafkaCallbacks.loggingCallback("Kafka(ExceptionMetaDataEntity)", logger);


    public PinotExceptionTraceDao(
            @Qualifier("kafkaExceptionMetaDataTemplate") KafkaTemplate<String, ExceptionMetaDataEntity> kafkaExceptionMetaDataTemplate,
            @Value("${kafka.exception.topic}") String topic,
            ExceptionMetaDataMapper mapper
    ) {
        this.kafkaExceptionMetaDataTemplate = Objects.requireNonNull(kafkaExceptionMetaDataTemplate, "kafkaExceptionMetaDataTemplate");
        this.topic = StringPrecondition.requireHasLength(topic, "topic");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public void insert(List<ExceptionMetaData> exceptionMetaData) {
        Objects.requireNonNull(exceptionMetaData);
        logger.info("Pinot data insert: {}", exceptionMetaData);

        for (ExceptionMetaData e : exceptionMetaData) {
            ExceptionMetaDataEntity dataEntity = mapper.toEntity(e);
            logger.info("data insert {}", dataEntity);
            CompletableFuture<SendResult<String, ExceptionMetaDataEntity>> response = this.kafkaExceptionMetaDataTemplate.send(
                    topic, dataEntity
            );
            response.whenComplete(resultCallback);
        }
    }
}
