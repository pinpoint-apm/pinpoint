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


import com.navercorp.pinpoint.common.timeseries.array.DoubleArray;
import com.navercorp.pinpoint.common.timeseries.array.LongArray;
import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.LongDataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.util.PointCreator;
import com.navercorp.pinpoint.metric.common.util.TagUtils;
import com.navercorp.pinpoint.metric.common.util.TimeSeriesBuilder;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.mapping.Field;
import com.navercorp.pinpoint.metric.web.mapping.Metric;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValueGroup;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;
import com.navercorp.pinpoint.metric.web.util.TagListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricDataServiceImpl implements SystemMetricDataService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SystemMetricDao systemMetricDoubleDao;

    private final SystemMetricDataTypeService systemMetricDataTypeService;
    private final YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager;

    private final SystemMetricHostInfoService systemMetricHostInfoService;

    public SystemMetricDataServiceImpl(SystemMetricDao systemMetricDoubleDao,
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

        List<MetricValue<? extends Number>> metricValueList = getMetricValues(metricDataSearchKey, timeWindow, tags);

        GroupingRule groupingRule = systemMetricBasicGroupManager.findGroupingRule(metricDefinitionId);
        List<MetricValueGroup<? extends Number>> metricValueGroupList = groupingMetricValue(metricValueList, groupingRule);

        List<Long> timeStampList = timeWindow.getTimeseriesWindows();
        String title = systemMetricBasicGroupManager.findMetricTitle(metricDefinitionId);
        String unit = systemMetricBasicGroupManager.findUnit(metricDefinitionId);
        return new SystemMetricData(title, unit, timeStampList, metricValueGroupList);
    }

    private List<MetricValue<? extends Number>> getMetricValues(MetricDataSearchKey metricDataSearchKey, TimeWindow timeWindow, List<Tag> tags) {
        Metric elementOfBasicGroupList = systemMetricBasicGroupManager.findElementOfBasicGroup(metricDataSearchKey.getMetricDefinitionId());

        List<QueryResult<Number>> queryResults = selectAll(metricDataSearchKey, elementOfBasicGroupList, tags);

        List<MetricValue<?>> metricValueList = new ArrayList<>(elementOfBasicGroupList.getFields().size());
        try {
            for (QueryResult<Number> result : queryResults) {
                CompletableFuture<List<DataPoint<Number>>> future = result.future();
                MetricDataType type = result.type();
                List<DataPoint<Number>> dataPoints = future.get();
                if (type == MetricDataType.LONG) {
                    List<DataPoint<Long>> longList = (List<DataPoint<Long>>) (List<?>) dataPoints;
                    MetricValue<Long> doubleMetricValue = createSystemLongMetricValue(timeWindow, result.tag(), longList);
                    metricValueList.add(doubleMetricValue);
                } else if (type == MetricDataType.DOUBLE) {
                    List<DataPoint<Double>> doubleList = (List<DataPoint<Double>>) (List<?>) dataPoints;
                    MetricValue<Double> doubleMetricValue = createSystemDoubleMetricValue(timeWindow, result.tag(), doubleList);
                    metricValueList.add(doubleMetricValue);
                }
            }

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
                if (MetricDataType.DOUBLE == metricDataType) {
                    CompletableFuture<List<DataPoint<Double>>> doubleFuture = systemMetricDoubleDao.getAsyncSampledSystemMetricData(metricDataSearchKey, metricTag);
                    invokeList.add(new QueryResult<>(metricDataType, doubleFuture, metricTag));
                } else {
                    throw new RuntimeException("No Such Metric");
                }
            }
        }
        return (List<QueryResult<Number>>)(List<?>) invokeList;
    }

    private record QueryResult<T extends Number>(MetricDataType type, CompletableFuture<List<DataPoint<T>>> future, MetricTag tag) {
    }


    private List<MetricValueGroup<? extends Number>> groupingMetricValue(List<MetricValue<?>> metricValueList, GroupingRule groupingRule) {
        if (GroupingRule.TAG == groupingRule) {
            return groupingByTag(metricValueList);
        }
        throw new UnsupportedOperationException("unsupported groupingRule :" + groupingRule);
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
            String groupName = entry.getKey().groupName();
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

        List<Tag> tagList1 = tagGroup1.tagList();
        List<Tag> tagList2 = tagGroup2.tagList();

        return TagListUtils.containsAll(tagList1, tagList2);
    }

    private record TagGroup(List<Tag> tagList) {

        public String groupName() {
            if (tagList.isEmpty()) {
                return "groupWithNoTag";
            }

            return TagUtils.toTagString(tagList);
        }

    }

    private MetricValue<Long> createSystemLongMetricValue(TimeWindow timeWindow, MetricTag metricTag,
                                                                      List<DataPoint<Long>> sampledSystemMetricDataList) {
        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<DataPoint<Long>> filledSystemMetricDataList = builder.buildLongMetric(PointCreator::longPoint, sampledSystemMetricDataList);

        List<Long> list = LongArray.asList(filledSystemMetricDataList, (data) -> ((LongDataPoint) data).getLongValue());
        return new MetricValue<>(metricTag.getFieldName(), metricTag.getTags(), list);
    }

    private MetricValue<Double> createSystemDoubleMetricValue(
                                                      TimeWindow timeWindow, MetricTag metricTag,
                                                      List<DataPoint<Double>> sampledSystemMetricDataList) {
        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<DataPoint<Double>> filledSystemMetricDataList = builder.buildDoubleMetric(PointCreator::doublePoint, sampledSystemMetricDataList);

        List<Double> list = DoubleArray.asList(filledSystemMetricDataList, Points::asDouble);
        return new MetricValue<>(metricTag.getFieldName(), metricTag.getTags(), list);
    }

}
