package com.navercorp.pinpoint.metric.collector;

import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import com.navercorp.pinpoint.metric.collector.cache.MetricCacheConfiguration;
import com.navercorp.pinpoint.metric.collector.config.MetricCollectorProperties;
import com.navercorp.pinpoint.metric.collector.config.MetricKafkaConfiguration;
import com.navercorp.pinpoint.metric.common.config.MetricCollectorPinotDaoConfiguration;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.metric.collector.dao",
        "com.navercorp.pinpoint.metric.collector.service",
        "com.navercorp.pinpoint.metric.common.model",
        "com.navercorp.pinpoint.metric.common",
        "com.navercorp.pinpoint.common.server.util",
        "com.navercorp.pinpoint.metric.collector.controller"
})
@Import({
        MetricAppPropertySources.class,
        MetricCacheConfiguration.class,
        PinotConfiguration.class,
        MetricCollectorPinotDaoConfiguration.class,
        MetricKafkaConfiguration.class,
        MetricCollectorProperties.class
})
@ConditionalOnProperty(value = "pinpoint.modules.collector.systemmetric.enabled", havingValue = "true")
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
