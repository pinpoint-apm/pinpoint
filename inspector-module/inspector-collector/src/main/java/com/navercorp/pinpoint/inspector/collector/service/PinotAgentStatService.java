/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.collector.service;

import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.dao.pinot.PinotTypeMapper;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service("pinotAgentStatService")
public class PinotAgentStatService implements AgentStatService {
    private final Logger logger = LogManager.getLogger(getClass());

    private final PinotTypeMapper<StatDataPoint>[] mappers;

    private final AgentStatDao agentStatDao;

    private final TenantProvider tenantProvider;

    public PinotAgentStatService(PinotMappers mappers,
                                 TenantProvider tenantProvider,
                                 AgentStatDao agentStatDao) {
        this.mappers = getTypeMappers(mappers);

        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");

    }

    @SuppressWarnings("unchecked")
    private PinotTypeMapper<StatDataPoint>[] getTypeMappers(PinotMappers mappers) {
        List<PinotTypeMapper<StatDataPoint>> mapper = mappers.getMapper();
        logger.info("AgentStatDao mappers: {}", mapper.size());
        return mapper.toArray(new PinotTypeMapper[0]);
    }

    @Override
    public void save(@Valid AgentStatBo agentStatBo) {
        for (PinotTypeMapper<StatDataPoint> mapper : mappers) {
            List<StatDataPoint> agentStatData = mapper.point(agentStatBo);
            if (!validateTime(agentStatData)) {
                continue;
            }

            List<AgentStat> agentStatList = mapper.agentStat(agentStatData, tenantProvider.getTenantId());
            this.agentStatDao.insertAgentStat(agentStatList);

            List<ApplicationStat> applicationStatList = mapper.applicationStat(agentStatList);
            this.agentStatDao.insertApplicationStat(applicationStatList);
        }
    }


    private boolean validateTime(List<? extends StatDataPoint> agentStatData) {
        if (agentStatData.isEmpty()) {
            return false;
        }
        StatDataPoint agentStat = agentStatData.get(0);
        DataPoint point = agentStat.getDataPoint();
        Instant collectedTime = Instant.ofEpochMilli(point.getTimestamp());
        Instant validTime = Instant.now().minus(Duration.ofMinutes(10));

        if (validTime.isBefore(collectedTime)) {
            return true;
        }
        if (logger.isInfoEnabled()) {
            logger.info("AgentStat data is invalid. applicationName: {}, agentId: {}, time: {}",
                    point.getApplicationName(), point.getAgentId(), new Date(point.getTimestamp()));
        }
        return false;
    }
}
