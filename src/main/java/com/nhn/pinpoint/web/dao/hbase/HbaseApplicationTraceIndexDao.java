package com.nhn.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.hbase.LimitEventHandler;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.DateUtils;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author emeroad
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("transactionIdMapper")
	private RowMapper<List<TransactionId>> traceIndexMapper;

	@Autowired
	@Qualifier("traceIndexScatterMapper")
	private RowMapper<List<Dot>> traceIndexScatterMapper;

    @Autowired
    @Qualifier("traceIdRowKeyDistributor")
    private AbstractRowKeyDistributor traceIdRowKeyDistributor;

	private int scanCacheSize = 256;

	public void setScanCacheSize(int scanCacheSize) {
		this.scanCacheSize = scanCacheSize;
	}

	@Override
	public LimitedScanResult<List<TransactionId>> scanTraceIndex(final String applicationName, long start, long end, int limit) {
        logger.debug("scanTraceIndex");
		Scan scan = createScan(applicationName, start, end);
		
		final LimitedScanResult<List<TransactionId>> limitedScanResult = new LimitedScanResult<List<TransactionId>>();
        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor);

        List<TransactionId> transactionIdSum = new ArrayList<TransactionId>(128);
        for(List<TransactionId> transactionId: traceIndexList) {
            transactionIdSum.addAll(transactionId);
        }
		limitedScanResult.setScanData(transactionIdSum);

        if (transactionIdSum.size() >= limit) {
            Long lastRowTimestamp = lastRowAccessor.getLastRowTimestamp();
            limitedScanResult.setLimitedTime(lastRowTimestamp);
            if (logger.isDebugEnabled()) {
                logger.debug("lastRowTimestamp lastTime:{}", DateUtils.longToDateStr(lastRowTimestamp));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("scanner start lastTime:{}", DateUtils.longToDateStr(start));
            }
            limitedScanResult.setLimitedTime(start);
        }


		return limitedScanResult;
	}

    private class LastRowAccessor implements LimitEventHandler {

        private Long lastRowTimestamp = -1L;

        @Override
        public void handleLastResult(Result lastResult) {
            if (lastResult == null) {
                return;
            }
            KeyValue[] keyValueArray = lastResult.raw();
            KeyValue last = keyValueArray[keyValueArray.length - 1];
            byte[] row = last.getRow();
            byte[] originalRow = traceIdRowKeyDistributor.getOriginalKey(row);
            long reverseStartTime = BytesUtils.bytesToLong(originalRow, PinpointConstants.APPLICATION_NAME_MAX_LEN);
            this.lastRowTimestamp = TimeUtils.recoveryCurrentTimeMillis(reverseStartTime);

            if (logger.isDebugEnabled()) {
                logger.debug("lastRowTimestamp {}", DateUtils.longToDateStr(lastRowTimestamp));
            }
        }

        private Long getLastRowTimestamp() {
            return lastRowTimestamp;
        }
    }

	private Scan createScan(String applicationName, long start, long end) {
		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);

		byte[] bAgent = Bytes.toBytes(applicationName);
		byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, start);
		byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, end);

		// key가 reverse되었기 떄문에 start, end가 뒤바뀌게 된다.
		scan.setStartRow(traceIndexEndKey);
		scan.setStopRow(traceIndexStartKey);

		scan.addFamily(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE);
		scan.setId("ApplicationTraceIndexScan");

		// json으로 변화해서 로그를 찍어서. 최초 변환 속도가 느림.
		logger.trace("create scan:{}", scan);
		return scan;
	}

	@Override
	public List<List<Dot>> scanTraceScatter(String applicationName, long start, long end) {
        logger.debug("scanTraceScatter");
		Scan scan = createScan(applicationName, start, end);
        return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, traceIndexScatterMapper);
	}

	@Override
	public List<Dot> scanTraceScatter2(String applicationName, long start, long end, final int limit) {
        logger.debug("scanTraceScatter2");
        Scan scan = createScan(applicationName, start, end);

        List<List<Dot>> dotListList = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, limit, traceIndexScatterMapper);
        List<Dot> mergeList = new ArrayList<Dot>(limit + 10);
        for(List<Dot> dotList : dotListList) {
            mergeList.addAll(dotList);
        }
        return mergeList;
	}
}
