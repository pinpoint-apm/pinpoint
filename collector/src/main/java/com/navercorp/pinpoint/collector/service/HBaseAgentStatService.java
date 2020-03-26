/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service("hBaseAgentStatService")
public class HBaseAgentStatService implements AgentStatService {

    private final Logger logger = LoggerFactory.getLogger(HBaseAgentStatService.class.getName());

    private final AgentStatDaoV2<JvmGcBo> jvmGcDao;

    private final AgentStatDaoV2<JvmGcDetailedBo> jvmGcDetailedDao;

    private final AgentStatDaoV2<CpuLoadBo> cpuLoadDao;

    private final AgentStatDaoV2<TransactionBo> transactionDao;

    private final AgentStatDaoV2<ActiveTraceBo> activeTraceDao;

    private final AgentStatDaoV2<DataSourceListBo> dataSourceListDao;

    private final AgentStatDaoV2<ResponseTimeBo> responseTimeDao;

    private final AgentStatDaoV2<DeadlockThreadCountBo> deadlockDao;

    private final AgentStatDaoV2<FileDescriptorBo> fileDescriptorDao;

    private final AgentStatDaoV2<DirectBufferBo> directBufferDao;

    public HBaseAgentStatService(AgentStatDaoV2<JvmGcBo> jvmGcDao,
                                 AgentStatDaoV2<JvmGcDetailedBo> jvmGcDetailedDao,
                                 AgentStatDaoV2<CpuLoadBo> cpuLoadDao,
                                 AgentStatDaoV2<TransactionBo> transactionDao,
                                 AgentStatDaoV2<ActiveTraceBo> activeTraceDao,
                                 AgentStatDaoV2<DataSourceListBo> dataSourceListDao,
                                 AgentStatDaoV2<ResponseTimeBo> responseTimeDao,
                                 AgentStatDaoV2<DeadlockThreadCountBo> deadlockDao,
                                 AgentStatDaoV2<FileDescriptorBo> fileDescriptorDao,
                                 AgentStatDaoV2<DirectBufferBo> directBufferDao) {
        this.jvmGcDao = Objects.requireNonNull(jvmGcDao, "jvmGcDao");
        this.jvmGcDetailedDao = Objects.requireNonNull(jvmGcDetailedDao, "jvmGcDetailedDao");
        this.cpuLoadDao = Objects.requireNonNull(cpuLoadDao, "cpuLoadDao");
        this.transactionDao = Objects.requireNonNull(transactionDao, "transactionDao");
        this.activeTraceDao = Objects.requireNonNull(activeTraceDao, "activeTraceDao");
        this.dataSourceListDao = Objects.requireNonNull(dataSourceListDao, "dataSourceListDao");
        this.responseTimeDao = Objects.requireNonNull(responseTimeDao, "responseTimeDao");
        this.deadlockDao = Objects.requireNonNull(deadlockDao, "deadlockDao");
        this.fileDescriptorDao = Objects.requireNonNull(fileDescriptorDao, "fileDescriptorDao");
        this.directBufferDao = Objects.requireNonNull(directBufferDao, "directBufferDao");
    }

    @Override
    public void save(AgentStatBo agentStatBo) {
        final String agentId = agentStatBo.getAgentId();
        try {
            this.jvmGcDao.insert(agentId, agentStatBo.getJvmGcBos());
            this.jvmGcDetailedDao.insert(agentId, agentStatBo.getJvmGcDetailedBos());
            this.cpuLoadDao.insert(agentId, agentStatBo.getCpuLoadBos());
            this.transactionDao.insert(agentId, agentStatBo.getTransactionBos());
            this.activeTraceDao.insert(agentId, agentStatBo.getActiveTraceBos());
            this.dataSourceListDao.insert(agentId, agentStatBo.getDataSourceListBos());
            this.responseTimeDao.insert(agentId, agentStatBo.getResponseTimeBos());
            this.deadlockDao.insert(agentId, agentStatBo.getDeadlockThreadCountBos());
            this.fileDescriptorDao.insert(agentId, agentStatBo.getFileDescriptorBos());
            this.directBufferDao.insert(agentId, agentStatBo.getDirectBufferBos());
        } catch (Exception e) {
            logger.warn("Error inserting AgentStatBo. Caused:{}", e.getMessage(), e);
        }
    }

}
