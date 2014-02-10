package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.mapper.AgentInfoMapper;
import com.nhn.pinpoint.web.vo.Range;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    private RowMapper<List<AgentInfoBo>> agentInfoMapper = new AgentInfoMapper();

    /**
     * agentId, startTime을 기반으로 유니크한 AgentInfo를 찾아낸다.
     * @param agentId
     * @param from
     * @param to
     * @return
     */
    @Override
	public List<AgentInfoBo> getAgentInfo(final String agentId, final Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        logger.debug("get agentInfo with, agentId={}, {}", agentId, range);
    	
        Scan scan = new Scan();
        scan.setCaching(20);
        
		long fromTime = TimeUtils.reverseCurrentTimeMillis(range.getTo());
		long toTime = TimeUtils.reverseCurrentTimeMillis(1);

        byte[] agentIdBytes = Bytes.toBytes(agentId);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, fromTime);
        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, toTime);

        scan.setStartRow(startKeyBytes);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(HBaseTables.AGENTINFO_CF_INFO);

        List<AgentInfoBo> found = hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new ResultsExtractor<List<AgentInfoBo>>() {
			@Override
			public List<AgentInfoBo> extractData(ResultScanner results) throws Exception {
				List<AgentInfoBo> result = new ArrayList<AgentInfoBo>();
				int found = 0;
				for (Result next; (next = results.next()) != null;) {
					found++;
					byte[] row = next.getRow();
					long reverseStartTime = BytesUtils.bytesToLong(row, HBaseTables.AGENT_NAME_MAX_LEN);
					long startTime = TimeUtils.recoveryCurrentTimeMillis(reverseStartTime);
					byte[] value = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
					
					logger.debug("found={}, {}, start={}", found, range, startTime);
					
					if (found > 1 && startTime <= range.getFrom()) {
						logger.debug("stop finding agentinfo.");
						break;
					}
					
					AgentInfoBo agentInfoBo = new AgentInfoBo();
					agentInfoBo.setAgentId(agentId);
					agentInfoBo.setStartTime(startTime);
					agentInfoBo.readValue(value);
					
					logger.debug("found agentInfoBo {}", agentInfoBo);
					result.add(agentInfoBo);
				}
				logger.debug("extracted agentInfoBo {}", result);
				return result;
			}
		});
        
        logger.debug("get agentInfo result, {}", found);
        
        return found;
        
//        F 1382320380000
//        S 1382579080389
//        T 1382579580000
        
//        F 1382557980000
//        S 1382579080389
//        T 1382579580000
        
//		byte[] agentIdBytes = Bytes.toBytes(agentId);
//		long reverseStartTime = TimeUtils.reverseCurrentTimeMillis(from);
//		byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, reverseStartTime);
//
//		Get get = new Get(rowKey);
//		get.addFamily(HBaseTables.AGENTINFO_CF_INFO);
//
//		List<AgentInfoBo> agentInfoBoList = hbaseOperations2.get(HBaseTables.AGENTINFO, get, agentInfoMapper);
//		return agentInfoBoList;
    }

    /**
     * currentTime에서 가장 근접한 시간의 agent startTime을 find한다.
     *
     * @param agentId
     * @param currentTime
     * @return
     */
    @Override
    @Deprecated
    public AgentInfoBo findAgentInfoBeforeStartTime(final String agentId, final long currentTime) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        // TODO cache를 걸어야 될듯 하다.
        Scan scan = createScan(agentId, currentTime);
        AgentInfoBo agentInfoBo = hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new ResultsExtractor<AgentInfoBo>() {
            @Override
            public AgentInfoBo extractData(ResultScanner results) throws Exception {
                for (Result next : results) {
                    byte[] row = next.getRow();
                    long reverseStartTime = BytesUtils.bytesToLong(row, HBaseTables.AGENT_NAME_MAX_LEN);
                    long startTime = TimeUtils.recoveryCurrentTimeMillis(reverseStartTime);
                    logger.debug("agent:{} startTime value {}", agentId, startTime);
                    // 바로 전 시작 시간을 찾아야 한다.
                    if (startTime < currentTime) {
                        byte[] value = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
                        AgentInfoBo agentInfoBo = new AgentInfoBo();
                        agentInfoBo.setAgentId(agentId);
                        agentInfoBo.setStartTime(startTime);
                        agentInfoBo.readValue(value);
                        
                        logger.debug("agent:{} startTime find {}", agentId, startTime);

                        return agentInfoBo;
                    }
                }
                
                logger.warn("agentInfo not found. agentId={}, time={}", agentId, currentTime);
                
                return null;
            }
        });

//        if (startTime == null) {
//            return -1;
//        }
        return agentInfoBo;
    }

    private Scan createScan(String agentInfo, long currentTime) {
        Scan scan = new Scan();
        scan.setCaching(20);

        byte[] agentIdBytes = Bytes.toBytes(agentInfo);
        long startTime = TimeUtils.reverseCurrentTimeMillis(currentTime);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, startTime);
        scan.setStartRow(startKeyBytes);

        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(HBaseTables.AGENTINFO_CF_INFO);

        return scan;
    }
}
