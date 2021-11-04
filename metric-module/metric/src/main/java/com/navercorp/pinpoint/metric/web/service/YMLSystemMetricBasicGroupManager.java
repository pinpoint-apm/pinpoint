/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.metric.web.mapping.Mappings;
import com.navercorp.pinpoint.metric.web.mapping.Metric;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author minwoo.jung
 */
@Service
public class YMLSystemMetricBasicGroupManager {

    public static final String TELEGRAF_METRIC = "/pinot-web/telegraf-metric.yml";
    private final Map<String, Metric> definitionIdMap;
    private final Map<String, List<String>> metricIdMap;

    public YMLSystemMetricBasicGroupManager() throws IOException {
        this(new ClassPathResource(TELEGRAF_METRIC));
    }

    public YMLSystemMetricBasicGroupManager(Resource telegrafMetric) throws IOException {
        Objects.requireNonNull(telegrafMetric, "telegrafMetric");

        InputStream stream = telegrafMetric.getInputStream();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Mappings mappings = mapper.readValue(stream, Mappings.class);
        List<Metric> metrics = mappings.getMappings();

        Map<String, Metric> definitionIdMap = new HashMap<>();
        for (Metric metric : metrics) {
            Metric exist = definitionIdMap.put(metric.getDefinitionId(), metric);
            Assert.state(exist == null, "duplicated metric " + metric + " / " + exist);
        }
        this.definitionIdMap = definitionIdMap;


        Map<String, List<String>> metricIdMap = new HashMap<>();
        for (Metric metric : metrics) {
            String definitionId = metric.getDefinitionId();
            metricIdMap.compute(metric.getName(), new BiFunction<String, List<String>, List<String>>() {
                @Override
                public List<String> apply(String metricId, List<String> definitionIdList) {
                    if (definitionIdList == null) {
                        definitionIdList = new ArrayList<>();
                    }
                    definitionIdList.add(definitionId);
                    return definitionIdList;
                }
            });
        }
        this.metricIdMap = metricIdMap;

    }

    public Metric findElementOfBasicGroup(String metricDefinitionId) {
        Metric metric = this.definitionIdMap.get(metricDefinitionId);
        if (metric != null) {
            return metric;
        }
        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public String findMetricName(String metricDefinitionId) {
        Metric metric = this.definitionIdMap.get(metricDefinitionId);
        if (metric != null) {
            return metric.getName();
        }


        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public String findMetricTitle(String metricDefinitionId) {
        Metric metric = this.definitionIdMap.get(metricDefinitionId);
        if (metric != null) {
            return metric.getTitle();
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public GroupingRule findGroupingRule(String metricDefinitionId) {
        Metric metric = this.definitionIdMap.get(metricDefinitionId);
        if (metric != null) {
            return metric.getGrouping();
        }
        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public String findUnit(String metricDefinitionId) {
        Metric metric = this.definitionIdMap.get(metricDefinitionId);
        if (metric != null) {
            return metric.getUnit();
        }
        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public List<String> findMetricDefinitionIdList(String metricName) {
        List<String> definitionIdList = metricIdMap.get(metricName);
        if (definitionIdList != null) {
            return definitionIdList;
        }

        return Collections.emptyList();
    }
}
