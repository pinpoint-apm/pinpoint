package com.nhn.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.hbase.LimitEventHandler;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.DateUtils;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.mapper.TraceIndexScatterMapper2;
import com.nhn.pinpoint.web.mapper.TransactionIdMapper;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTimeRange;
import com.nhn.pinpoint.web.vo.SelectedScatterArea;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author emeroad
 * @author netspider
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
    @Qualifier("applicationTraceIndexDistributor")
    private AbstractRowKeyDistributor traceIdRowKeyDistributor;

	private int scanCacheSize = 256;

	public void setScanCacheSize(int scanCacheSize) {
		this.scanCacheSize = scanCacheSize;
	}

	@Override
	public LimitedScanResult<List<TransactionId>> scanTraceIndex(final String applicationName, Range range, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex");
		Scan scan = createScan(applicationName, range);
		
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
                logger.debug("scanner start lastTime:{}", DateUtils.longToDateStr(range.getFrom()));
            }
            limitedScanResult.setLimitedTime(range.getFrom());
        }

		return limitedScanResult;
	}

	@Override
	public LimitedScanResult<List<TransactionId>> scanTraceIndex(final String applicationName, SelectedScatterArea area, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (area == null) {
            throw new NullPointerException("area must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex");
		Scan scan = createScan(applicationName, area.getTimeRange());
		
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
                logger.debug("scanner start lastTime:{}", DateUtils.longToDateStr(area.getTimeRange().getFrom()));
            }
            limitedScanResult.setLimitedTime(area.getTimeRange().getFrom());
        }

		return limitedScanResult;
	}

    private class LastRowAccessor implements LimitEventHandler {
        private Long lastRowTimestamp = -1L;
        private TransactionId lastTransactionId = null;
        private int lastTransactionElapsed = -1;

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
            this.lastRowTimestamp = TimeUtils.recoveryTimeMillis(reverseStartTime);
            
			byte[] qualifier = last.getQualifier();
			this.lastTransactionId = TransactionIdMapper.parseVarTransactionId(qualifier, 0);
			this.lastTransactionElapsed = BytesUtils.bytesToInt(qualifier, 0);
            
            if (logger.isDebugEnabled()) {
                logger.debug("lastRowTimestamp={}, lastTransactionId={}, lastTransactionElapsed={}", DateUtils.longToDateStr(lastRowTimestamp), lastTransactionId, lastTransactionElapsed);
            }
        }

        private Long getLastRowTimestamp() {
            return lastRowTimestamp;
        }

		public TransactionId getLastTransactionId() {
			return lastTransactionId;
		}

		public int getLastTransactionElapsed() {
			return lastTransactionElapsed;
		}
    }

	private Scan createScan(String applicationName, Range range) {
		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);

		byte[] bAgent = Bytes.toBytes(applicationName);
		byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, range.getFrom());
		byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, range.getTo());

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
	public List<Dot> scanTraceScatter(String applicationName, Range range, final int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatter");
        Scan scan = createScan(applicationName, range);

        List<List<Dot>> dotListList = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, limit, traceIndexScatterMapper);
        List<Dot> mergeList = new ArrayList<Dot>(limit + 10);
        for(List<Dot> dotList : dotListList) {
            mergeList.addAll(dotList);
        }
        return mergeList;
	}
	
	/**
	 * 
	 */
	@Override
	public List<Dot> scanTraceScatter(String applicationName, SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed, int limit) {
		if (applicationName == null) {
			throw new NullPointerException("applicationName must not be null");
		}
		if (area == null) {
			throw new NullPointerException("range must not be null");
		}
		if (limit < 0) {
			throw new IllegalArgumentException("negative limit:" + limit);
		}
		logger.debug("scanTraceScatter");
		Scan scan = createScan(applicationName, area.getTimeRange());

		// method 1
		// 아직 사용하지 않음. 대신 row mapper를 다른것을 사용. (테스트.)
		// scan.setFilter(makeResponseTimeFilter(area, offsetTransactionId, offsetTransactionElapsed));
		
		// method 2
		ResponseTimeRange responseTimeRange = area.getResponseTimeRange();
		TraceIndexScatterMapper2 mapper = new TraceIndexScatterMapper2(responseTimeRange.getFrom(), responseTimeRange.getTo());
		
		List<List<Dot>> dotListList = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, limit, mapper);
		
		List<Dot> result = new ArrayList<Dot>();
		for(List<Dot> dotList : dotListList) {
			result.addAll(dotList);
		}
		
		return result;
	}
	
	/**
	 * scatter chart에 속하는 트랜잭션을 구하기위해 선택된 y축영역(response time) 내에 속하는 값을 필터하기 위한
	 * hbase filter를 생성한다. 이 필터를 사용하려면 column qualifier의 prefix로 elapsed time 4byte를
	 * 붙여두어야 한다.
	 * 
	 * @param area
	 * @param offsetTransactionId
	 * @param offsetTransactionElapsed
	 * @return
	 */
	private Filter makeResponseTimeFilter(final SelectedScatterArea area, final TransactionId offsetTransactionId, int offsetTransactionElapsed) {
		// filter by response time
		ResponseTimeRange responseTimeRange = area.getResponseTimeRange();
		byte[] responseFrom = Bytes.toBytes(responseTimeRange.getFrom());
		byte[] responseTo = Bytes.toBytes(responseTimeRange.getTo());
		FilterList filterList = new FilterList(Operator.MUST_PASS_ALL);
		filterList.addFilter(new QualifierFilter(CompareOp.GREATER_OR_EQUAL, new BinaryPrefixComparator(responseFrom)));
		filterList.addFilter(new QualifierFilter(CompareOp.LESS_OR_EQUAL, new BinaryPrefixComparator(responseTo)));
		
		// add offset
		if (offsetTransactionId != null) {
			final Buffer buffer = new AutomaticBuffer(32);
			buffer.put(offsetTransactionElapsed);
			buffer.putPrefixedString(offsetTransactionId.getAgentId());
			buffer.putSVar(offsetTransactionId.getAgentStartTime());
			buffer.putVar(offsetTransactionId.getTransactionSequence());
			byte[] qualifierOffset = buffer.getBuffer();

			filterList.addFilter(new QualifierFilter(CompareOp.GREATER, new BinaryPrefixComparator(qualifierOffset)));
		}
		return filterList;
	}
}
