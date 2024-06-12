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
import com.navercorp.pinpoint.batch.alarm.collector.HeapDataGetter;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.AgentFieldUsage;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
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
public class HeapDataCollector extends DataCollector implements HeapDataGetter {

    private final static String METRIC_NAME = "jvmGc";
    private final static String FIELD_HEAP_USED = "heapUsed";
    private final static String FIELD_HEAP_MAX = "heapMax";
    private final AlarmDao alarmDao;
    private final Application application;
    private final Map<String, Long> agentHeapUsageRate = new HashMap<>();
    private final List<String> fieldList = List.of(FIELD_HEAP_MAX, FIELD_HEAP_USED);
    private final long timeSlotEndTime;
    private final long slotInterval;

    public HeapDataCollector(DataCollectorCategory dataCollectorCategory, AlarmDao alarmDao, Application application, long timeSlotEndTime, long slotInterval) {
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
        List<AgentFieldUsage> agentFieldUsageList = alarmDao.selectSumGroupByField(application.name(), METRIC_NAME, fieldList, range);
        Map<String, AgentHeapUsage> agentHeapUsageMap = new HashMap<>();

        for(AgentFieldUsage agentFieldUsage : agentFieldUsageList) {
            String agentId = agentFieldUsage.getAgentId();
            AgentHeapUsage agentHeapUsage = agentHeapUsageMap.computeIfAbsent(agentId, AgentHeapUsage::new);

            if (FIELD_HEAP_MAX.equals(agentFieldUsage.getFieldName())) {
                agentHeapUsage.setHeapMax(agentFieldUsage.getValue());
            } else if (FIELD_HEAP_USED.equals(agentFieldUsage.getFieldName())) {
                agentHeapUsage.setHeapUsed(agentFieldUsage.getValue());
            }
        }
        for (Map.Entry<String, AgentHeapUsage> entry : agentHeapUsageMap.entrySet()) {
            String agentId = entry.getKey();
            AgentHeapUsage agentHeapUsage = entry.getValue();
            long heapUsagePercent = calculatePercent(agentHeapUsage.getHeapUsed().longValue(), agentHeapUsage.getHeapMax().longValue());
            agentHeapUsageRate.put(agentId, heapUsagePercent);
        }
    }

    @Override
    public Map<String, Long> getHeapUsageRate() {
        return agentHeapUsageRate;
    }

    private static class AgentHeapUsage {
        private final String agentId;
        private Double heapMax;
        private Double heapUsed;

        public AgentHeapUsage(String agentId) {
            this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        }

        public void setHeapMax(Double heapMax) {
            this.heapMax = heapMax;
        }

        public void setHeapUsed(Double heapUsed) {
            this.heapUsed = heapUsed;
        }

        public String getAgentId() {
            return agentId;
        }

        public Double getHeapMax() {
            return heapMax;
        }

        public Double getHeapUsed() {
            return heapUsed;
        }
    }
}
