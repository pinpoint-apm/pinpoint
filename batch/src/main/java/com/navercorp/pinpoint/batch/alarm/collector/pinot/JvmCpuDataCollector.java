/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.alarm.collector.pinot;

import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.JvmCpuDataGetter;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsageCount;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class JvmCpuDataCollector extends DataCollector implements JvmCpuDataGetter {

    private final static String METRIC_NAME = "cpuLoad";
    private final static String FIELD_NAME = "jvm";

    private final AlarmDao alarmDao;
    private final Application application;
    private final long timeSlotEndTime;
    private final long slotInterval;

    private final Map<String, Long> agentJvmCpuUsageRate = new HashMap<>();

    public JvmCpuDataCollector(DataCollectorCategory dataCollectorCategory, AlarmDao alarmDao, Application application, long timeSlotEndTime, long slotInterval) {
        super(dataCollectorCategory);
        Objects.requireNonNull(dataCollectorCategory, "dataCollectorCategory");
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
        this.application = Objects.requireNonNull(application, "application");
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        Range range = Range.between(timeSlotEndTime - slotInterval, timeSlotEndTime);
        List<AgentUsageCount> agentUsageCountList = alarmDao.selectSumCount(application.name(), METRIC_NAME, FIELD_NAME, range);

        for (AgentUsageCount agentUsageCount : agentUsageCountList) {
            long jvmCpuUsagePercent = calculatePercent(agentUsageCount.getValue().longValue(), 100L * agentUsageCount.getCountValue().longValue());
            agentJvmCpuUsageRate.put(agentUsageCount.getAgentId(), jvmCpuUsagePercent);
        }
    }

    @Override
    public Map<String, Long> getJvmCpuUsageRate() {
        return agentJvmCpuUsageRate;
    }
}
