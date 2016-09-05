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
import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TJvmInfo;
import com.navercorp.pinpoint.thrift.dto.TServerMetaData;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author emeroad
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    @Qualifier("agentInfoBoMapper")
    private ThriftBoMapper<AgentInfoBo, TAgentInfo> agentInfoBoMapper;

    @Autowired
    @Qualifier("serverMetaDataBoMapper")
    private ThriftBoMapper<ServerMetaDataBo, TServerMetaData> serverMetaDataBoMapper;

    @Autowired
    @Qualifier("jvmInfoBoMapper")
    private ThriftBoMapper<JvmInfoBo, TJvmInfo> jvmInfoBoMapper;

    @Override
    public void insert(TAgentInfo agentInfo) {

        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("insert agent info. {}", agentInfo);
        }

        byte[] agentId = Bytes.toBytes(agentInfo.getAgentId());
        long reverseKey = TimeUtils.reverseTimeMillis(agentInfo.getStartTimestamp());
        byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(agentId, HBaseTables.AGENT_NAME_MAX_LEN, reverseKey);
        Put put = new Put(rowKey);

        // should add additional agent informations. for now added only starttime for sqlMetaData
        AgentInfoBo agentInfoBo = this.agentInfoBoMapper.map(agentInfo);
        byte[] agentInfoBoValue = agentInfoBo.writeValue();
        put.addColumn(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER, agentInfoBoValue);

        if (agentInfo.isSetServerMetaData()) {
            ServerMetaDataBo serverMetaDataBo = this.serverMetaDataBoMapper.map(agentInfo.getServerMetaData());
            byte[] serverMetaDataBoValue = serverMetaDataBo.writeValue();
            put.addColumn(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA, serverMetaDataBoValue);
        }

        if (agentInfo.isSetJvmInfo()) {
            JvmInfoBo jvmInfoBo = this.jvmInfoBoMapper.map(agentInfo.getJvmInfo());
            byte[] jvmInfoBoValue = jvmInfoBo.writeValue();
            put.addColumn(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_JVM, jvmInfoBoValue);
        }

        hbaseTemplate.put(HBaseTables.AGENTINFO, put);
    }
}
