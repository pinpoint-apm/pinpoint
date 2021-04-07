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

package com.navercorp.pinpoint.metric.collector.batch;

import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SystemMetricDataTypeBatch {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SystemMetricDataTypeService systemMetricDataTypeService;

    public SystemMetricDataTypeBatch(SystemMetricDataTypeService systemMetricDataTypeService) {
        this.systemMetricDataTypeService = Objects.requireNonNull(systemMetricDataTypeService, "systemMetricDataTypeService");
    }

    //TODO : (minwoo) 이것도 batch 하지말고 ehcache를 사용하는게 좋을듯함.
    public void update() {
        logger.info("[SystemMetricDataTypeBatch] batch start");
        Map<MetricDataName, MetricDataType> metricDataTypeMapFromDB = systemMetricDataTypeService.getMetricDataTypeFromDataBase();
        Map<MetricDataName, MetricDataType> metricDataTypeMap = systemMetricDataTypeService.copyMetricDataTypeMap();

        Map<MetricDataName, MetricDataType> metricDataTypeMapForInsert = new HashMap<MetricDataName, MetricDataType>();
        Map<MetricDataName, MetricDataType> newMetricDataTypeMap = new HashMap<MetricDataName, MetricDataType>();

        for (Map.Entry<MetricDataName, MetricDataType> metricDataType : metricDataTypeMap.entrySet()) {
            newMetricDataTypeMap.put(metricDataType.getKey(), metricDataType.getValue());

            if (metricDataTypeMapFromDB.containsKey(metricDataType.getKey())) {
                metricDataTypeMapFromDB.remove(metricDataType.getKey());
            } else {
                metricDataTypeMapForInsert.put(metricDataType.getKey(), metricDataType.getValue());
            }
        }

        newMetricDataTypeMap.putAll(metricDataTypeMapFromDB);
        systemMetricDataTypeService.replaceMetricDataTypeMap(newMetricDataTypeMap);

        systemMetricDataTypeService.saveMetricDataTypeToDB(metricDataTypeMapForInsert);

        logger.info("[SystemMetricDataTypeBatch] end batch. replace count : {}, insert count : {}", newMetricDataTypeMap.size(), metricDataTypeMapForInsert.size());
    }
}
