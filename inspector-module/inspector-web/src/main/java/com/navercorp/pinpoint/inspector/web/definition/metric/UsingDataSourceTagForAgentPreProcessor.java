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

package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.inspector.web.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component
public class UsingDataSourceTagForAgentPreProcessor implements MetricPreProcessor {

    private final static String JDBC_URL = "jdbcUrl";
    private final AgentStatDao agentStatDao;

    private final AgentStatDao agentStatDaoV2;

    public UsingDataSourceTagForAgentPreProcessor(@Qualifier("pinotAgentStatDao")AgentStatDao agentStatDao, @Qualifier("pinotAgentStatDaoV2")AgentStatDao agentStatDaoV2) {
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");
        this.agentStatDaoV2 = Objects.requireNonNull(agentStatDaoV2, "agentStatDao");
    }


    @Override
    public String getName() {
        return "usingDataSourceTagForAgent";
    }

    @Override
    public MetricDefinition preProcess(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<Field> newFieldList = new ArrayList<>(metricDefinition.getFields().size());

        //TODO : (minwoo) Performance improvement, it seems that you need to import jdbcurl only once and fill the rest of the fields with data, rather than doing it in turn.
        for (Field field : metricDefinition.getFields()) {
            if (!field.getMatchingRule().equals(MatchingRule.ALL)) {
                continue;
            }

            List<Tag> tagList = getTagList(inspectorDataSearchKey, metricDefinition, field);

            List<Tag> filteredTagList = new ArrayList<>();
            for (Tag tag : tagList) {
                if (tag.getName().equals(JDBC_URL)) {
                    filteredTagList.add(tag);
                }
            }

            for (Tag filteredTag : filteredTagList) {
                TagInformation tagInfo = getTagInformation(inspectorDataSearchKey, metricDefinition, field, filteredTag);
                newFieldList.add(new Field(field.getFieldName(), field.getFieldAlias(), tagInfo.tags(), field.getMatchingRule(), field.getAggregationFunction(), field.getChartType(), field.getUnit(), field.getPostProcess()));
            }
        }

        return new MetricDefinition(metricDefinition.getDefinitionId(), metricDefinition.getMetricName(), metricDefinition.getTitle(), metricDefinition.getGroupingRule(), metricDefinition.getPreProcess(), metricDefinition.getPostProcess(), newFieldList);
    }

    private TagInformation getTagInformation(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition, Field field, Tag filteredTag) {
        if (inspectorDataSearchKey.getVersion() == 2) {
            return agentStatDaoV2.getTagInfoContainedSpecificTag(inspectorDataSearchKey, metricDefinition.getMetricName(), field, filteredTag);
        } else {
            return agentStatDao.getTagInfoContainedSpecificTag(inspectorDataSearchKey, metricDefinition.getMetricName(), field, filteredTag);
        }
    }

    private List<Tag> getTagList(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition, Field field) {
        if (inspectorDataSearchKey.getVersion() == 2) {
            return agentStatDaoV2.getTagInfo(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
        } else {
            return agentStatDao.getTagInfo(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
        }
    }
}
