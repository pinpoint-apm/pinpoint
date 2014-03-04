package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;
import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT;
import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT_CF_STATISTICS;
import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT_CF_STATISTICS_V1;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.collector.dao.AgentStatDao;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * Agent 통계 정보를 저장하는 AgentStat 테이블에 접근하기 위한 DAO
 * 
 * @author harebox
 * @author emeroad
 */
@Repository
public class HbaseAgentStatDao implements AgentStatDao {

    @Autowired
	private HbaseOperations2 hbaseTemplate;

    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

	public void insert(final TAgentStat agentStat, final byte[] value) {
        if (agentStat == null) {
            throw new NullPointerException("agentStat must not be null");
        }
        long timestamp = agentStat.getTimestamp();
		byte[] key = getDistributedRowKey(agentStat, timestamp);

		Put put = new Put(key);
		put.add(AGENT_STAT_CF_STATISTICS, AGENT_STAT_CF_STATISTICS_V1, timestamp, value);
		
		hbaseTemplate.put(AGENT_STAT, put);
	}

	/**
	 * timestamp 기반의 row key를 만든다.
	 */
	private byte[] getRowKey(String agentId, long timestamp) {
		if (agentId == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		byte[] bAgentId = BytesUtils.toBytes(agentId);
		return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
	}
	
	/**
	 * row key를 bucket 단위로 분산시킨다.  
	 */
    private byte[] getDistributedRowKey(TAgentStat agentStat, long timestamp) {
        byte[] key = getRowKey(agentStat.getAgentId(), timestamp);
        return rowKeyDistributor.getDistributedKey(key);
    }

}
