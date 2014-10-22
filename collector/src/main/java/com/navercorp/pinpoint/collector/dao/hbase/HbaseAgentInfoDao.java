package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.thrift.dto.TServerMetaData;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.bo.ServerMetaDataBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.collector.dao.AgentInfoDao;
import com.nhn.pinpoint.collector.mapper.thrift.ThriftBoMapper;

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

		// 추가 agent 정보를 넣어야 됨. 일단 sqlMetaData에 필요한 starttime만 넣음.
		AgentInfoBo agentInfoBo = this.agentInfoBoMapper.map(agentInfo);
		byte[] agentInfoBoValue = agentInfoBo.writeValue();
		put.add(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER, agentInfoBoValue);
		
		if (agentInfo.isSetServerMetaData()) {
		    ServerMetaDataBo serverMetaDataBo = this.serverMetaDataBoMapper.map(agentInfo.getServerMetaData());
		    byte[] serverMetaDataBoValue = serverMetaDataBo.writeValue();
		    put.add(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA, serverMetaDataBoValue);
		}
		
		hbaseTemplate.put(HBaseTables.AGENTINFO, put);
	}
}
