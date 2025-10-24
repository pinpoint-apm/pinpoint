/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.inspector.web.dao.ApplicationStatDao;
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
 * @author minwoo-jung
 */
@Component
public class UsingDataSourceTagForApplicationPreProcessor implements MetricPreProcessor {

    private final static String JDBC_URL = "jdbcUrl";
    private final ApplicationStatDao applicationStatDao;

    public UsingDataSourceTagForApplicationPreProcessor(@Qualifier("pinotApplicationStatDao") ApplicationStatDao applicationStatDao) {
        this.applicationStatDao = Objects.requireNonNull(applicationStatDao, "applicationStatDao");
    }

    @Override
    public String getName() {
        return "usingDataSourceTagForApplication";
    }

    @Override
    public MetricDefinition preProcess(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<Field> newFieldList = new ArrayList<>(metricDefinition.getFields().size());

        //TODO : (minwoo) Performance improvement, it seems that you need to import jdbcurl only once and fill the rest of the fields with data, rather than doing it in turn.
        for (Field field : metricDefinition.getFields()) {
            if (!MatchingRule.ALL.equals(field.getMatchingRule())) {
                continue;
            }

            List<Tag> tagList = applicationStatDao.getTagInfo(inspectorDataSearchKey, metricDefinition.getMetricName(), field);

            for (Tag tag : tagList) {
                List<Tag> newTagList = new ArrayList<Tag>(1);
                newTagList.add(tag);
                newFieldList.add(new Field(field.getFieldName(), field.getFieldAlias(), newTagList, field.getMatchingRule(), field.getAggregationFunction(), field.getChartType(), field.getUnit(), field.getPostProcess()));
            }
        }

        return new MetricDefinition(metricDefinition.getDefinitionId(), metricDefinition.getMetricName(), metricDefinition.getTitle(), metricDefinition.getGroupingRule(), metricDefinition.getPreProcess(), metricDefinition.getPostProcess(), newFieldList);
    }
}

