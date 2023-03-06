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

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.collector.view.SystemMetricView;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.util.KafkaCallbacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao<DoubleMetric> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate;

    private final String topic;
    private final String topic2;

    private final ListenableFutureCallback<SendResult<String, SystemMetricView>> resultCallback
            = KafkaCallbacks.loggingCallback("Kafka(SystemMetricView)", logger);

    private final ListenableFutureCallback<SendResult<String, SystemMetricView>> resultCallback2
            = KafkaCallbacks.loggingCallback("Kafka(SystemMetricView2)", logger);


    public PinotSystemMetricDoubleDao(KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate,
                                      @Value("${kafka.double.topic}") String topic) {
        this.kafkaDoubleTemplate = Objects.requireNonNull(kafkaDoubleTemplate, "kafkaDoubleTemplate");
        this.topic = Objects.requireNonNull(topic, "topic");
        this.topic2 = "system-metric-double2";
    }

    @Override
    public void insert(String tenantId, String hostGroupName, String hostName, List<DoubleMetric> systemMetrics) {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(hostGroupName, "hostGroupName");
        Objects.requireNonNull(systemMetrics, "systemMetrics");

        for (DoubleMetric doubleMetric : systemMetrics) {
            SystemMetricView systemMetricView = new SystemMetricView(tenantId, hostGroupName, doubleMetric);
            String kafkaKey = generateKafkaKey(doubleMetric);
            ListenableFuture<SendResult<String, SystemMetricView>> callback = this.kafkaDoubleTemplate.send(topic, kafkaKey, systemMetricView);
            callback.addCallback(resultCallback);

            String kafkaKey2 = generateKafkaKey2(doubleMetric);
            ListenableFuture<SendResult<String, SystemMetricView>> callback2 = this.kafkaDoubleTemplate.send(topic2, kafkaKey2, systemMetricView);
            callback2.addCallback(resultCallback2);
        }
    }

    private String generateKafkaKey(DoubleMetric doubleMetric) {
        return doubleMetric.getHostName() +
                "_" +
                doubleMetric.getMetricName() +
                "_" +
                doubleMetric.getFieldName();
    }

    private String generateKafkaKey2(DoubleMetric doubleMetric) {
        return doubleMetric.getHostName() +
                "_" +
                doubleMetric.getMetricName() +
                "_" +
                doubleMetric.getFieldName() +
                "_" +
                doubleMetric.getEventTime();
    }
}
