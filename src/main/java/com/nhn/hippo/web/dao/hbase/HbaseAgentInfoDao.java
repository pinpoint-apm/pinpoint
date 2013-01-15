package com.nhn.hippo.web.dao.hbase;

import com.nhn.hippo.web.dao.AgentInfoDao;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.RowKeyUtils;
import com.profiler.common.util.TimeUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    /**
     * currentTime에서 가장 근접한 시간의 agent startTime을 find한다.
     *
     * @param agentInfo
     * @param currentTime
     * @return
     */
    @Override
    public long selectAgentInfoBeforeStartTime(final String agentInfo, final long currentTime) {
        Scan scan = createScan(agentInfo, currentTime);
        Long startTime = hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new ResultsExtractor<Long>() {
            @Override
            public Long extractData(ResultScanner results) throws Exception {
                for (Result next; (next = results.next()) != null; ) {
                    byte[] row = next.getRow();
                    long reverseStartTime = BytesUtils.bytesToLong(row, RowKeyUtils.AGENT_NAME_LIMIT);
                    long startTime = TimeUtils.recoveryCurrentTimeMillis(reverseStartTime);
                    logger.debug("agent:{} startTime value {}", agentInfo, startTime);
                    // 바로 전 시작 시간을 찾아야 한다.
                    if (startTime < currentTime) {
                        logger.info("agent:{} startTime find {}", agentInfo, startTime);
                        return startTime;
                    }
                }
                return 0L;
            }
        });

//        if (startTime == null) {
//            return -1;
//        }
        return startTime;
    }

    private Scan createScan(String agentInfo, long currentTime) {
        Scan scan = new Scan();
        scan.setCaching(20);

        byte[] agentIdBytes = Bytes.toBytes(agentInfo);
        long startTime = TimeUtils.reverseCurrentTimeMillis(currentTime);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, RowKeyUtils.AGENT_NAME_LIMIT, startTime);
        scan.setStartRow(startKeyBytes);

        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, RowKeyUtils.AGENT_NAME_LIMIT, Long.MAX_VALUE);
        scan.setStopRow(endKeyBytes);

        return scan;
    }
}
