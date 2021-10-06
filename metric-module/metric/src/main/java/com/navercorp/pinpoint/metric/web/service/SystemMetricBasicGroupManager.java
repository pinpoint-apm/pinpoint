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
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricBasicGroupManager {

    private static final String UNIT_PERCENT = "percent";
    private static final String UNIT_COUNT = "count";
    private static final String UNIT_BYTE = "byte";

    //TODO : (minwoo) 추후 좀더 정의가 쌓이면 domain(객체)로 만들예정, 현재로써는 구조를 잡기보다는 데이터 조합 사례가 더 쌓여야할 필요가 있음.
    private static final String CPU_METRIC_NAME = "cpu";
    private static final String CPU_DEFINITION_ID = "cpu";
    private static final String CPU_TITLE = "cpu";
    private static final GroupingRule CPU_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> CPU_METRIC;
    private static final String CPU_UNIT = UNIT_PERCENT;

    private static final String MEMORY_METRIC_NAME = "mem";

    private static final String MEMORY_PERCENT_METRIC_NAME = MEMORY_METRIC_NAME;
    private static final String MEMORY_PERCENT_DEFINITION_ID = "memoryPercent";
    private static final String MEMORY_PERCENT_TITLE = "memory usage percent";
    private static final GroupingRule MEMORY_PERCENT_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> MEMORY_PERCENT_METRIC;
    private static final String MEMORY_PERCENT_UNIT = UNIT_PERCENT;

    private static final String MEMORY_USAGE_METRIC_NAME = MEMORY_METRIC_NAME;
    private static final String MEMORY_USAGE_DEFINITION_ID = "memoryUsage";
    private static final String MEMORY_USAGE_TITLE = "memory usage";
    private static final GroupingRule MEMORY_USAGE_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> MEMORY_USAGE_METRIC;
    private static final String MEMORY_USAGE_UNIT = UNIT_BYTE;

    private static final String DISK_METRIC_NAME = "disk";

    private static final String DISK_USAGE_METRIC_NAME = DISK_METRIC_NAME;
    private static final String DISK_USAGE_DEFINITION_ID = "diskUsage";
    private static final String DISK_USAGE_TITLE = "disk usage";
    private static final GroupingRule DISK_USAGE_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> DISK_USAGE_METRIC;
    private static final String DISK_USAGE_UNIT = UNIT_BYTE;

    private static final String DISK_PERCENT_METRIC_NAME = DISK_METRIC_NAME;
    private static final String DISK_PERCENT_DEFINITION_ID = "diskPercent";
    private static final String DISK_PERCENT_TITLE = "disk usage percent";
    private static final GroupingRule DISK_PERCENT_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> DISK_PERCENT_METRIC;
    private static final String DISK_PERCENT_UNIT = UNIT_PERCENT;

    private static final String DISK_INODE_METRIC_NAME = DISK_METRIC_NAME;
    private static final String DISK_INODE_DEFINITION_ID = "inodeUsage";
    private static final String DISK_INODE_TITLE = "inode usage percent";
    private static final GroupingRule DISK_INODE_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> DISK_INODE_METRIC;
    private static final String DISK_INODE_UNIT = UNIT_COUNT;

    private static final String SYSTEM_LOAD_METRIC_NAME = "system";
    private static final String SYSTEM_LOAD_DEFINITION_ID = "systemLoad";
    private static final String SYSTEM_LOAD_TITLE = "system load";
    private static final GroupingRule SYSTEM_LOAD_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> SYSTEM_LOAD_METRIC;
    private static final String SYSTEM_LOAD_UNIT = UNIT_COUNT;

    private static final String SWAP_METRIC_NAME = "swap";
    private static final String SWAP_DEFINITION_ID = "swap";
    private static final String SWAP_TITLE = "swap";
    private static final GroupingRule SWAP_GROUPING_RULE = GroupingRule.TAG;
    private static final List<ElementOfBasicGroup> SWAP_METRIC;
    private static final String SWAP_UNIT = UNIT_COUNT;

    static {
        //CPU
        List<Tag> tagList = new ArrayList<>(1);
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
        List<Tag> tagList = new ArrayList<>(0);

        ElementOfBasicGroup  memoryUsedPercent = new ElementOfBasicGroup(MEMORY_PERCENT_METRIC_NAME, "used_percent", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(1);
        elementOfBasicGroupList.add(memoryUsedPercent);

        MEMORY_PERCENT_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //memory usage
        List<Tag> tagList = new ArrayList<>(0);

        ElementOfBasicGroup  memoryTotal = new ElementOfBasicGroup(MEMORY_USAGE_METRIC_NAME, "total", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  memoryUsed = new ElementOfBasicGroup(MEMORY_USAGE_METRIC_NAME, "used", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(2);
        elementOfBasicGroupList.add(memoryTotal);
        elementOfBasicGroupList.add(memoryUsed);

        MEMORY_USAGE_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //disk usage
        List<Tag> tagList = new ArrayList<>(0);
        ElementOfBasicGroup  diskTotal = new ElementOfBasicGroup(DISK_USAGE_METRIC_NAME, "total", tagList, MatchingRule.ALL);
        ElementOfBasicGroup  diskUsed = new ElementOfBasicGroup(DISK_USAGE_METRIC_NAME, "used", tagList, MatchingRule.ALL);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(2);
        elementOfBasicGroupList.add(diskTotal);
        elementOfBasicGroupList.add(diskUsed);

        DISK_USAGE_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //disk usage percent
        List<Tag> tagList = new ArrayList<>(0);
        ElementOfBasicGroup  diskUsedPercent = new ElementOfBasicGroup(DISK_PERCENT_METRIC_NAME, "used_percent", tagList, MatchingRule.ALL);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(1);
        elementOfBasicGroupList.add(diskUsedPercent);

        DISK_PERCENT_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //disk usage
        List<Tag> tagList = new ArrayList<>(0);
        ElementOfBasicGroup  inodeTotal = new ElementOfBasicGroup(DISK_INODE_METRIC_NAME, "inodes_total", tagList, MatchingRule.ALL);
        ElementOfBasicGroup  inodeUsed = new ElementOfBasicGroup(DISK_INODE_METRIC_NAME, "inodes_used", tagList, MatchingRule.ALL);
        ElementOfBasicGroup  inodeFree = new ElementOfBasicGroup(DISK_INODE_METRIC_NAME, "inodes_free", tagList, MatchingRule.ALL);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(3);
        elementOfBasicGroupList.add(inodeTotal);
        elementOfBasicGroupList.add(inodeUsed);
        elementOfBasicGroupList.add(inodeFree);

        DISK_INODE_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //system load
        List<Tag> tagList = new ArrayList<Tag>(0);
        ElementOfBasicGroup  load1 = new ElementOfBasicGroup(SYSTEM_LOAD_METRIC_NAME, "load1", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  load5 = new ElementOfBasicGroup(SYSTEM_LOAD_METRIC_NAME, "load5", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  load15 = new ElementOfBasicGroup(SYSTEM_LOAD_METRIC_NAME, "load15", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(3);
        elementOfBasicGroupList.add(load1);
        elementOfBasicGroupList.add(load5);
        elementOfBasicGroupList.add(load15);

        SYSTEM_LOAD_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    static {
        //swap
        List<Tag> tagList = new ArrayList<Tag>(0);
        ElementOfBasicGroup  total = new ElementOfBasicGroup(SWAP_METRIC_NAME, "total", tagList, MatchingRule.EXACT);
        ElementOfBasicGroup  used = new ElementOfBasicGroup(SWAP_METRIC_NAME, "used", tagList, MatchingRule.EXACT);
        List<ElementOfBasicGroup> elementOfBasicGroupList = new ArrayList<>(2);
        elementOfBasicGroupList.add(total);
        elementOfBasicGroupList.add(used);

        SWAP_METRIC = Collections.unmodifiableList(elementOfBasicGroupList);
    }

    public List<ElementOfBasicGroup> findElementOfBasicGroup(String metricDefinitionId) {
        if (CPU_DEFINITION_ID.equals(metricDefinitionId)) {
            return CPU_METRIC;
        } else if (MEMORY_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_PERCENT_METRIC;
        } else if (MEMORY_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_USAGE_METRIC;
        } else if (DISK_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_USAGE_METRIC;
        } else if (DISK_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_PERCENT_METRIC;
        } else if (DISK_INODE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_INODE_METRIC;
        } else if (SYSTEM_LOAD_DEFINITION_ID.equals(metricDefinitionId)) {
            return SYSTEM_LOAD_METRIC;
        } else if (SWAP_DEFINITION_ID.equals(metricDefinitionId)) {
            return SWAP_METRIC;
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public String findMetricName(String metricDefinitionId) {
        if (CPU_DEFINITION_ID.equals(metricDefinitionId)) {
            return CPU_METRIC_NAME;
        } else if (MEMORY_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_PERCENT_METRIC_NAME;
        } else if (MEMORY_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_USAGE_METRIC_NAME;
        } else if (DISK_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_USAGE_METRIC_NAME;
        } else if (DISK_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_PERCENT_METRIC_NAME;
        } else if (DISK_INODE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_INODE_METRIC_NAME;
        } else if (SYSTEM_LOAD_DEFINITION_ID.equals(metricDefinitionId)) {
            return SYSTEM_LOAD_METRIC_NAME;
        } else if (SWAP_DEFINITION_ID.equals(metricDefinitionId)) {
            return SWAP_METRIC_NAME;
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
        } else if (DISK_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_USAGE_TITLE;
        } else if (DISK_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_PERCENT_TITLE;
        } else if (DISK_INODE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_INODE_TITLE;
        } else if (SYSTEM_LOAD_DEFINITION_ID.equals(metricDefinitionId)) {
            return SYSTEM_LOAD_TITLE;
        } else if (SWAP_DEFINITION_ID.equals(metricDefinitionId)) {
            return SWAP_TITLE;
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public GroupingRule findGroupingRule(String metricDefinitionId) {
        if (CPU_DEFINITION_ID.equals(metricDefinitionId)) {
            return CPU_GROUPING_RULE;
        } else if (MEMORY_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_PERCENT_GROUPING_RULE;
        } else if (MEMORY_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_USAGE_GROUPING_RULE;
        } else if (DISK_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_USAGE_GROUPING_RULE;
        } else if (DISK_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_PERCENT_GROUPING_RULE;
        } else if (DISK_INODE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_INODE_GROUPING_RULE;
        } else if (SYSTEM_LOAD_DEFINITION_ID.equals(metricDefinitionId)) {
            return SYSTEM_LOAD_GROUPING_RULE;
        } else if (SWAP_DEFINITION_ID.equals(metricDefinitionId)) {
            return SWAP_GROUPING_RULE;
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public String findUnit(String metricDefinitionId) {
        if (CPU_DEFINITION_ID.equals(metricDefinitionId)) {
            return CPU_UNIT;
        } else if (MEMORY_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_PERCENT_UNIT;
        } else if (MEMORY_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return MEMORY_USAGE_UNIT;
        } else if (DISK_USAGE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_USAGE_UNIT;
        } else if (DISK_PERCENT_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_PERCENT_UNIT;
        } else if (DISK_INODE_DEFINITION_ID.equals(metricDefinitionId)) {
            return DISK_INODE_UNIT;
        } else if (SYSTEM_LOAD_DEFINITION_ID.equals(metricDefinitionId)) {
            return SYSTEM_LOAD_UNIT;
        } else if (SWAP_DEFINITION_ID.equals(metricDefinitionId)) {
            return SWAP_UNIT;
        }

        throw new UnsupportedOperationException("unsupported metric :" + metricDefinitionId);
    }

    public List<String> findMetricDefinitionIdList(String metricName) {
        List<String> definitionIdList = new LinkedList<>();
        if (CPU_METRIC_NAME.equals(metricName)) {
            definitionIdList.add(CPU_DEFINITION_ID);
        } else if (MEMORY_METRIC_NAME.equals(metricName)) {
            definitionIdList.add(MEMORY_PERCENT_DEFINITION_ID);
            definitionIdList.add(MEMORY_USAGE_DEFINITION_ID);
        } else if (DISK_METRIC_NAME.equals(metricName)) {
            definitionIdList.add(DISK_USAGE_DEFINITION_ID);
            definitionIdList.add(DISK_PERCENT_DEFINITION_ID);
            definitionIdList.add((DISK_INODE_DEFINITION_ID));
        } else if (SYSTEM_LOAD_METRIC_NAME.equals(metricName)) {
            definitionIdList.add(SYSTEM_LOAD_DEFINITION_ID);
        } else if (SWAP_METRIC_NAME.equals(metricName)) {
            definitionIdList.add(SYSTEM_LOAD_DEFINITION_ID);
        }

        return definitionIdList;
    }
}
