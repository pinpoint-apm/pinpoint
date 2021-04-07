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

package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(transactionManager="metricTransactionManager")
public class SystemMetricDataTypeServiceImpl implements SystemMetricDataTypeService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SystemMetricDataTypeDao systemMetricDataTypeDao;
    private volatile Map<MetricDataName, MetricDataType> metricDataTypeMap;


    public SystemMetricDataTypeServiceImpl(SystemMetricDataTypeDao systemMetricDataTypeDao) {
        this.systemMetricDataTypeDao = Objects.requireNonNull(systemMetricDataTypeDao, "systemMetricDataTypeDao");
        //TODO : (minwoo) ConcurrentHashMap이 가장 적합한 객체인가
        //TODO : (minwoo) metricDataTypeMap을 별도 객체로 빼는것도 답인듯, metricDataTypeCollector 라는 이름으로. service에서 객체를 직접 들고있으니까 이상하네.
        metricDataTypeMap = new ConcurrentHashMap<>();
    }

    @Override
    public void saveMetricDataType(SystemMetric systemMetric) {
        if (systemMetric instanceof LongCounter) {
            metricDataTypeMap.putIfAbsent(new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName()), MetricDataType.LONG);
        } else if (systemMetric instanceof DoubleCounter) {
            metricDataTypeMap.putIfAbsent(new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName()), MetricDataType.DOUBLE);
        } else {
            logger.error("can not find metric data type.  systemMetric : {}", systemMetric);
        }
    }

    @Override
    public void replaceMetricDataTypeMap(Map<MetricDataName, MetricDataType> metricDataTypeMap) {
        this.metricDataTypeMap = new ConcurrentHashMap<>(metricDataTypeMap);
    }

    @Override
    public Map<MetricDataName, MetricDataType> copyMetricDataTypeMap() {
        return new HashMap<MetricDataName, MetricDataType>(metricDataTypeMap);
    }

    @Override
    public Map<MetricDataName, MetricDataType> getMetricDataTypeFromDataBase() {
        List<MetricData> metricDataList = systemMetricDataTypeDao.selectMetricDataType();

        Map<MetricDataName, MetricDataType> metricDataTypeMap = new HashMap<MetricDataName, MetricDataType>();
        for (MetricData metricData : metricDataList) {
            metricDataTypeMap.put(new MetricDataName(metricData.getMetricName(), metricData.getFieldName()), metricData.getMetricDataType());
        }

        return metricDataTypeMap;
    }

    @Override
    public void saveMetricDataTypeToDB(Map<MetricDataName, MetricDataType> metricDataTypeMap) {
        List<MetricData> metricDataList = new ArrayList<>();
        for (Map.Entry<MetricDataName, MetricDataType> metricDataType : metricDataTypeMap.entrySet()) {
            MetricDataName metricDataName = metricDataType.getKey();
            metricDataList.add(new MetricData(metricDataName.getMetricName(), metricDataName.getFieldName(), metricDataType.getValue()));
        }

        systemMetricDataTypeDao.updateMetricDataType(metricDataList);
    }

}
