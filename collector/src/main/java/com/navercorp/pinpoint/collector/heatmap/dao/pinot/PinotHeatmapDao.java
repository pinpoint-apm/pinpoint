/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.dao.pinot;

import com.navercorp.pinpoint.collector.heatmap.config.HeatmapProperties;
import com.navercorp.pinpoint.collector.heatmap.dao.HeatmapDao;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStat;
import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
@Repository
public class PinotHeatmapDao implements HeatmapDao {

    private final Logger logger = LogManager.getLogger(getClass());
    private final KafkaTemplate<String, HeatmapStat> kafkaHeatmapStatTemplate;
    private final TopicNameManager topicNameManager;

    public PinotHeatmapDao(KafkaTemplate<String, HeatmapStat> kafkaHeatmapStatTemplate, HeatmapProperties heatmapProperties) {
        this.kafkaHeatmapStatTemplate = Objects.requireNonNull(kafkaHeatmapStatTemplate, "kafkaHeatmapStatTemplate");
        this.topicNameManager = new TopicNameManager(heatmapProperties.getHeatmapTopicPrefix(), heatmapProperties.getHeatMapTopicPaddingLength(), heatmapProperties.getHeatmapTopicCount());
    }

    @Override
    public void insert(HeatmapStat heatmapStat) {
        if (heatmapStat.getElapsedTime() < 0) {
            logger.warn("elapsedTime is negative. {}", heatmapStat);
            return;
        }
        String topic = topicNameManager.getTopicName(heatmapStat.getApplicationName());
        kafkaHeatmapStatTemplate.send(topic, heatmapStat.getAgentId(), heatmapStat);
    }
}
