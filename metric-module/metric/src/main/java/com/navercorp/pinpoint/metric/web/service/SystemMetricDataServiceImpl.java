/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.service;


import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.mapping.Field;
import com.navercorp.pinpoint.metric.web.mapping.Metric;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValueGroup;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.util.TagUtils;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.metric.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.metric.web.util.metric.LongUncollectedDataCreator;
import com.navercorp.pinpoint.metric.web.util.metric.TimeSeriesBuilder;
import com.navercorp.pinpoint.metric.web.util.metric.UncollectedDataCreator;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricDataServiceImpl implements SystemMetricDataService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SystemMetricDao<Double> systemMetricDoubleDao;

    private final SystemMetricDataTypeService systemMetricDataTypeService;
    private final YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager;

    private final SystemMetricHostInfoService systemMetricHostInfoService;

    public SystemMetricDataServiceImpl(SystemMetricDao<Double> systemMetricDoubleDao,
                                       SystemMetricDataTypeService systemMetricDataTypeService,
                                       YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager,
                                       SystemMetricHostInfoService systemMetricHostInfoService) {
        this.systemMetricDoubleDao = Objects.requireNonNull(systemMetricDoubleDao, "systemMetricDoubleDao");
        this.systemMetricDataTypeService = Objects.requireNonNull(systemMetricDataTypeService, "systemMetricDataTypeService");
        this.systemMetricBasicGroupManager = Objects.requireNonNull(systemMetricBasicGroupManager, "systemMetricMetadataManager");
        this.systemMetricHostInfoService = Objects.requireNonNull(systemMetricHostInfoService, "systemMetricHostInfoService");
    }

    @Override
    public SystemMetricData<? extends Number> getCollectedMetricData(MetricDataSearchKey metricDataSearchKey, TimeWindow timeWindow, List<Tag> tags) {
        String metricDefinitionId = metricDataSearchKey.getMetricDefinitionId();

        List<MetricValue<?>> metricValueList = getMetricValues(metricDataSearchKey, timeWindow, tags);

        GroupingRule groupingRule = systemMetricBasicGroupManager.findGroupingRule(metricDefinitionId);
        List<MetricValueGroup<?>> metricValueGroupList = groupingMetricValue(metricValueList, groupingRule);

        List<Long> timeStampList = createTimeStampList(timeWindow);
        String title = systemMetricBasicGroupManager.findMetricTitle(metricDefinitionId);
        String unit = systemMetricBasicGroupManager.findUnit(metricDefinitionId);
        return new SystemMetricData(title, unit, timeStampList, metricValueGroupList);
    }

    private List<MetricValue<? extends Number>> getMetricValues(MetricDataSearchKey metricDataSearchKey, TimeWindow timeWindow, List<Tag> tags) {
        Metric elementOfBasicGroupList = systemMetricBasicGroupManager.findElementOfBasicGroup(metricDataSearchKey.getMetricDefinitionId());

        StopWatch watch = StopWatch.createStarted();
        List<QueryResult<Number>> queryResults = selectAll(metricDataSearchKey, elementOfBasicGroupList, tags);

        List<MetricValue<?>> metricValueList = new ArrayList<>(elementOfBasicGroupList.getFields().size());
        try {
            for (QueryResult<Number> result : queryResults) {
                Future<List<SystemMetricPoint<Number>>> future = result.getFuture();
                MetricDataType type = result.getType();
                List<SystemMetricPoint<Number>> systemMetricPoints = future.get();
                if (type == MetricDataType.LONG) {
                    List<SystemMetricPoint<Long>> longList = (List<SystemMetricPoint<Long>>) (List<?>) systemMetricPoints;
                    MetricValue<Long> doubleMetricValue = createSystemMetricValue(timeWindow, result.getTag(), longList, LongUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                    metricValueList.add(doubleMetricValue);
                } else if (type == MetricDataType.DOUBLE) {
                    List<SystemMetricPoint<Double>> doubleList = (List<SystemMetricPoint<Double>>) (List<?>) systemMetricPoints;
                    StopWatch dataProcessWatch = StopWatch.createStarted();
                    MetricValue<Double> doubleMetricValue = createSystemMetricValue(timeWindow, result.getTag(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                    dataProcessWatch.stop();
                    metricValueList.add(doubleMetricValue);
                }
            }

            watch.stop();
            return metricValueList;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private List<QueryResult<Number>> selectAll(MetricDataSearchKey metricDataSearchKey, Metric elementOfBasicGroupList, List<Tag> tags) {
        List<QueryResult<? extends Number>> invokeList = new ArrayList<>();
        for (Field field : elementOfBasicGroupList.getFields()) {
            MetricDataName metricDataName = new MetricDataName(metricDataSearchKey.getMetricName(), field.getName());
            MetricDataType metricDataType = systemMetricDataTypeService.getMetricDataType(metricDataName);
            List<MetricTag> metricTagList = systemMetricHostInfoService.getTag(metricDataSearchKey, field, tags);

            for (MetricTag metricTag : metricTagList) {
                switch (metricDataType) {
                    case DOUBLE:
                        Future<List<SystemMetricPoint<Double>>> doubleFuture = systemMetricDoubleDao.getAsyncSampledSystemMetricData(metricDataSearchKey, metricTag);
                        invokeList.add(new QueryResult<>(metricDataType, doubleFuture, metricTag));
                        break;
                    default:
                        throw new RuntimeException("No Such Metric");
                }
            }
        }
        return (List<QueryResult<Number>>)(List<?>) invokeList;
    }

    private static class QueryResult<T extends Number> {
        private final MetricDataType type;
        private final Future<List<SystemMetricPoint<T>>> future;
        private final MetricTag tag;

        public QueryResult(MetricDataType type, Future<List<SystemMetricPoint<T>>> future, MetricTag tag) {
            this.type = Objects.requireNonNull(type, "type");
            this.future = Objects.requireNonNull(future, "future");
            this.tag = Objects.requireNonNull(tag, "tag");
        }

        public MetricDataType getType() {
            return type;
        }

        public Future<List<SystemMetricPoint<T>>> getFuture() {
            return future;
        }

        public MetricTag getTag() {
            return tag;
        }
    }


    private List<MetricValueGroup<? extends Number>> groupingMetricValue(List<MetricValue<?>> metricValueList, GroupingRule groupingRule) {
        switch (groupingRule) {
            case TAG:
                return groupingByTag(metricValueList);
            default:
                throw new UnsupportedOperationException("unsupported groupingRule :" + groupingRule);
        }
    }

    private List<MetricValueGroup<? extends Number>> groupingByTag(List<MetricValue<? extends Number>> metricValueList) {
        List<TagGroup> uniqueTagGroupList = createUniqueTagGroupList(metricValueList);

        Map<TagGroup, List<MetricValue<?>>> metricValueGroupMap = new HashMap<>();
        for (MetricValue<?> metricValue : metricValueList) {
            TagGroup tagGroup = findTagGroup(uniqueTagGroupList, new TagGroup(metricValue.getTagList()));

            List<MetricValue<?>> metricValues = metricValueGroupMap.computeIfAbsent(tagGroup, v -> new ArrayList<>(1));
            metricValues.add(metricValue);
        }

        Collection<List<MetricValue<?>>> valueList = metricValueGroupMap.values();

        List<MetricValueGroup<?>> metricValueGroupList = new ArrayList<>(valueList.size());
        for (Map.Entry<TagGroup, List<MetricValue<?>>> entry : metricValueGroupMap.entrySet()) {
            String groupName = entry.getKey().getGroupName();
            List<MetricValue<?>> value = entry.getValue();
            MetricValueGroup<?> group = new MetricValueGroup(value, groupName);
            metricValueGroupList.add(group);
        }

        return metricValueGroupList;
    }

    private TagGroup findTagGroup(List<TagGroup> uniqueTagGroupList, TagGroup tagGroup) {
        for (TagGroup tg : uniqueTagGroupList) {
            if (equalsTagGroup(tg, tagGroup)) {
                return tg;
            }
        }
        throw new RuntimeException("cann't find tagGroup");
    }

    private List<TagGroup> createUniqueTagGroupList(List<MetricValue<? extends Number>> metricValueList) {
        List<TagGroup> uniqueTagGroupList = new ArrayList<>();

        for (MetricValue<?> metricValue : metricValueList) {
            boolean containTagGroup = false;

            List<Tag> tagList = metricValue.getTagList();
            TagGroup newTagGroup = new TagGroup(tagList);

            for (TagGroup tagGroup : uniqueTagGroupList) {
                if (equalsTagGroup(tagGroup, newTagGroup)) {
                    containTagGroup = true;
                }
            }

            if (containTagGroup == false) {
                uniqueTagGroupList.add(newTagGroup);
            }
        }

        return uniqueTagGroupList;
    }

    private boolean equalsTagGroup(TagGroup tagGroup1, TagGroup tagGroup2) {
        if (tagGroup1 == tagGroup2) {
            return true;
        }

        List<Tag> tagList1 = tagGroup1.getTagList();
        List<Tag> tagList2 = tagGroup2.getTagList();

        if (tagList1.size() == tagList2.size()) {
            if (tagList1.containsAll(tagList2)) {
                return true;
            }
        }

        return false;
    }

    private static class TagGroup {
        private final List<Tag> tagList;

        public TagGroup(List<Tag> tagList) {
            this.tagList = Objects.requireNonNull(tagList, "tagList");
        }

        public List<Tag> getTagList() {
            return tagList;
        }

        public String getGroupName() {
            if (tagList.isEmpty()) {
                return "groupWithNoTag";
            }

            return TagUtils.toTagString(tagList);
        }

        @Override
        public String toString() {
            return "TagGroup{" +
                    "tagList=" + tagList +
                    '}';
        }
    }


    private List<Long> createTimeStampList(TimeWindow timeWindow) {
        List<Long> timestampList = new ArrayList<>((int) timeWindow.getWindowRangeCount());

        for (Long timestamp : timeWindow) {
            timestampList.add(timestamp);
        }

        return timestampList;
    }

    private <T extends Number> MetricValue<T> createSystemMetricValue(TimeWindow timeWindow, MetricTag metricTag,
                                                                      List<SystemMetricPoint<T>> sampledSystemMetricDataList,
                                                                      UncollectedDataCreator<T> uncollectedDataCreator) {
        TimeSeriesBuilder<T> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<SystemMetricPoint<T>> filledSystemMetricDataList = builder.build(sampledSystemMetricDataList);

        List<T> valueList = filledSystemMetricDataList.stream()
                .map(SystemMetricPoint::getYVal)
                .collect(Collectors.toList());

        return new MetricValue<>(metricTag.getFieldName(), metricTag.getTags(), valueList);
    }

    private String getChartName(String metricName, String fieldName) {
        return metricName + "_" + fieldName;
    }
}
