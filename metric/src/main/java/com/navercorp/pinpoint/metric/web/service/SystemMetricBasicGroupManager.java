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

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.ElementOfBasicGroup;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricBasicGroupManager {
    private static final String CPU_DEFINITION_ID = "cpu";
    private static final String CPU_METRIC_NAME = "cpu";
    private static final String CPU_TITLE = "cpu";
    private static final List<ElementOfBasicGroup> CPU_METRIC;

    private static final String MEMORY_PERCENT_DEFINITION_ID = "memoryPercent";
    private static final String MEMORY_PERCENT_METRIC_NAME = "mem";
    private static final String MEMORY_PERCENT_TITLE = "memory usage percent";
    private static final List<ElementOfBasicGroup> MEMORY_PERCENT_METRIC;

    private static final String MEMORY_USAGE_DEFINITION_ID = "memoryUsage";
    private static final String MEMORY_USAGE_METRIC_NAME = "mem";
    private static final String MEMORY_USAGE_TITLE = "memory usage";
    private static final List<ElementOfBasicGroup> MEMORY_USAGE_METRIC;

    static {
        //CPU
        List<Tag> tagList = new ArrayList<Tag>(1);
        Tag tag = new Tag("cpu", "cpu-total");
        tagList.add(tag);

        ElementOfBasicGroup  cpuUsageUser = new ElementOfBasicGroup(CPU_METRIC_NAME, "usage_user", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  cpuUsageSystem = new ElementOfBasicGroup(CPU_METRIC_NAME, "usage_system", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  cpuUsageIdle = new ElementOfBasicGroup(CPU_METRIC_NAME, "usage_idle", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(3);
        elementOfBasicGroupList.add(cpuUsageUser);
        elementOfBasicGroupList.add(cpuUsageSystem);
        elementOfBasicGroupList.add(cpuUsageIdle);

        CPU_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //memory usage percent
        List<Tag> tagList = new ArrayList<Tag>(0);

        ElementOfBasicGroup  memoryUsedPercent = new ElementOfBasicGroup(MEMORY_PERCENT_METRIC_NAME, "used_percent", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(1);
        elementOfBasicGroupList.add(memoryUsedPercent);

        MEMORY_PERCENT_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //memory usage
        List<Tag> tagList = new ArrayList<Tag>(0);

        ElementOfBasicGroup  memoryTotal = new ElementOfBasicGroup(MEMORY_USAGE_METRIC_NAME, "total", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  memoryUsed = new ElementOfBasicGroup(MEMORY_USAGE_METRIC_NAME, "used", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(2);
        elementOfBasicGroupList.add(memoryTotal);
        elementOfBasicGroupList.add(memoryUsed);

        MEMORY_USAGE_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }


    public List<ElementOfBasicGroup> findElementOfBasicGroup(String metricDefinitionId) {
        if (CPU_DEFINITION_ID.equals(metricDefinitionId)) {
            return CPU_METRIC;
        } else if (MEMORY_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_PERCENT_METRIC;
        } else if (MEMORY_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_USAGE_METRIC;
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public String findMetricTitle(String metricDefinitionId) {
        if (CPU_DEFINITION_ID.equals(metricDefinitionId)) {
            return CPU_TITLE;
        } else if (MEMORY_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_PERCENT_TITLE;
        } else if (MEMORY_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_USAGE_TITLE;
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);

    }
}
