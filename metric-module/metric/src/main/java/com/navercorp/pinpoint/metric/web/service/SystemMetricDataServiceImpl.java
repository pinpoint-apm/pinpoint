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
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.ElementOfBasicGroup;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;
import com.navercorp.pinpoint.metric.web.util.metric.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.metric.web.util.metric.LongUncollectedDataCreator;
import com.navercorp.pinpoint.metric.web.util.metric.TimeSeriesBuilder;
import com.navercorp.pinpoint.metric.web.util.metric.UncollectedDataCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricDataServiceImpl implements SystemMetricDataService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SystemMetricDao<Long> systemMetricLongDao;
    private final SystemMetricDao<Double> systemMetricDoubleDao;

    private final SystemMetricDataTypeService systemMetricDataTypeService;
    private final SystemMetricBasicGroupManager systemMetricBasicGroupManager;
    private final SystemMetricHostInfoService systemMetricHostInfoService;

    public SystemMetricDataServiceImpl(SystemMetricDao<Long> systemMetricLongDao,
                                       SystemMetricDao<Double> systemMetricDoubleDao,
                                       SystemMetricDataTypeService systemMetricDataTypeService,
                                       SystemMetricBasicGroupManager systemMetricBasicGroupManager,
                                       SystemMetricHostInfoService systemMetricHostInfoService) {
        this.systemMetricLongDao = Objects.requireNonNull(systemMetricLongDao, "systemMetricLongDao");
        this.systemMetricDoubleDao = Objects.requireNonNull(systemMetricDoubleDao, "systemMetricDoubleDao");
        this.systemMetricDataTypeService = Objects.requireNonNull(systemMetricDataTypeService, "systemMetricDataTypeService");
        this.systemMetricBasicGroupManager = Objects.requireNonNull(systemMetricBasicGroupManager, "systemMetricMetadataManager");
        this.systemMetricHostInfoService = Objects.requireNonNull(systemMetricHostInfoService, "systemMetricHostInfoService");
    }

    @Override
    public List<SystemMetric> getSystemMetricBoList(QueryParameter queryParameter) {

        MetricDataName metricDataName = new MetricDataName(queryParameter.getMetricName(), queryParameter.getFieldName());
        MetricDataType metricDataType = systemMetricDataTypeService.getMetricDataType(metricDataName);

        switch (metricDataType) {
            case LONG:
                return systemMetricLongDao.getSystemMetric(queryParameter);
            case DOUBLE:
                return systemMetricDoubleDao.getSystemMetric(queryParameter);
            default:
                throw new RuntimeException("No Such Metric");
        }
    }

    @Override
    public SystemMetricChart getSystemMetricChart(TimeWindow timeWindow, QueryParameter queryParameter) {
        String metricName = queryParameter.getMetricName();
        String fieldName = queryParameter.getFieldName();

        MetricDataType metricDataType = systemMetricDataTypeService.getMetricDataType(new MetricDataName(metricName, fieldName));
        String chartName = getChartName(metricName, fieldName);

        switch (metricDataType) {
            case LONG:
                List<SampledSystemMetric<Long>> sampledLongSystemMetrics = systemMetricLongDao.getSampledSystemMetric(queryParameter);
                return new SystemMetricChart<>(timeWindow, chartName, sampledLongSystemMetrics);
            case DOUBLE:
                List<SampledSystemMetric<Double>> sampledDoubleSystemMetrics = systemMetricDoubleDao.getSampledSystemMetric(queryParameter);
                return new SystemMetricChart<>(timeWindow, chartName, sampledDoubleSystemMetrics);
            default:
                throw new RuntimeException("No Such Metric");
        }
    }

    @Override
    public SystemMetricData getCollectedMetricData(MetricDataSearchKey metricDataSearchKey, TimeWindow timeWindow) {
        String metricDefinitionId = metricDataSearchKey.getMetricDefinitionId();
        List<ElementOfBasicGroup> elementOfBasicGroupList = systemMetricBasicGroupManager.findElementOfBasicGroup(metricDataSearchKey.getMetricDefinitionId());
        String title = systemMetricBasicGroupManager.findMetricTitle(metricDefinitionId);
        List<MetricValue> metricValueList = new ArrayList<>(elementOfBasicGroupList.size());

        for (ElementOfBasicGroup elementOfBasicGroup : elementOfBasicGroupList) {
            MetricDataType metricDataType = systemMetricDataTypeService.getMetricDataType(new MetricDataName(metricDataSearchKey.getMetricName(), elementOfBasicGroup.getFieldName()));
            List<MetricTag> metricTagList = systemMetricHostInfoService.getTag(metricDataSearchKey, elementOfBasicGroup);


            for (MetricTag metricTag : metricTagList) {
                switch (metricDataType) {
                    case LONG:
                        List<SystemMetricPoint<Long>> longSampledSystemMetricData = systemMetricLongDao.getSampledSystemMetricData(metricDataSearchKey, metricTag);
                        MetricValue longMetricValue = createSystemMetricValue(timeWindow, metricTag, longSampledSystemMetricData, LongUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                        metricValueList.add(longMetricValue);
                        //TODO : (minwoo) 위의 2줄도 중복 제거필요
                        break;
                    case DOUBLE:
                        List<SystemMetricPoint<Double>> doubleSampledSystemMetricData = systemMetricDoubleDao.getSampledSystemMetricData(metricDataSearchKey, metricTag);
                        MetricValue doubleMetricValue = createSystemMetricValue(timeWindow, metricTag, doubleSampledSystemMetricData, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                        metricValueList.add(doubleMetricValue);
                        break;
                    default:
                        throw new RuntimeException("No Such Metric");
                }
            }
        }

        List<Long> timeStampList = createTimeStampList(timeWindow);
        return new SystemMetricData(title, timeStampList ,metricValueList);
    }

    private List<Long> createTimeStampList(TimeWindow timeWindow) {
        List<Long> timestampList = new ArrayList<>((int) timeWindow.getWindowRangeCount());

        for (Long timestamp : timeWindow) {
            timestampList.add(timestamp);
        }

        return timestampList;
    }

    private <T extends Number> MetricValue createSystemMetricValue(TimeWindow timeWindow, MetricTag metricTag, List<SystemMetricPoint<T>> sampledSystemMetricDataList, UncollectedDataCreator uncollectedDataCreator) {
        TimeSeriesBuilder<T> builder = new TimeSeriesBuilder(timeWindow, uncollectedDataCreator);
        List<SystemMetricPoint<T>> filledSystemMetricDataList = builder.build(sampledSystemMetricDataList);

        List<T> valueList = filledSystemMetricDataList.stream()
                .map(SystemMetricPoint::getYVal)
                .collect(Collectors.toList());

        return new MetricValue(metricTag.getFieldName(), metricTag.getTags(), valueList);
    }

    private String getChartName(String metricName, String fieldName) {
        return metricName + "_" + fieldName;
    }
}
