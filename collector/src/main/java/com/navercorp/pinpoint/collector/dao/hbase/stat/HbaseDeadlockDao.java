/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.DeadlockSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseDeadlockDao implements AgentStatDaoV2<DeadlockBo> {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    @Autowired
    private DeadlockSerializer deadlockSerializer;

    @Override
    public void insert(String agentId, List<DeadlockBo> deadlockBos) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (CollectionUtils.isEmpty(deadlockBos)) {
            return;
        }
        List<Put> deadlockPuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.DEADLOCK, deadlockBos, this.deadlockSerializer);
        if (!deadlockPuts.isEmpty()) {
            List<Put> rejectedPuts = this.hbaseTemplate.asyncPut(HBaseTables.AGENT_STAT_VER2, deadlockPuts);
            if (CollectionUtils.isNotEmpty(rejectedPuts)) {
                this.hbaseTemplate.put(HBaseTables.AGENT_STAT_VER2, rejectedPuts);
            }
        }
    }

}
