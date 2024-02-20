/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.service;

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.otlp.collector.dao.OtlpMetricDao;
import com.navercorp.pinpoint.otlp.collector.model.*;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PinotOtlpMetricCollectorService implements OtlpMetricCollectorService {
    private static final String KEY_SERVICE_NAME = "service.name";
    private static final String KEY_SERVICE_NAMESPACE = "service.namespace";
    private static final String KEY_PINPOINT_AGENTID = "pinpoint.agentid";
    private final Logger logger = LogManager.getLogger(this.getClass());
    @NotNull private final OtlpMetricDao otlpMetricDao;

    public PinotOtlpMetricCollectorService(@Valid OtlpMetricDao otlpMetricDao) {
        this.otlpMetricDao = Objects.requireNonNull(otlpMetricDao, "otlpMetricDao");
    }
    @Override
    public void save(OtlpMetricData otlpMetricData) {
        if (logger.isDebugEnabled()) {
            logger.debug("save {}", otlpMetricData);
        }

        String tenantId = otlpMetricData.getTenantId();                 // TODO: currently not used. data for different tenants will be saved to the different tables
        ServiceId serviceId = otlpMetricData.getServiceNamespace();
        ApplicationId applicationId = otlpMetricData.getServiceName();

        String agentId = otlpMetricData.getAgentId();
        String metricGroupName = otlpMetricData.getMetricGroupName();
        String metricName = otlpMetricData.getMetricName();
        String unit = otlpMetricData.getUnit();
        int metricType = otlpMetricData.getMetricType();
        int aggreTemporality = otlpMetricData.getAggreTemporality();
        String version = "";

        Long saveTime = System.currentTimeMillis();

        for(OtlpMetricDataPoint dataPoint : otlpMetricData.getValues()) {
            DataType dataType = dataPoint.getDataType();

            Map<String, String> tags = dataPoint.getTags();

            List<String> tagList = tags.entrySet().stream().filter((e) -> {
                String key = e.getKey().toLowerCase();
                return (!key.equals(KEY_SERVICE_NAME) && !key.equals(KEY_SERVICE_NAMESPACE) && !key.equals(KEY_PINPOINT_AGENTID));
            }).map((e) -> {
                return e.getKey() + ":" + e.getValue();
            }).collect(Collectors.toList());

            String rawTags = String.join(",", tagList);
            PinotOtlpMetricMetadata metadata = new PinotOtlpMetricMetadata(serviceId.toString(), applicationId.toString(), agentId,
                    metricGroupName, metricName, dataPoint.getFieldName(), unit, dataPoint.getDescription(), metricType,
                    dataType.getNumber(), dataPoint.getAggreFunc(), aggreTemporality, rawTags, dataPoint.getStartTime(), saveTime, version);
            otlpMetricDao.updateMetadata(metadata);

            if (dataType == DataType.LONG) {
                long longValue = dataPoint.getValue().longValue();
                PinotOtlpMetricLongData row = new PinotOtlpMetricLongData(serviceId, applicationId, agentId, metricGroupName, metricName, dataPoint.getFieldName(),
                        dataPoint.getFlag(), tagList, version, longValue, dataPoint.getEventTime(), dataPoint.getStartTime());
                otlpMetricDao.insertLong(row);

            } else {
                double doubleValue = dataPoint.getValue().doubleValue();
                PinotOtlpMetricDoubleData row = new PinotOtlpMetricDoubleData(serviceId, applicationId, agentId, metricGroupName, metricName, dataPoint.getFieldName(),
                        dataPoint.getFlag(), tagList, version, doubleValue, dataPoint.getEventTime(), dataPoint.getStartTime());
                otlpMetricDao.insertDouble(row);
            }
        }
    }
}
