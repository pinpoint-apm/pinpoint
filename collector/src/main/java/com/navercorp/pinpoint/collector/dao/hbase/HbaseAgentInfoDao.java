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

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author emeroad
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.AgentInfo> descriptor;

    public HbaseAgentInfoDao(HbaseOperations2 hbaseTemplate, TableDescriptor<HbaseColumnFamily.AgentInfo> descriptor) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void insert(AgentInfoBo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert agent info. {}", agentInfo);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(agentInfo.getAgentId());
        // Assert applicationName
        CollectorUtils.checkApplicationName(agentInfo.getApplicationName());

        final byte[] agentId = Bytes.toBytes(agentInfo.getAgentId());
        final long reverseKey = TimeUtils.reverseTimeMillis(agentInfo.getStartTime());
        final byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(agentId, HbaseTableConstatns.AGENT_NAME_MAX_LEN, reverseKey);
        final Put put = new Put(rowKey);

        // should add additional agent informations. for now added only starttime for sqlMetaData
        final byte[] agentInfoBoValue = agentInfo.writeValue();
        put.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_IDENTIFIER, agentInfoBoValue);

        if (agentInfo.getServerMetaData() != null) {
            final byte[] serverMetaDataBoValue = agentInfo.getServerMetaData().writeValue();
            put.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_SERVER_META_DATA, serverMetaDataBoValue);
        }

        if (agentInfo.getJvmInfo() != null) {
            final byte[] jvmInfoBoValue = agentInfo.getJvmInfo().writeValue();
            put.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_JVM, jvmInfoBoValue);
        }

        final TableName agentInfoTableName = descriptor.getTableName();
        hbaseTemplate.put(agentInfoTableName, put);
    }
}