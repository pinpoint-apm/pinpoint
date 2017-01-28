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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
@Service("agentStatHandlerV2")
public class AgentStatHandlerV2 implements Handler {

    private final Logger logger = LoggerFactory.getLogger(AgentStatHandlerV2.class.getName());

    @Autowired
    private AgentStatMapper agentStatMapper;

    @Autowired
    private AgentStatBatchMapper agentStatBatchMapper;

    @Autowired
    private AgentStatDaoV2<JvmGcBo> jvmGcDao;

    @Autowired
    private AgentStatDaoV2<JvmGcDetailedBo> jvmGcDetailedDao;

    @Autowired
    private AgentStatDaoV2<CpuLoadBo> cpuLoadDao;

    @Autowired
    private AgentStatDaoV2<TransactionBo> transactionDao;

    @Autowired
    private AgentStatDaoV2<ActiveTraceBo> activeTraceDao;

    @Autowired
    private AgentStatDaoV2<DataSourceListBo> dataSourceListDao;

    @Autowired(required = false)
    private AgentStatService agentStatService;

    @Override
    public void handle(TBase<?, ?> tbase) {
        // FIXME (2014.08) Legacy - TAgentStat should not be sent over the wire.
        if (tbase instanceof TAgentStat) {
            TAgentStat tAgentStat = (TAgentStat)tbase;
            this.handleAgentStat(tAgentStat);
        } else if (tbase instanceof TAgentStatBatch) {
            TAgentStatBatch tAgentStatBatch = (TAgentStatBatch) tbase;
            this.handleAgentStatBatch(tAgentStatBatch);
        } else {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + TAgentStat.class.getName() + " or " + TAgentStatBatch.class.getName());
        }

        if (agentStatService != null) {
            agentStatService.save(tbase);
        }

    }

    private void handleAgentStat(TAgentStat tAgentStat) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received TAgentStat={}", tAgentStat);
        }
        AgentStatBo agentStatBo = this.agentStatMapper.map(tAgentStat);
        this.insertAgentStatBatch(agentStatBo);
    }

    private void handleAgentStatBatch(TAgentStatBatch tAgentStatBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received TAgentStatBatch={}", tAgentStatBatch);
        }
        AgentStatBo agentStatBo = this.agentStatBatchMapper.map(tAgentStatBatch);
        this.insertAgentStatBatch(agentStatBo);
    }

    private void insertAgentStatBatch(AgentStatBo agentStatBo) {
        if (agentStatBo == null) {
            return;
        }
        final String agentId = agentStatBo.getAgentId();
        try {
            this.jvmGcDao.insert(agentId, agentStatBo.getJvmGcBos());
            this.jvmGcDetailedDao.insert(agentId, agentStatBo.getJvmGcDetailedBos());
            this.cpuLoadDao.insert(agentId, agentStatBo.getCpuLoadBos());
            this.transactionDao.insert(agentId, agentStatBo.getTransactionBos());
            this.activeTraceDao.insert(agentId, agentStatBo.getActiveTraceBos());
            this.dataSourceListDao.insert(agentId, agentStatBo.getDataSourceListBos());
        } catch (Exception e) {
            logger.warn("Error inserting AgentStatBo. Caused:{}", e.getMessage(), e);
        }
    }

}
