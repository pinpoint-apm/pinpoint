/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.dao.pinot;

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author minwoo.jung
 */
@Repository
public class PinotSystemMetricDataTypeDao implements SystemMetricDataTypeDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotSystemMetricDataTypeDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;
    private final KafkaTemplate<String, MetricData> kafkaDataTypeTemplate;
    private final String topic;

    private final BiConsumer<SendResult<String, MetricData>, Throwable> resultCallback
            = KafkaCallbacks.loggingCallback("Kafka(MetricData)", logger);

    public PinotSystemMetricDataTypeDao(SqlSessionTemplate sqlPinotSessionTemplate,
                             KafkaTemplate<String, MetricData> kafkaDataTypeTemplate,
                             @Value("${kafka.metadata.data.type.topic}") String topic) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.kafkaDataTypeTemplate = Objects.requireNonNull(kafkaDataTypeTemplate, "kafkaDataTypeTemplate");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Deprecated
    @Override
    public List<MetricData> selectMetricDataTypeList() {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectMetricDataTypeList");
    }

    @Override
    public MetricData selectMetricDataType(MetricDataName metricDataName) {
        return sqlPinotSessionTemplate.selectOne(NAMESPACE + "selectMetricDataType", metricDataName);
    }

    @Override
    public void updateMetricDataType(MetricData metricData) {
        CompletableFuture<SendResult<String, MetricData>> callback = kafkaDataTypeTemplate.send(topic, metricData.getMetricName(), metricData);
        callback.whenComplete(resultCallback);
    }
}
