/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat.compatibility;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.dao.stat.ActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.dao.stat.CpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.DataSourceDao;
import com.navercorp.pinpoint.web.dao.stat.DeadlockDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.ResponseTimeDao;
import com.navercorp.pinpoint.web.dao.stat.TransactionDao;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class HbaseAgentStatDualReadDao<T extends AgentStatDataPoint> implements AgentStatDao<T> {

    private final AgentStatDao<T> master;
    private final AgentStatDao<T> slave;

    protected HbaseAgentStatDualReadDao(AgentStatDao<T> master, AgentStatDao<T> slave) {
        this.master = master;
        this.slave = slave;
    }

    @Override
    public List<T> getAgentStatList(String agentId, Range range) {
        List<T> agentStats = this.master.getAgentStatList(agentId, range);
        if (CollectionUtils.isNotEmpty(agentStats)) {
            return agentStats;
        } else {
            return this.slave.getAgentStatList(agentId, range);
        }
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        boolean exists = this.master.agentStatExists(agentId, range);
        if (exists) {
            return true;
        } else {
            return this.slave.agentStatExists(agentId, range);
        }
    }

    public static class JvmGcDualReadDao extends HbaseAgentStatDualReadDao<JvmGcBo> implements JvmGcDao {
        public JvmGcDualReadDao(AgentStatDao<JvmGcBo> master, AgentStatDao<JvmGcBo> slave) {
            super(master, slave);
        }
    }

    public static class JvmGcDetailedDualReadDao extends HbaseAgentStatDualReadDao<JvmGcDetailedBo> implements JvmGcDetailedDao {
        public JvmGcDetailedDualReadDao(AgentStatDao<JvmGcDetailedBo> master, AgentStatDao<JvmGcDetailedBo> slave) {
            super(master, slave);
        }
    }

    public static class CpuLoadDualReadDao extends HbaseAgentStatDualReadDao<CpuLoadBo> implements CpuLoadDao {
        public CpuLoadDualReadDao(AgentStatDao<CpuLoadBo> master, AgentStatDao<CpuLoadBo> slave) {
            super(master, slave);
        }
    }

    public static class TransactionDualReadDao extends HbaseAgentStatDualReadDao<TransactionBo> implements TransactionDao {
        public TransactionDualReadDao(AgentStatDao<TransactionBo> master, AgentStatDao<TransactionBo> slave) {
            super(master, slave);
        }
    }

    public static class ActiveTraceDualReadDao extends HbaseAgentStatDualReadDao<ActiveTraceBo> implements ActiveTraceDao {
        public ActiveTraceDualReadDao(AgentStatDao<ActiveTraceBo> master, AgentStatDao<ActiveTraceBo> slave) {
            super(master, slave);
        }
    }

    public static class DataSourceDualReadDao extends HbaseAgentStatDualReadDao<DataSourceListBo> implements DataSourceDao {
        public DataSourceDualReadDao(AgentStatDao<DataSourceListBo> master, AgentStatDao<DataSourceListBo> slave) {
            super(master, slave);
        }
    }

    public static class ResponseTimeDualReadDao extends HbaseAgentStatDualReadDao<ResponseTimeBo> implements ResponseTimeDao {
        public ResponseTimeDualReadDao(AgentStatDao<ResponseTimeBo> master, AgentStatDao<ResponseTimeBo> slave) {
            super(master, slave);
        }
    }

    public static class DeadlockDualReadDao extends HbaseAgentStatDualReadDao<DeadlockBo> implements DeadlockDao {
        public DeadlockDualReadDao(AgentStatDao<DeadlockBo> master, AgentStatDao<DeadlockBo> slave) {
            super(master, slave);
        }
    }

}
