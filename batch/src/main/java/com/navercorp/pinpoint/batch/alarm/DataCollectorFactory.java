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
import com.navercorp.pinpoint.batch.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.FileDescriptorDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
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

    public final static long SLOT_INTERVAL_THREE_MIN = 180000;

    private final MapResponseDao mapResponseDao;

    private final AgentStatDao<JvmGcBo> jvmGcDao;

    private final AgentStatDao<CpuLoadBo> cpuLoadDao;

    private final AgentStatDao<DataSourceListBo> dataSourceDao;

    private final AgentStatDao<FileDescriptorBo> fileDescriptorDao;

    private final AgentEventDao agentEventDao;

    private final MapStatisticsCallerDao callerDao;

    public DataCollectorFactory(MapResponseDao mapResponseDao,
                                AgentStatDao<JvmGcBo> jvmGcDao,
                                AgentStatDao<CpuLoadBo> cpuLoadDao,
                                AgentStatDao<DataSourceListBo> dataSourceDao,
                                AgentStatDao<FileDescriptorBo> fileDescriptorDao,
                                AgentEventDao agentEventDao,
                                MapStatisticsCallerDao callerDao) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
        this.jvmGcDao = Objects.requireNonNull(jvmGcDao, "jvmGcDao");
        this.cpuLoadDao = Objects.requireNonNull(cpuLoadDao, "cpuLoadDao");
        this.dataSourceDao = Objects.requireNonNull(dataSourceDao, "dataSourceDao");
        this.fileDescriptorDao = Objects.requireNonNull(fileDescriptorDao, "fileDescriptorDao");
        this.agentEventDao = Objects.requireNonNull(agentEventDao, "agentEventDao");
        this.callerDao = Objects.requireNonNull(callerDao, "callerDao");
    }

    public DataCollector createDataCollector(CheckerCategory checker, Application application, Supplier<List<String>> agentIds, long timeSlotEndTime) {
        return switch (checker.getDataCollectorCategory()) {
            case RESPONSE_TIME ->
                    new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mapResponseDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case AGENT_STAT ->
                    new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, jvmGcDao, cpuLoadDao, agentIds.get(), timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case AGENT_EVENT ->
                    new AgentEventDataCollector(DataCollectorCategory.AGENT_EVENT, agentEventDao, agentIds.get(), timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case CALLER_STAT ->
                    new MapStatisticsCallerDataCollector(DataCollectorCategory.CALLER_STAT, application, callerDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case DATA_SOURCE_STAT ->
                    new DataSourceDataCollector(DataCollectorCategory.DATA_SOURCE_STAT, dataSourceDao, agentIds.get(), timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case FILE_DESCRIPTOR ->
                    new FileDescriptorDataCollector(DataCollectorCategory.FILE_DESCRIPTOR, fileDescriptorDao, agentIds.get(), timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
        };

    }

}
