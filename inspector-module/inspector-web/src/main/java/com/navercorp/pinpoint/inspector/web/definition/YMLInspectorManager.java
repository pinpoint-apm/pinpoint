/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.metric.web.mapping.Metric;
import com.navercorp.pinpoint.metric.web.model.MetricInfo;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author minwoo.jung
 */
@Component
// TODO : (minwoo) It seems that it can be integrated with the metric's com.navercorp.pinpoint.metric.web.service.YMLSystemMetricBasic GroupManager.
public class YMLInspectorManager {

    public static final String TELEGRAF_METRIC = "/inspector/web/inspector-definition.yml";
    private final Map<String, MetricDefinition> definitionIdMap;
    private final Map<String, List<String>> metricIdMap;
    private final Comparator<MetricInfo> metricInfoComparator;

    public YMLInspectorManager() throws IOException {
        this(new ClassPathResource(TELEGRAF_METRIC));
    }

    public YMLInspectorManager(ClassPathResource inspectorDefinitionFile) throws IOException {
        Objects.requireNonNull(inspectorDefinitionFile, "inspectorDefinitionFile");

        InputStream stream = inspectorDefinitionFile.getInputStream();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Mappings mappings = mapper.readValue(stream, Mappings.class);
        List<MetricDefinition> metricDefinitions = mappings.getMappings();

        Map<String, MetricDefinition> definitionIdMap = new HashMap<>();
        for (MetricDefinition metric : metricDefinitions) {
            MetricDefinition exist = definitionIdMap.put(metric.getDefinitionId(), metric);
            Assert.state(exist == null, "duplicated metric " + metric + " / " + exist);
        }
        this.definitionIdMap = definitionIdMap;

        Map<String, List<String>> metricIdMap = new HashMap<>();
        for (MetricDefinition metric : metricDefinitions) {
            String definitionId = metric.getDefinitionId();
            metricIdMap.compute(metric.getMetricName(), new BiFunction<String, List<String>, List<String>>() {
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

        List<String> metricIdSortOrder = new ArrayList<>(metricDefinitions.size());
        for (MetricDefinition metric : metricDefinitions) {
            metricIdSortOrder.add(metric.getDefinitionId());
        }
        metricInfoComparator = Comparator.comparing(metricInfo -> metricIdSortOrder.indexOf(metricInfo.getMetricDefinitionId()));
    }

    public MetricDefinition findElementOfBasicGroup(String metricDefinitionId) {
        MetricDefinition metricDefinition = this.definitionIdMap.get(metricDefinitionId);
        if (metricDefinition != null) {
            return metricDefinition;
        }
        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public static boolean containedMatchingRule(MetricDefinition metricDefinition, MatchingRule matchingRule) {
        for (Field field : metricDefinition.getFields()) {
            if (matchingRule.equals(field.getMatchingRule())) {
                return true;
            }
        }

        return false;
    }
}
