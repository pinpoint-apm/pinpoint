/*
 * Copyright 2018 Naver Corp.
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
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.FileDescriptorSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Repository
public class HbaseFileDescriptorDao implements AgentStatDaoV2<FileDescriptorBo> {

    @Qualifier("asyncPutHbaseTemplate")
    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private TableNameProvider tableNameProvider;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    @Autowired
    private FileDescriptorSerializer fileDescriptorSerializer;

    @Override
    public void insert(String agentId, List<FileDescriptorBo> fileDescriptorBos) {
        Objects.requireNonNull(agentId, "agentId");
        // Assert agentId
        CollectorUtils.checkAgentId(agentId);

        if (CollectionUtils.isEmpty(fileDescriptorBos)) {
            return;
        }
        List<Put> fileDescriptorPuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.FILE_DESCRIPTOR, fileDescriptorBos, this.fileDescriptorSerializer);
        if (!fileDescriptorPuts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_STAT_VER2);
            this.hbaseTemplate.asyncPut(agentStatTableName, fileDescriptorPuts);
        }
    }
}
