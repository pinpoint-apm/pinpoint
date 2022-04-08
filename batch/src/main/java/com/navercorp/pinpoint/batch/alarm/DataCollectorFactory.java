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
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseMapStatisticsCallerDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component
public class DataCollectorFactory {

    public final static long SLOT_INTERVAL_FIVE_MIN = 300000;

    public final static long SLOT_INTERVAL_THREE_MIN = 180000;

    private final HbaseMapResponseTimeDao hbaseMapResponseTimeDao;

    private final AgentStatDao<JvmGcBo> jvmGcDao;

    private final AgentStatDao<CpuLoadBo> cpuLoadDao;

    private final AgentStatDao<DataSourceListBo> dataSourceDao;

    private final AgentStatDao<FileDescriptorBo> fileDescriptorDao;

    private final AgentEventDao agentEventDao;

    private final HbaseApplicationIndexDao hbaseApplicationIndexDao;

    private final HbaseMapStatisticsCallerDao mapStatisticsCallerDao;

    public DataCollectorFactory(HbaseMapResponseTimeDao hbaseMapResponseTimeDao,
                                AgentStatDao<JvmGcBo> jvmGcDao,
                                AgentStatDao<CpuLoadBo> cpuLoadDao,
                                AgentStatDao<DataSourceListBo> dataSourceDao,
                                AgentStatDao<FileDescriptorBo> fileDescriptorDao,
                                AgentEventDao agentEventDao,
                                HbaseApplicationIndexDao hbaseApplicationIndexDao,
                                HbaseMapStatisticsCallerDao mapStatisticsCallerDao) {
        this.hbaseMapResponseTimeDao = Objects.requireNonNull(hbaseMapResponseTimeDao, "hbaseMapResponseTimeDao");
        this.jvmGcDao = Objects.requireNonNull(jvmGcDao, "jvmGcDao");
        this.cpuLoadDao = Objects.requireNonNull(cpuLoadDao, "cpuLoadDao");
        this.dataSourceDao = Objects.requireNonNull(dataSourceDao, "dataSourceDao");
        this.fileDescriptorDao = Objects.requireNonNull(fileDescriptorDao, "fileDescriptorDao");
        this.agentEventDao = Objects.requireNonNull(agentEventDao, "agentEventDao");
        this.hbaseApplicationIndexDao = Objects.requireNonNull(hbaseApplicationIndexDao, "hbaseApplicationIndexDao");
        this.mapStatisticsCallerDao = Objects.requireNonNull(mapStatisticsCallerDao, "mapStatisticsCallerDao");
    }

    public DataCollector createDataCollector(CheckerCategory checker, Application application, long timeSlotEndTime) {
        switch (checker.getDataCollectorCategory()) {
            case RESPONSE_TIME:
                return new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, hbaseMapResponseTimeDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case AGENT_STAT:
                return new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, jvmGcDao, cpuLoadDao, hbaseApplicationIndexDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case AGENT_EVENT:
                return new AgentEventDataCollector(DataCollectorCategory.AGENT_EVENT, application, agentEventDao, hbaseApplicationIndexDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case CALLER_STAT:
                return new MapStatisticsCallerDataCollector(DataCollectorCategory.CALLER_STAT, application, mapStatisticsCallerDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case DATA_SOURCE_STAT:
                return new DataSourceDataCollector(DataCollectorCategory.DATA_SOURCE_STAT, application, dataSourceDao, hbaseApplicationIndexDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
            case FILE_DESCRIPTOR:
                return new FileDescriptorDataCollector(DataCollectorCategory.FILE_DESCRIPTOR, application, fileDescriptorDao, hbaseApplicationIndexDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
        }

        throw new IllegalArgumentException("unable to create DataCollector : " + checker.getName());
    }

}
