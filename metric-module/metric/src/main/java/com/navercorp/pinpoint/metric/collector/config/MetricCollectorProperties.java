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

package com.navercorp.pinpoint.metric.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author minwoo-jung
 */
@Component
public class MetricCollectorProperties {

    private final Logger logger = LogManager.getLogger(MetricCollectorProperties.class);

    @Value("${metric.cache.tag.init:1000}")
    private int metricTagCacheInitSize;

    @Value("${metric.cache.tag.size:10000}")
    private int metricTagCacheSize;

    @Value("${kafka.systemmetric.double.single.topic.enabled:true}")
    private boolean systemMetricDoubleSingleTopicEnabled;
    @Value("${kafka.systemmetric.double.single.topic:system-metric-double}")
    private String systemMetricDoubleSingleTopicName;

    @Value("${kafka.systemmetric.double.multi.topic.enabled:false}")
    private boolean systemMetricDoubleMultiTopicEnabled;
    @Value("${kafka.systemmetric.double.topic.prefix:systemMetricDouble}")
    private String systemMetricDoubleTopicPrefix;
    @Value("${kafka.systemmetric.double.topic.table.padding.length:2}")
    private int systemMetricDoubleTopicPaddingLength;
    @Value("${kafka.systemmetric.double.topic.count:1}")
    private int systemMetricDoubleTopicCount;

    public int getMetricTagCacheSize() {
        return metricTagCacheSize;
    }

    public int getMetricTagCacheInitSize() {
        return metricTagCacheInitSize;
    }

    public boolean isSystemMetricDoubleSingleTopicEnabled() {
        return systemMetricDoubleSingleTopicEnabled;
    }

    public String getSystemMetricDoubleSingleTopicName() {
        return systemMetricDoubleSingleTopicName;
    }

    public boolean isSystemMetricDoubleMultiTopicEnabled() {
        return systemMetricDoubleMultiTopicEnabled;
    }

    public String getSystemMetricDoubleTopicPrefix() {
        return systemMetricDoubleTopicPrefix;
    }

    public int getSystemMetricDoubleTopicPaddingLength() {
        return systemMetricDoubleTopicPaddingLength;
    }

    public int getSystemMetricDoubleTopicCount() {
        return systemMetricDoubleTopicCount;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    @Override
    public String toString() {
        return "MetricCollectorProperties{" +
                "metricTagCacheInitSize=" + metricTagCacheInitSize +
                ", metricTagCacheSize=" + metricTagCacheSize +
                ", systemMetricDoubleSingleTopicEnabled=" + systemMetricDoubleSingleTopicEnabled +
                ", systemMetricDoubleSingleTopicName='" + systemMetricDoubleSingleTopicName + '\'' +
                ", systemMetricDoubleMultiTopicEnabled=" + systemMetricDoubleMultiTopicEnabled +
                ", systemMetricDoubleTopicPrefix='" + systemMetricDoubleTopicPrefix + '\'' +
                ", systemMetricDoubleTopicPaddingLength=" + systemMetricDoubleTopicPaddingLength +
                ", systemMetricDoubleTopicCount=" + systemMetricDoubleTopicCount +
                '}';
    }
}
