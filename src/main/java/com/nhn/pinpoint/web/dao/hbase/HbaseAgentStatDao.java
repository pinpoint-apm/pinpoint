package com.nhn.pinpoint.web.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.web.vo.Range;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.dao.AgentStatDao;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author emeroad
 */
@Repository
public class HbaseAgentStatDao implements AgentStatDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("agentStatMapper")
	private RowMapper<List<TAgentStat>> agentStatMapper;
	
    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

	private int scanCacheSize = 256;

	public void setScanCacheSize(int scanCacheSize) {
		this.scanCacheSize = scanCacheSize;
	}
	
	public List<TAgentStat> scanAgentStatList(String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        if (logger.isDebugEnabled()) {
			logger.debug("scanAgentStat : agentId={}, {}", agentId, range);
		}
		

		Scan scan = createScan(agentId, range);
		
		List<List<TAgentStat>> intermediate = hbaseOperations2.find(HBaseTables.AGENT_STAT, scan, rowKeyDistributor, agentStatMapper);
		
		int expectedSize = (int)(range.getRange() / 5000); // 5초간 데이터
        List<TAgentStat> merged = new ArrayList<TAgentStat>(expectedSize);
        
        for(List<TAgentStat> each : intermediate) {
            merged.addAll(each);
        }
		
        return merged;
	}
	
	/**
	 * timestamp 기반의 row key를 만든다.
	 * FIXME collector에 있는 DAO에도 동일한 코드가 중복되어 있으니 참고.
	 */
	private byte[] getRowKey(String agentId, long timestamp) {
		if (agentId == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		byte[] bAgentId = BytesUtils.toBytes(agentId);
		return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseCurrentTimeMillis(timestamp));
	}

	private Scan createScan(String agentId, Range range) {
		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);

		byte[] startKey = getRowKey(agentId, range.getFrom());
		byte[] endKey = getRowKey(agentId, range.getTo());

		// key가 reverse되었기 떄문에 start, end가 뒤바뀌게 된다.
		scan.setStartRow(endKey);
		scan.setStopRow(startKey);

		scan.addColumn(HBaseTables.AGENT_STAT_CF_STATISTICS, HBaseTables.AGENT_STAT_CF_STATISTICS_V1);
		scan.setId("AgentStatScan");

		// json으로 변화해서 로그를 찍어서. 최초 변환 속도가 느림.
		logger.debug("create scan:{}", scan);
		return scan;
	}

//	public List<AgentStat> scanAgentStatList(String agentId, long start, long end, final int limit) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("scanAgentStatList");
//		}
//		Scan scan = createScan(agentId, start, end);
//		
//		List<AgentStat> list = hbaseOperations2.find(HBaseTables.AGENT_STAT, scan, rowKeyDistributor, new ResultsExtractor<List<AgentStat>>() {
//			@Override
//			public List<AgentStat> extractData(ResultScanner results) throws Exception {
//				TDeserializer deserializer = new TDeserializer();
//				List<AgentStat> list = new ArrayList<AgentStat>();
//				for (Result result : results) {
//					if (result == null) {
//						continue;
//					}
//					
//					if (list.size() >= limit) {
//						break;
//					}
//					
//					for (KeyValue kv : result.raw()) {
//						AgentStat agentStat = new AgentStat();
//						deserializer.deserialize(agentStat, kv.getBuffer());
//						list.add(agentStat);
//					}
//				}
//				return list;
//			}
//		});
//		return list;
//	}
	
}
