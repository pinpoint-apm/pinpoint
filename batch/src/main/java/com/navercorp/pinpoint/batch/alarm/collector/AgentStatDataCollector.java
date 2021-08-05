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

package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class AgentStatDataCollector extends DataCollector {

    private final Application application;
    private final AgentStatDao<JvmGcBo> jvmGcDao;
    private final AgentStatDao<CpuLoadBo> cpuLoadDao;
    private final ApplicationIndexDao applicationIndexDao;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final AtomicBoolean init = new AtomicBoolean(false); // need to consider a race condition when checkers start simultaneously.

    private final Map<String, Long> agentHeapUsageRate = new HashMap<>();
    private final Map<String, Long> agentGcCount = new HashMap<>();
    private final Map<String, Long> agentJvmCpuUsageRate = new HashMap<>();
    private final Map<String, Long> agentSystemCpuUsageRate = new HashMap<>();

    public AgentStatDataCollector(DataCollectorCategory category, Application application, AgentStatDao<JvmGcBo> jvmGcDao, AgentStatDao<CpuLoadBo> cpuLoadDao, ApplicationIndexDao applicationIndexDao, long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = application;
        this.jvmGcDao = jvmGcDao;
        this.cpuLoadDao = cpuLoadDao;
        this.applicationIndexDao = applicationIndexDao;
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        Range range = Range.newUncheckedRange(timeSlotEndTime - slotInterval, timeSlotEndTime);
        List<String> agentIds = applicationIndexDao.selectAgentIds(application.getName());

        for(String agentId : agentIds) {
            List<JvmGcBo> jvmGcBos = jvmGcDao.getAgentStatList(agentId, range);
            List<CpuLoadBo> cpuLoadBos = cpuLoadDao.getAgentStatList(agentId, range);
            long totalHeapSize = 0;
            long usedHeapSize = 0;
            long jvmCpuUsaged = 0;
            long systemCpuUsaged = 0;

            for (JvmGcBo jvmGcBo : jvmGcBos) {
                totalHeapSize += jvmGcBo.getHeapMax();
                usedHeapSize += jvmGcBo.getHeapUsed();
            }

            for (CpuLoadBo cpuLoadBo : cpuLoadBos) {
                jvmCpuUsaged += cpuLoadBo.getJvmCpuLoad() * 100;
                systemCpuUsaged += cpuLoadBo.getSystemCpuLoad() * 100;
            }

            if (!jvmGcBos.isEmpty()) {
                long percent = calculatePercent(usedHeapSize, totalHeapSize);
                agentHeapUsageRate.put(agentId, percent);

                long accruedLastGcCount = jvmGcBos.get(0).getGcOldCount();
                long accruedFirstGcCount = jvmGcBos.get(jvmGcBos.size() - 1).getGcOldCount();
                agentGcCount.put(agentId, accruedLastGcCount - accruedFirstGcCount);
            }
            if (!cpuLoadBos.isEmpty()) {
                long jvmCpuUsagedPercent = calculatePercent(jvmCpuUsaged, 100 * cpuLoadBos.size());
                agentJvmCpuUsageRate.put(agentId, jvmCpuUsagedPercent);
                long systemCpuUsagedPercent = calculatePercent(systemCpuUsaged, 100 * cpuLoadBos.size());
                agentSystemCpuUsageRate.put(agentId, systemCpuUsagedPercent);
            }

        }

        init.set(true);

    }

    public Map<String, Long> getHeapUsageRate() {
        return agentHeapUsageRate;
    }

    public Map<String, Long> getGCCount() {
        return agentGcCount;
    }

    public Map<String, Long> getJvmCpuUsageRate() {
        return agentJvmCpuUsageRate;
    }

    public Map<String, Long> getSystemCpuUsageRate() { return agentSystemCpuUsageRate; }
}
