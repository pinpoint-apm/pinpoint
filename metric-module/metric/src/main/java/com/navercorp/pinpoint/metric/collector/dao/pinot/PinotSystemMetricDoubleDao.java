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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao<DoubleMetric> {

    private final KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate;

    private final String topic;

    public PinotSystemMetricDoubleDao(KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate,
                                      @Value("${kafka.double.topic}") String topic) {
        this.kafkaDoubleTemplate = Objects.requireNonNull(kafkaDoubleTemplate, "kafkaDoubleTemplate");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Override
    public void insert(String tenantId, String hostGroupName, String hostName, List<DoubleMetric> systemMetrics) {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(hostGroupName, "hostGroupName");
        Objects.requireNonNull(systemMetrics, "systemMetrics");

        for (DoubleMetric doubleMetric : systemMetrics) {
            String kafkaKey = generateKafkaKey(doubleMetric);
            SystemMetricView systemMetricView = new SystemMetricView(tenantId, hostGroupName, doubleMetric);
            this.kafkaDoubleTemplate.send(topic, kafkaKey, systemMetricView);
        }
    }

    private String generateKafkaKey(DoubleMetric doubleMetric) {
        StringBuilder sb = new StringBuilder();
        sb.append(doubleMetric.getHostName());
        sb.append("_");
        sb.append(doubleMetric.getMetricName());
        sb.append("_");
        sb.append(doubleMetric.getFieldName());
        return sb.toString();
    }
}
