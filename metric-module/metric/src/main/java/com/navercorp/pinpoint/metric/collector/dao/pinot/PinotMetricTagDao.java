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

import com.navercorp.pinpoint.metric.collector.dao.MetricTagDao;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class PinotMetricTagDao implements MetricTagDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotMetricTagDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;
    private final KafkaTemplate<String, MetricJsonTag> kafkaTagTemplate;
    private final TagListTypeHandler tagListTypeHandler = new TagListTypeHandler();
    private final String topic;

    public PinotMetricTagDao(SqlSessionTemplate sqlPinotSessionTemplate,
                             KafkaTemplate<String, MetricJsonTag> kafkaTagTemplate,
                             @Value("${kafka.metadata.tag.topic}") String topic) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.kafkaTagTemplate = Objects.requireNonNull(kafkaTagTemplate, "kafkaTagTemplate");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Override
    public void insertMetricTag(MetricTag metricTag) {
        MetricJsonTag metricJsonTag = MetricJsonTag.covertMetricJsonTag(tagListTypeHandler, metricTag);
        kafkaTagTemplate.send(topic, metricTag.getHostGroupName(), metricJsonTag);
    }

    private static class MetricJsonTag {

        private String hostGroupName;
        private String hostName;
        private String metricName;
        private String fieldName;
        private String tags;
        private long saveTime;

        public MetricJsonTag(String hostGroupName, String hostName, String metricName, String fieldName, String jsonTag, long saveTime) {
            this.hostGroupName = hostGroupName;
            this.hostName = hostName;
            this.metricName = metricName;
            this.fieldName = fieldName;
            this.tags = jsonTag;
            this.saveTime = saveTime;
        }

        public String getHostGroupName() {
            return hostGroupName;
        }

        public String getHostName() {
            return hostName;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getTags() {
            return tags;
        }

        public long getSaveTime() {
            return saveTime;
        }

        static MetricJsonTag covertMetricJsonTag(TagListTypeHandler tagListTypeHandler, MetricTag metricTag) {
            String jsonTag = tagListTypeHandler.serialize(metricTag.getTags());
            return new MetricJsonTag(metricTag.getHostGroupName(), metricTag.getHostName(), metricTag.getMetricName(), metricTag.getFieldName(), jsonTag, metricTag.getSaveTime());
        }
    }

    @Override
    public MetricTagCollection selectMetricTag(MetricTagKey metricTagKey) {
        List<MetricTag> metricTagList = sqlPinotSessionTemplate.selectList(NAMESPACE + "selectMetricTagList", metricTagKey);
        return new MetricTagCollection(metricTagKey.getHostGroupName(), metricTagKey.getHostName(), metricTagKey.getMetricName(), metricTagKey.getFieldName(), metricTagList);
    }
}
