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

package com.navercorp.pinpoint.profiler.context.monitor.metric;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.IdValidateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class CustomMetricIdGenerator {

    static final int NOT_REGISTERED = -1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lockObject = new Object();

    private final int limitIdNumber;

    private Map<String, Integer> metricNameToIdMap = new HashMap<String, Integer>();

    private int currentId = 0;

    CustomMetricIdGenerator(int limitIdNumber) {
        Assert.isTrue(limitIdNumber > 0, "'limitIdNumber' must be >= 0");
        this.limitIdNumber = limitIdNumber;
    }

    int create(String metricName) {
        if (!checkValidCustomMetricName(metricName)) {
            logger.warn("Failed to create MetricId. metricName:{}", metricName);
            return NOT_REGISTERED;
        }

        synchronized (lockObject) {
            if (currentId >= limitIdNumber) {
                return NOT_REGISTERED;
            }

            boolean contains = metricNameToIdMap.containsKey(metricName);
            if (contains) {
                return NOT_REGISTERED;
            }

            ++currentId;
            metricNameToIdMap.put(metricName, currentId);
            return currentId;
        }
    }

    private boolean checkValidCustomMetricName(String customMetricName) {
        try {
            String[] split = customMetricName.split("/");

            if (split == null || split.length != 3) {
                throw new IllegalArgumentException("customMetricName must consist of {GroupName}/{MetricName}/LabelName} ");
            }

            for (String eachName : split) {
                if (!IdValidateUtils.validateId(eachName, 64)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.warn("Inserted customMetricName({}) is not valid. cause:{}", customMetricName, e.getMessage(), e);
        }
        return false;
    }

}
