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
import com.navercorp.pinpoint.metric.common.model.DoubleCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao<DoubleCounter> {

    private final KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate;

    private final String topic;

    public PinotSystemMetricDoubleDao(KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate,
                                      @Value("${kafka.double.topic}") String topic) {
        this.kafkaDoubleTemplate = Objects.requireNonNull(kafkaDoubleTemplate, "kafkaDoubleTemplate");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Override
    public void insert(String applicationName, List<DoubleCounter> systemMetrics) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(systemMetrics, "systemMetrics");

        for (DoubleCounter doubleCounter : systemMetrics) {
            SystemMetricView systemMetricView = new SystemMetricView(applicationName, doubleCounter);
            this.kafkaDoubleTemplate.send(topic, systemMetricView);
        }
    }
}
