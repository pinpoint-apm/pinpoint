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

import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.dao.stat.SampledActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledCpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDataSourceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledDeadlockDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.SampledResponseTimeDao;
import com.navercorp.pinpoint.web.dao.stat.SampledTransactionDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class HbaseSampledAgentStatDualReadDao<S extends SampledAgentStatDataPoint> implements SampledAgentStatDao<S> {

    private final SampledAgentStatDao<S> master;
    private final SampledAgentStatDao<S> slave;

    protected HbaseSampledAgentStatDualReadDao(SampledAgentStatDao<S> master, SampledAgentStatDao<S> slave) {
        this.master = master;
        this.slave = slave;
    }

    @Override
    public List<S> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        List<S> sampledAgentStats = this.master.getSampledAgentStatList(agentId, timeWindow);
        if (CollectionUtils.isNotEmpty(sampledAgentStats)) {
            return sampledAgentStats;
        } else {
            return this.slave.getSampledAgentStatList(agentId, timeWindow);
        }
    }

    public static class SampledJvmGcDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledJvmGc> implements SampledJvmGcDao {
        public SampledJvmGcDualReadDao(SampledAgentStatDao<SampledJvmGc> master, SampledAgentStatDao<SampledJvmGc> slave) {
            super(master, slave);
        }
    }

    public static class SampledJvmGcDetailedDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledJvmGcDetailed> implements SampledJvmGcDetailedDao {
        public SampledJvmGcDetailedDualReadDao(SampledAgentStatDao<SampledJvmGcDetailed> master, SampledAgentStatDao<SampledJvmGcDetailed> slave) {
            super(master, slave);
        }
    }

    public static class SampledCpuLoadDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledCpuLoad> implements SampledCpuLoadDao {
        public SampledCpuLoadDualReadDao(SampledAgentStatDao<SampledCpuLoad> master, SampledAgentStatDao<SampledCpuLoad> slave) {
            super(master, slave);
        }
    }

    public static class SampledTransactionDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledTransaction> implements SampledTransactionDao {
        public SampledTransactionDualReadDao(SampledAgentStatDao<SampledTransaction> master, SampledAgentStatDao<SampledTransaction> slave) {
            super(master, slave);
        }
    }

    public static class SampledActiveTraceDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledActiveTrace> implements SampledActiveTraceDao {
        public SampledActiveTraceDualReadDao(SampledAgentStatDao<SampledActiveTrace> master, SampledAgentStatDao<SampledActiveTrace> slave) {
            super(master, slave);
        }
    }

    public static class SampledDataSourceDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledDataSourceList> implements SampledDataSourceDao {
        public SampledDataSourceDualReadDao(SampledAgentStatDao<SampledDataSourceList> master, SampledAgentStatDao<SampledDataSourceList> slave) {
            super(master, slave);
        }
    }

    public static class SampledResponseTimeDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledResponseTime> implements SampledResponseTimeDao {
        public SampledResponseTimeDualReadDao(SampledAgentStatDao<SampledResponseTime> master, SampledAgentStatDao<SampledResponseTime> slave) {
            super(master, slave);
        }
    }

    public static class SampledDeadlockDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledDeadlock> implements SampledDeadlockDao {
        public SampledDeadlockDualReadDao(SampledAgentStatDao<SampledDeadlock> master, SampledAgentStatDao<SampledDeadlock> slave) {
            super(master, slave);
        }
    }

}
