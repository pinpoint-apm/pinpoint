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
import com.navercorp.pinpoint.metric.common.model.LongCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricLongDao implements SystemMetricDao<LongCounter> {

    private final KafkaTemplate<String, SystemMetricView> kafkaLongTemplate;

    private final String topic;

    public PinotSystemMetricLongDao(KafkaTemplate<String, SystemMetricView> kafkaLongTemplate,
                                    @Value("${kafka.long.topic}") String topic) {
        this.kafkaLongTemplate = Objects.requireNonNull(kafkaLongTemplate, "kafkaLongTemplate");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Override
    public void insert(String applicationName, List<LongCounter> systemMetrics) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(systemMetrics, "systemMetrics");

        for (LongCounter longCounter : systemMetrics) {
            SystemMetricView systemMetricView = new SystemMetricView(applicationName, longCounter);
            this.kafkaLongTemplate.send(topic, systemMetricView);
        }
    }
}
