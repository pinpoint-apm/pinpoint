/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.common.model;

import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public final class SortKeyUtils {

    public static String generateKeyForAgentStat(StatDataPoint statDataPoint) {
        Objects.requireNonNull(statDataPoint, "statDataPoint");
        DataPoint point = statDataPoint.getDataPoint();
        String metricName = statDataPoint.getAgentStatType().getChartType();
        return generateKeyForAgentStat(point.getApplicationName(), point.getAgentId(), metricName);
    }

    public static String generateKeyForAgentStat(String applicationName, String agentId, String metricName) {
        return applicationName +
                "#" +
                agentId +
                "#" +
                metricName;
    }

    public static String generateKeyForApplicationStat(String applicationName, String metricName) {
        return applicationName +
                "#" +
                metricName;
    }
}
