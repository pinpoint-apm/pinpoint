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
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.DeadlockThreadCountSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseDeadlockThreadCountDao implements AgentStatDaoV2<DeadlockThreadCountBo> {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private TableNameProvider tableNameProvider;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    @Autowired
    private DeadlockThreadCountSerializer deadlockThreadCountSerializer;

    @Override
    public void insert(String agentId, List<DeadlockThreadCountBo> deadlockThreadCountBos) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (CollectionUtils.isEmpty(deadlockThreadCountBos)) {
            return;
        }
        List<Put> deadlockPuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.DEADLOCK, deadlockThreadCountBos, this.deadlockThreadCountSerializer);
        if (!deadlockPuts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_STAT_VER2);
            List<Put> rejectedPuts = this.hbaseTemplate.asyncPut(agentStatTableName, deadlockPuts);
            if (CollectionUtils.isNotEmpty(rejectedPuts)) {
                this.hbaseTemplate.put(agentStatTableName, rejectedPuts);
            }
        }
    }

}
