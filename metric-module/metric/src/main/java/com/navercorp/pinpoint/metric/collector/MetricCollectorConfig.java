package com.navercorp.pinpoint.metric.collector;

import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import com.navercorp.pinpoint.metric.collector.config.MetricCollectorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MetricCollectorConfig {

    @Bean("systemMetricTopicNameManagers")
    List<TopicNameManager> systemMetricTopicNameManagers(MetricCollectorProperties properties) {
        List<TopicNameManager> topicNameManagers = new ArrayList<>();
        if (properties.isSystemMetricDoubleSingleTopicEnabled()) {
            topicNameManagers.add(new TopicNameManager(properties.getSystemMetricDoubleSingleTopicName()));
        }
        if (properties.isSystemMetricDoubleMultiTopicEnabled()) {
            topicNameManagers.add(new TopicNameManager(
                    properties.getSystemMetricDoubleTopicPrefix(),
                    properties.getSystemMetricDoubleTopicPaddingLength(),
                    properties.getSystemMetricDoubleTopicCount())
            );
        }
        return topicNameManagers;
    }
}
