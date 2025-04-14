/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.dao.pinot;

import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.collector.view.SystemMetricView;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao<DoubleMetric> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate;

    private final List<TopicNameManager> topicNameManagers;

    private final BiConsumer<SendResult<String, SystemMetricView>, Throwable> resultCallback
            = KafkaCallbacks.loggingCallback("Kafka(SystemMetricView)", logger);

    public PinotSystemMetricDoubleDao(KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate,
                                      @Qualifier("systemMetricTopicNameManagers") List<TopicNameManager> topicNameManagers) {
        this.kafkaDoubleTemplate = Objects.requireNonNull(kafkaDoubleTemplate, "kafkaDoubleTemplate");
        this.topicNameManagers = Objects.requireNonNull(topicNameManagers, "topicNameManagers");
        logger.info("PinotSystemMetricDoubleDao initialized with {} topicNameManagers", topicNameManagers.size());
    }

    @Override
    public void insert(String tenantId, String hostGroupName, String hostName, List<DoubleMetric> systemMetrics) {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(hostGroupName, "hostGroupName");
        Objects.requireNonNull(systemMetrics, "systemMetrics");

        List<String> topicNames = topicNameManagers.stream()
                .map(topicNameManager -> topicNameManager.getTopicName(hostGroupName))
                .toList();
        for (DoubleMetric doubleMetric : systemMetrics) {
            String kafkaKey = generateKafkaKey(doubleMetric);
            SystemMetricView systemMetricView = new SystemMetricView(tenantId, hostGroupName, doubleMetric);
            for (String topic : topicNames) {
                CompletableFuture<SendResult<String, SystemMetricView>> callback = this.kafkaDoubleTemplate.send(topic, kafkaKey, systemMetricView);
                callback.whenComplete(resultCallback);
            }
        }
    }

    private String generateKafkaKey(DoubleMetric doubleMetric) {
        return doubleMetric.getHostName() +
                "_" +
                doubleMetric.getMetricName() +
                "_" +
                doubleMetric.getFieldName();
    }
}
