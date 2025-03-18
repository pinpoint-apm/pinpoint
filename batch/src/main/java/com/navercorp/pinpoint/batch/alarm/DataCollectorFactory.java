/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.collector.AgentEventDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.MapOutLinkDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.pinot.DataSourceDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.pinot.FileDescriptorDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.pinot.HeapDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.pinot.JvmCpuDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.pinot.SystemCpuDataCollector;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author minwoo.jung
 */
@Component
public class DataCollectorFactory {

    public final static long SLOT_INTERVAL_FIVE_MIN = 300000;

    private final MapResponseDao mapResponseDao;

    private final AgentEventDao agentEventDao;

    private final MapOutLinkDao outLinkDao;

    private final AlarmDao alarmDao;
    
    public DataCollectorFactory(MapResponseDao mapResponseDao,
                                AgentEventDao agentEventDao,
                                MapOutLinkDao outLinkDao,
                                AlarmDao alarmDao) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
        this.agentEventDao = Objects.requireNonNull(agentEventDao, "agentEventDao");
        this.outLinkDao = Objects.requireNonNull(outLinkDao, "outLinkDao");
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
    }

    public DataCollector createDataCollector(CheckerCategory checker, Application application, Supplier<List<String>> agentIds, long timeSlotEndTime) {
        return switch (checker.getDataCollectorCategory()) {
            case RESPONSE_TIME ->
                    new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mapResponseDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case AGENT_EVENT ->
                    new AgentEventDataCollector(DataCollectorCategory.AGENT_EVENT, agentEventDao, agentIds.get(), timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case CALLER_STAT ->
                    new MapOutLinkDataCollector(DataCollectorCategory.CALLER_STAT, application, outLinkDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case DATA_SOURCE_STAT ->
                    new DataSourceDataCollector(DataCollectorCategory.DATA_SOURCE_STAT, alarmDao, application, agentIds.get(), timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case FILE_DESCRIPTOR ->
                    new FileDescriptorDataCollector(DataCollectorCategory.FILE_DESCRIPTOR, alarmDao, application, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case HEAP_USAGE_RATE ->
                    new HeapDataCollector(DataCollectorCategory.HEAP_USAGE_RATE, alarmDao, application, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case JVM_CPU_USAGE_RATE ->
                    new JvmCpuDataCollector(DataCollectorCategory.JVM_CPU_USAGE_RATE, alarmDao, application, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case SYSTEM_CPU_USAGE_RATE ->
                    new SystemCpuDataCollector(DataCollectorCategory.SYSTEM_CPU_USAGE_RATE, alarmDao, application, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
        };
    }

}
