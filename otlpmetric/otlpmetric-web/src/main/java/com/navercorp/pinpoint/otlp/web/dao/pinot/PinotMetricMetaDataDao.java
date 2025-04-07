/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.web.dao.pinot;

import com.navercorp.pinpoint.otlp.common.web.definition.property.MetricDescriptor;
import com.navercorp.pinpoint.otlp.common.web.definition.property.MetricGroup;
import com.navercorp.pinpoint.otlp.web.dao.MetricDefinitionDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
@Repository
public class PinotMetricMetaDataDao implements MetricDefinitionDao {

    private static final String NAMESPACE = PinotMetricMetaDataDao.class.getName() + ".";
    private final SqlSessionTemplate syncTemplate;

    private static final Comparator<MetricGroup> COMPARATOR = Comparator.comparing(MetricGroup::getMetricGroupName);

    public PinotMetricMetaDataDao(@Qualifier("otlpMetricPinotSessionTemplate") SqlSessionTemplate syncTemplate) {
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
    }

    @Override
    public List<MetricGroup> getMetricGroupList(String applicationName) {
        List<MetricDescriptor> metricDescriptorList = syncTemplate.selectList(NAMESPACE + "selectMetricDescriptorList", applicationName);

        final Map<String, MetricGroup> metricGroupMap = new HashMap<>();
        for (MetricDescriptor metricDescriptor : metricDescriptorList) {
            final String key = metricDescriptor.metricGroupName();
            MetricGroup metricGroup = metricGroupMap.computeIfAbsent(key, k -> new MetricGroup(key));
            metricGroup.addUniqueMetric(metricDescriptor);
        }

        return sortedMetricGroups(metricGroupMap);
    }

    private List<MetricGroup> sortedMetricGroups(Map<String, MetricGroup> metricGroupMap) {
        List<MetricGroup> list = new ArrayList<>(metricGroupMap.size());
        list.addAll(metricGroupMap.values());
        list.sort(COMPARATOR);
        return list;
    }
}
