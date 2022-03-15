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

import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDataTypeDao;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricDataTypeServiceImpl implements SystemMetricDataTypeService {


    private final SystemMetricDataTypeDao pinotSystemMetricLongDao;

    public SystemMetricDataTypeServiceImpl(SystemMetricDataTypeDao pinotSystemMetricLongDao) {
        this.pinotSystemMetricLongDao = Objects.requireNonNull(pinotSystemMetricLongDao, "pinotSystemMetricLongDao");
    }

    @Override
    public MetricDataType getMetricDataType(MetricDataName metricDataName) {
        MetricData metricData = pinotSystemMetricLongDao.selectMetricDataType(metricDataName);

        if (Objects.isNull(metricData)) {
            return MetricDataType.UNKNOWN;
        }

        return metricData.getMetricDataType();
    }
}
