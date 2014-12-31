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

package com.navercorp.pinpoint.collector.dao.hbase;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * 
 * @author harebox
 * @author emeroad
 * @author hyungil.jeong
 */
@Repository
public class HbaseAgentStatDao implements AgentStatDao {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    @Qualifier("agentStatMemoryGcBoMapper")
    private ThriftBoMapper<AgentStatMemoryGcBo, TAgentStat> agentStatMemoryGcBoMapper;

    @Autowired
    @Qualifier("agentStatCpuLoadBoMapper")
    private ThriftBoMapper<AgentStatCpuLoadBo, TAgentStat> agentStatCpuLoadBoMapper;

    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

    public void insert(final TAgentStat agentStat) {
        if (agentStat == null) {
            throw new NullPointerException("agentStat must not be null");
        }
        long timestamp = agentStat.getTimestamp();
        byte[] key = getDistributedRowKey(agentStat, timestamp);

        Put put = new Put(key);

        final AgentStatMemoryGcBo agentStatMemoryGcBo = this.agentStatMemoryGcBoMapper.map(agentStat);
        put.add(AGENT_STAT_CF_STATISTICS, AGENT_STAT_CF_STATISTICS_MEMORY_GC, timestamp, agentStatMemoryGcBo.writeValue());

        final AgentStatCpuLoadBo agentStatCpuLoadBo = this.agentStatCpuLoadBoMapper.map(agentStat);
        put.add(AGENT_STAT_CF_STATISTICS, AGENT_STAT_CF_STATISTICS_CPU_LOAD, timestamp, agentStatCpuLoadBo.writeValue());

        hbaseTemplate.put(AGENT_STAT, put);
    }

    /**
     * Create row key based on the timestamp
     */
    private byte[] getRowKey(String agentId, long timestamp) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not null");
        }
        byte[] bAgentId = BytesUtils.toBytes(agentId);
        return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
    }

    /**
     * Create row key based on the timestamp and distribute it into different buckets 
     */
    private byte[] getDistributedRowKey(TAgentStat agentStat, long timestamp) {
        byte[] key = getRowKey(agentStat.getAgentId(), timestamp);
        return rowKeyDistributor.getDistributedKey(key);
    }

}
