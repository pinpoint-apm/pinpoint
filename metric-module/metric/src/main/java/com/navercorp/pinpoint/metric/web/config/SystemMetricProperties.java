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

package com.navercorp.pinpoint.metric.web.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author minwoo-jung
 */
public class SystemMetricProperties {

    private final Logger logger = LogManager.getLogger(SystemMetricProperties.class);

    @Value("${pinot.systemmetric.double.table.mode:single}") //single or multi
    private String systemMetricDoubleTableMode;
    // single
    @Value("${pinot.systemmetric.double.single.table.name:systemMetricDouble}")
    private String systemMetricDoubleSingleTableName;
    // multi
    @Value("${pinot.systemmetric.double.table.prefix:systemMetricDouble}")
    private String systemMetricTablePrefix;
    @Value("${pinot.systemmetric.double.table.padding.length:2}")
    private int systemMetricTablePaddingLength;
    @Value("${pinot.systemmetric.double.table.count:1}")
    private int systemMetricTableCount;

    @Value("${web.systemmetric.api.period.max:28}")
    private int systemMetricPeriodMax;

    @Value("${web.systemmetric.api.period.interval:5m,20m,1h,3h,6h,12h,1d,2d,1w,2w,4w}")
    private List<String> systemMetricPeriodInteval;

    public String getSystemMetricDoubleTableMode() {
        return systemMetricDoubleTableMode;
    }

    public String getSystemMetricDoubleSingleTableName() {
        return systemMetricDoubleSingleTableName;
    }

    public int getSystemMetricTableCount() {
        return systemMetricTableCount;
    }

    public String getSystemMetricTablePrefix() {
        return systemMetricTablePrefix;
    }

    public int getSystemMetricTablePaddingLength() {
        return systemMetricTablePaddingLength;
    }

    public int getSystemMetricPeriodMax() {
        return systemMetricPeriodMax;
    }

    public List<String> getSystemMetricPeriodInteval() {
        return systemMetricPeriodInteval;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    @Override
    public String toString() {
        return "SystemMetricProperties{" +
                "systemMetricDoubleTableMode='" + systemMetricDoubleTableMode + '\'' +
                ", systemMetricDoubleSingleTableName='" + systemMetricDoubleSingleTableName + '\'' +
                ", systemMetricTablePrefix='" + systemMetricTablePrefix + '\'' +
                ", systemMetricTablePaddingLength=" + systemMetricTablePaddingLength +
                ", systemMetricTableCount=" + systemMetricTableCount +
                ", systemMetricPeriodMax=" + systemMetricPeriodMax +
                ", systemMetricPeriodInteval=" + systemMetricPeriodInteval +
                '}';
    }
}
