/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Hyunjoon Cho
 */
@Component
@Deprecated
public class SimpleSystemMetricMetadata implements SystemMetricMetadata {
    private final String METADATA_PATH = "/Users/user/workspace/pinpoint_olap/pinpoint/metric/SystemMetricMetadata.txt";
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<String, MetricDataType> fieldTypeMap;

    private SimpleSystemMetricMetadata() {
        fieldTypeMap = loadOrCreate();
    }

    private ConcurrentMap<String, MetricDataType> loadOrCreate() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(new File(METADATA_PATH)));
            Map<String, MetricDataType> map = (Map<String, MetricDataType>) ois.readObject();
            logger.info("Loaded Metadata");
            return new ConcurrentHashMap<>(map);
        } catch (Exception e) {
            logger.info("Failed Loading Metadata");
            return new ConcurrentHashMap<>();
        }
    }

    @Override
    public void put(String metricName, String fieldName, MetricDataType type) {
        fieldTypeMap.put(metricName.concat(fieldName), type);
    }

    @Override
    public MetricDataType get(String metricName, String fieldName) {
        MetricDataType metricDataType = fieldTypeMap.get(metricName.concat(fieldName));
        if (metricDataType == null) {
            metricDataType = MetricDataType.UNKNOWN;
        }
        return metricDataType;
    }

    @Override
    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(new File(METADATA_PATH)));
            oos.writeObject(fieldTypeMap);
            oos.close();
        } catch (IOException e) {
            logger.warn("Failed to Save Metadata", e);
        }
    }
}
