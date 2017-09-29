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

package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.alarm.collector.AgentEventDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseMapStatisticsCallerDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author minwoo.jung
 */
@Component
public class DataCollectorFactory {

    public final static long SLOT_INTERVAL_FIVE_MIN = 300000;

    public final static long SLOT_INTERVAL_THREE_MIN = 180000;

    @Autowired
    private HbaseMapResponseTimeDao hbaseMapResponseTimeDao;

    @Autowired
    @Qualifier("jvmGcDaoFactory")
    private AgentStatDao<JvmGcBo> jvmGcDao;

    @Autowired
    @Qualifier("cpuLoadDaoFactory")
    private AgentStatDao<CpuLoadBo> cpuLoadDao;

    @Autowired
    @Qualifier("dataSourceDaoFactory")
    private AgentStatDao<DataSourceListBo> dataSourceDao;

    @Autowired
    private AgentEventDao agentEventDao;

    @Autowired
    private HbaseApplicationIndexDao hbaseApplicationIndexDao;

    @Autowired
    private HbaseMapStatisticsCallerDao mapStatisticsCallerDao;

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
        }

        throw new IllegalArgumentException("unable to create DataCollector : " + checker.getName());
    }

    public enum DataCollectorCategory {
        RESPONSE_TIME,
        AGENT_STAT,
        AGENT_EVENT,
        DATA_SOURCE_STAT,
        CALLER_STAT
    }

}
