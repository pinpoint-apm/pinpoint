package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.bo.ServerMetaDataBo;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.Range;

import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
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

    /**
     * agentId, startTime을 기반으로 유니크한 AgentInfo를 찾아낸다.
     * @param agentId
     * @param range
     * @return
     */
    @Override
	public List<AgentInfoBo> getAgentInfo(final String agentId, final Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }


        logger.debug("get agentInfo with, agentId={}, {}", agentId, range);
    	
        Scan scan = new Scan();
        scan.setCaching(20);
        
		long fromTime = TimeUtils.reverseTimeMillis(range.getTo());
		long toTime = TimeUtils.reverseTimeMillis(1);

        byte[] agentIdBytes = Bytes.toBytes(agentId);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, fromTime);
        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, toTime);

        scan.setStartRow(startKeyBytes);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(HBaseTables.AGENTINFO_CF_INFO);

        List<AgentInfoBo> found = hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new ResultsExtractor<List<AgentInfoBo>>() {
			@Override
			public List<AgentInfoBo> extractData(ResultScanner results) throws Exception {
				final List<AgentInfoBo> result = new ArrayList<AgentInfoBo>();
				int found = 0;
                for (Result next : results) {
					found++;
					byte[] row = next.getRow();
					long reverseStartTime = BytesUtils.bytesToLong(row, HBaseTables.AGENT_NAME_MAX_LEN);
					long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);
					byte[] serializedAgentInfo = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
					byte[] serializedServerMetaData = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA);
					        
					logger.debug("found={}, {}, start={}", found, range, startTime);
					
					if (found > 1 && startTime <= range.getFrom()) {
						logger.debug("stop finding agentInfo.");
						break;
					}
					
					final AgentInfoBo.Builder agentInfoBoBuilder = new AgentInfoBo.Builder(serializedAgentInfo).agentId(agentId).startTime(startTime);
					if (serializedServerMetaData != null) {
					    agentInfoBoBuilder.serverMetaData(new ServerMetaDataBo.Builder(serializedServerMetaData).build());
					}
					final AgentInfoBo agentInfoBo = agentInfoBoBuilder.build();
					
					logger.debug("found agentInfoBo {}", agentInfoBo);
					result.add(agentInfoBo);
				}
				logger.debug("extracted agentInfoBo {}", result);
				return result;
			}
		});
        
        logger.debug("get agentInfo result, {}", found);
        
        return found;
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
                    long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);
                    logger.debug("agent:{} startTime value {}", agentId, startTime);
                    // 바로 전 시작 시간을 찾아야 한다.
                    if (startTime < currentTime) {
                        byte[] serializedAgentInfo = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
                        byte[] serializedServerMetaData = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA);
                        
                        final AgentInfoBo.Builder agentInfoBoBuilder = new AgentInfoBo.Builder(serializedAgentInfo).agentId(agentId).startTime(startTime);
                        if (serializedServerMetaData != null) {
                            agentInfoBoBuilder.serverMetaData(new ServerMetaDataBo.Builder(serializedServerMetaData).build());
                        }
                        final AgentInfoBo agentInfoBo = agentInfoBoBuilder.build();
                        
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
        long startTime = TimeUtils.reverseTimeMillis(currentTime);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, startTime);
        scan.setStartRow(startKeyBytes);

        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(HBaseTables.AGENTINFO_CF_INFO);

        return scan;
    }
}
