/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.otlp.collector.dao.OtlpMetricDao;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricDataPoint;
import com.navercorp.pinpoint.otlp.collector.model.OtlpResourceAttributes;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricDoubleData;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricLongData;
import com.navercorp.pinpoint.otlp.collector.model.PinotOtlpMetricMetadata;
import com.navercorp.pinpoint.otlp.collector.model.SortKeyUtils;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PinotOtlpMetricCollectorService implements OtlpMetricCollectorService {

    private static final String DEFAULT_SERVICE_NAME = "";

    private final Logger logger = LogManager.getLogger(this.getClass());

    @NonNull
    private final OtlpMetricDao otlpMetricDao;

    public PinotOtlpMetricCollectorService(OtlpMetricDao otlpMetricDao) {
        this.otlpMetricDao = Objects.requireNonNull(otlpMetricDao, "otlpMetricDao");
    }
    @Override
    public void save(OtlpMetricData otlpMetricData) {
        if (logger.isDebugEnabled()) {
            logger.debug("save {}", otlpMetricData);
        }

        String tenantId = otlpMetricData.getTenantId();                 // TODO: currently not used. data for different tenants will be saved to the different tables`
        String applicationName = otlpMetricData.getServiceName();

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
                return (!key.equals(OtlpResourceAttributes.KEY_SERVICE_NAME) && !key.equals(OtlpResourceAttributes.KEY_SERVICE_NAMESPACE) && !key.equals(OtlpResourceAttributes.KEY_PINPOINT_AGENTID));
            }).map((e) -> {
                return e.getKey() + ":" + e.getValue();
            }).collect(Collectors.toList());

            String rawTags = String.join(",", tagList);
            PinotOtlpMetricMetadata metadata = new PinotOtlpMetricMetadata(DEFAULT_SERVICE_NAME, applicationName, agentId,
                    metricGroupName, metricName, dataPoint.getFieldName(), unit, dataPoint.getDescription(), metricType,
                    dataType.getNumber(), dataPoint.getAggreFunc(), aggreTemporality, rawTags, dataPoint.getStartTime(), saveTime, version);
            otlpMetricDao.updateMetadata(metadata);

            String sortKey = SortKeyUtils.generateKey(applicationName, metricGroupName, metricName);
            if (dataType == DataType.LONG) {
                long longValue = dataPoint.getValue().longValue();
                PinotOtlpMetricLongData row = new PinotOtlpMetricLongData(DEFAULT_SERVICE_NAME, sortKey, applicationName, agentId, metricGroupName, metricName, dataPoint.getFieldName(),
                        dataPoint.getFlag(), tagList, version, longValue, dataPoint.getEventTime(), dataPoint.getStartTime());
                otlpMetricDao.insertLong(row);

            } else {
                double doubleValue = dataPoint.getValue().doubleValue();
                PinotOtlpMetricDoubleData row = new PinotOtlpMetricDoubleData(DEFAULT_SERVICE_NAME, sortKey, applicationName, agentId, metricGroupName, metricName, dataPoint.getFieldName(),
                        dataPoint.getFlag(), tagList, version, doubleValue, dataPoint.getEventTime(), dataPoint.getStartTime());
                otlpMetricDao.insertDouble(row);
            }
        }
    }
}
