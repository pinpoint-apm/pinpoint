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
import com.navercorp.pinpoint.batch.alarm.collector.FileDescriptorDataGetter;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsage;
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
public class FileDescriptorDataCollector extends DataCollector implements FileDescriptorDataGetter {
    private final static String METRIC_NAME = "fileDescriptor";
    private final static String FIELD_NAME = "openFileDescriptorCount";

    private final AlarmDao alarmDao;
    private final Application application;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final Map<String, Long> agentFileDescriptorCount = new HashMap<>();

    public FileDescriptorDataCollector(DataCollectorCategory dataCollectorCategory, AlarmDao alarmDao, Application application, long timeSlotEndTime, long slotInterval) {
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
        List<AgentUsage> agentUsageList = alarmDao.selectAvg(application.name(), METRIC_NAME, FIELD_NAME, range);

        for (AgentUsage agentUsage : agentUsageList) {
            agentFileDescriptorCount.put(agentUsage.getAgentId(), agentUsage.getValue().longValue());
        }
    }

    @Override
    public Map<String, Long> getFileDescriptorCount() {
        return agentFileDescriptorCount;
    }
}
