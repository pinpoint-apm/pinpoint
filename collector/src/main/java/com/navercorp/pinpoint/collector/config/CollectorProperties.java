/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class CollectorProperties {
    private final Logger logger = LogManager.getLogger(getClass());

    @Value("${collector.l4.ip:}")
    private String[] l4IpList = new String[0];
    @Value("${collector.metric.jmx:false}")
    private boolean metricJmxEnable;
    @Value("${collector.metric.jmx.domain:pinpoint.collector.metrics}")
    private String metricJmxDomainName;

    @Value("${collector.span.sampling.enable:false}")
    private boolean spanSamplingEnable;
    @Value("${collector.span.sampling.type:MOD}")
    private String spanSamplingType;
    @Value("${collector.span.sampling.mod.sampling-rate:1}")
    private long spanSamplingRate;
    @Value("${collector.span.sampling.percent.sampling-rate:100}")
    private String spanSamplingPercent;

    @Value("${collector.stat.uri:false}")
    private boolean uriStatEnable;
    @Value("${collector.statistics.agent-state.enable:false}")
    private boolean statisticsAgentStateEnable;


    public List<String> getL4IpList() {
        return List.of(l4IpList);
    }

    public void setL4IpList(List<String> l4IpList) {
        Objects.requireNonNull(l4IpList, "l4IpList");
        this.l4IpList = l4IpList.toArray(new String[0]);
    }

    public boolean isMetricJmxEnable() {
        return metricJmxEnable;
    }

    public void setMetricJmxEnable(boolean metricJmxEnable) {
        this.metricJmxEnable = metricJmxEnable;
    }

    public String getMetricJmxDomainName() {
        return metricJmxDomainName;
    }

    public void setMetricJmxDomainName(String metricJmxDomainName) {
        this.metricJmxDomainName = metricJmxDomainName;
    }

    public boolean isSpanSamplingEnable() {
        return spanSamplingEnable;
    }

    public void setSpanSamplingEnable(boolean spanSamplingEnable) {
        this.spanSamplingEnable = spanSamplingEnable;
    }

    public String getSpanSamplingType() {
        return spanSamplingType;
    }

    public void setSpanSamplingType(String spanSamplingType) {
        this.spanSamplingType = spanSamplingType;
    }

    public void setSpanSamplingRate(int spanSamplingRate) {
        this.spanSamplingRate = spanSamplingRate;
    }

    public long getSpanSamplingRate() {
        return spanSamplingRate;
    }

    public String getSpanSamplingPercent() {
        return spanSamplingPercent;
    }

    public void setSpanSamplingPercent(String spanSamplingPercent) {
        this.spanSamplingPercent = spanSamplingPercent;
    }

    public boolean isUriStatEnable() {
        return uriStatEnable;
    }

    public void setUriStatEnable(boolean uriStatEnable) {
        this.uriStatEnable = uriStatEnable;
    }

    public void setStatisticsAgentStateEnable(boolean statisticsAgentStateEnable) {
        this.statisticsAgentStateEnable = statisticsAgentStateEnable;
    }

    public boolean isStatisticsAgentStateEnable() {
        return statisticsAgentStateEnable;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> visitor = new AnnotationVisitor<>(Value.class);
        visitor.visit(this, new LoggingEvent(logger));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CollectorConfiguration{");
        sb.append(", l4IpList=").append(Arrays.toString(l4IpList));
        sb.append(", metricJmxEnable=").append(metricJmxEnable);
        sb.append(", metricJmxDomainName='").append(metricJmxDomainName).append('\'');
        sb.append(", spanSamplingEnable=").append(spanSamplingEnable);
        sb.append(", spanSamplingType=").append(spanSamplingType);
        sb.append(", uriStatEnable=").append(uriStatEnable);
        sb.append(", statisticsAgentStateEnable=").append(statisticsAgentStateEnable);
        sb.append('}');
        return sb.toString();
    }
}
