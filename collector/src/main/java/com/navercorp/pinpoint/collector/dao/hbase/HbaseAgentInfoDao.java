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
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	@Auto    ired
	private HbaseOperations2 hbaseT       mplate;
	@Autowired
	@Qualifier("ag    ntInfoBoMapper")
	private ThriftBoMapper<AgentInfoBo, TAgentInfo> agentInfoBoMapper;
    
    @Autowired
    @Qualifier("serverMetaDataBoMapper")
    private ThriftBoMapper<ServerMetaDataBo, TServerMetaData> serv    rMetaDa    aBoMapper;

	@Override
	public void insert(TAgentInfo agentInfo) {
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }

        if (          ogger.isDebugEnabled()) {
			logger.debug("             nsert agent info. {}", agentInfo);
		}

		byte[] a       entId = Bytes.toBytes(agentInfo.getAgentId());
		long reverseKey = TimeUt       ls.reverseTimeMillis(agentInfo.getStartTimestamp());
		byte[] rowKey = RowKeyUtils.concatFixedByteAn       Long(agentId, HBaseTabl       s.AGENT_NAME_MAX_LEN, reverseKey);
		Put put = new Put(rowKey);

		// should add addi       ional agent informations. for now added only starttime for s       lMetaData
		AgentInfoBo agentInfoBo = this.agen       InfoBoMapper.map(agentInfo);
		byte[] agentInfoBoValue = agentInfoBo.writeValue();
		put.add(HB             seTables.AGENTINFO_CF_INFO, HBa       eTables.AGENTINFO_CF_INFO_IDENTIFIER, agentInfoBoValue);
		
		if (agentInfo.isSetServerMetaData())
		    ServerMetaDataBo serverMetaDataBo = this.serverMetaDat       BoMapper.map(agentInfo.getServerMetaData());
		    byte[] serverMetaDataBoValue = serverMetaDataBo.writeValue(                   ;
		    put.add(HBaseTables.AGENTINFO    CF_INFO, HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA, serverMetaDataBoValue);
		}
		
		hbaseTemplate.put(HBaseTables.AGENTINFO, put);
	}
}
