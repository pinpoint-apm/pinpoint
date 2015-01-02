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

package com.navercorp.pinpoint.web.dao.hbase;

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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.common.util.SpanUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper2;
import com.navercorp.pinpoint.web.mapper.TransactionIdMapper;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTimeRange;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author emeroad
 * @author netspider
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	@Auto    ired
	private HbaseOperations2 hbaseOpera    ions2;

    @Autowired
	@Qualifier("transac    ionIdMapper")
	private RowMapper<List<TransactionId>> t    aceIndex    apper;

	@Autowired
	@Qualifier("tr    ceIndexScatterMapper")
	private RowMapper<List<Dot>> traceIndexScatterMapper;

    @Autowired
    @Qualifier("applicationTraceIndexDistributor")
    private AbstractRowKeyDistributor     raceIdRowKeyDistributor;

	priv    te int scanCacheSize = 256;

	public void setSc       nCacheSize(int scanCacheSize) {        	this.s    anCacheSize = scanCacheSize;
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
        logger.debug("scanTraceIndex             );
		Scan scan = createScan(applicationName, range);
		
		final LimitedScanResult<List<TransactionId>> limitedScanResult = new LimitedScanResult<List<TransactionId>>();
        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor);

        List<TransactionId> transactionIdSum = new ArrayList<TransactionId>(128);
        for(List<TransactionId> transactionId: traceIndexList        {
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
                  limitedScanR        ult.set    imitedTime(range.getFrom());
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
            throw new IllegalArgumentE       ception("negative limit:" + limit);
        }
        log             er.debug("scanTraceIndex");
		Scan scan = createScan(applicationName, area.getTimeRange());
		
		final LimitedScanResult<List<TransactionId>> limitedScanResult = new LimitedScanResult<List<TransactionId>>();
        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor);

        List<TransactionId> transactionIdSum = new ArrayList<TransactionId>(128);
        for(List<Trans       ctionId> transactionId: traceIndexList) {
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
                logger.debug("scanner start lastTime:{}", DateUtils.longToDateStr(area.getTimeRange().getF       om()));
            }                limitedScanResult.setLimitedTime(area.getTimeRange().getFrom());
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
            long reverseStartTime = BytesUtils.bytesToLong(originalRow, PinpointConstants.APPL          CATION_NAME_MAX_LEN);
                     this.lastRowTimestamp = TimeUtils.recoveryTimeMillis(reverseStartTime);

			byte[] qualifier = last.getQualifier();
			this.lastTransactionId = TransactionIdMapper.parseVarTransactionId(qualifier, 0);
			this.lastTransactionElapsed = BytesUtils.bytesToInt(qualifier, 0);
            
            if (logger.isDebugEnabled()) {
                logger.debug("lastRowTimestamp={}, lastTransactionId={}, lastTransactionElapsed={}", DateUtils.longToDateStr(lastRowTimestamp), lastTransaction       d, lastTransactionElapsed);
            }                  }

                     rivate Long getLastRowTimestamp() {                      return lastR       wTim    stamp;
        }

		public TransactionId getLastTransactionI       () {
			return last       ransactionId;
		}

		public int g       tLastTransactionElapsed() {
			return lastT       ansactionElapsed;
		}
    }

	private Scan createScan(String applicationName, R       nge range) {
		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);

		byte[] bAgent = Bytes.toBytes(applicationName);
		byte[] traceIndexSt       rtKey = SpanUtils.getTraceIndex       owKey(bAgent, range.getFrom());
	       byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(b       gent, range.getTo());

        // start key is replaced by end key because key has been reversed
		scan.setStartRow(traceIndexEndKey);
		scan.       etStopRow(traceIndexStartKey);

	       scan.add        mily(HB    seTables.APPLICATION_TRACE_INDEX_CF_TRACE);
		scan.setId("ApplicationTraceIndexScan");

        // toString() method of Scan converts a message to json format so it is slow for the first time.
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

        List<List<Dot>> dotListList = hbaseOperations2.find(HBaseTables.APPLICA              O    _    RACE_IN    EX, scan, traceIdRowKeyDistributor, limit, traceIndexScatterMapper);
        List<Dot> mergeList = new ArrayList<Dot>(limit + 10);
        for(List<Dot> dotLis        : dotListList) {
                     mergeList.addAll(dotList);
        }
        return mergeL             st;
	}
	
	/**          	 *
	 */
	@Override
	public List<Dot> scanTraceSca             ter(String          applicationName, SelectedScatterArea area, TransactionId             offsetTransactionId, int of       setTransactionElapsed, int limit) {
		if (applicationName        = null) {
			throw new NullPointerException("applicationName must not be n       ll");
		}
		if (area == null) {
			throw new NullPointerException("range must not be null")
		}       		if (limit < 0) {
			throw new IllegalArgumentException("nega       ive limit:" + limit);
		}
		logger.debug("scanTraceScatter");
		Scan scan = createScan(applicationName, area.getTim             Range());

		// method 1
        // not used yet. instead, use another row mapper (testing)
		// scan.setFilter(makeResponseTimeF             lter(area, offsetTransactionId, o       fsetTransactionElapsed));
		
		//           ethod 2
		Respons                   TimeR          nge responseTimeRange = area.getResponseTimeRange();
		TraceIndexScatterMapper2 mapper = new TraceIndexScatterMapper2(responseTimeRange.getFrom(), responseTimeRange.getTo());
		
		List<List<Dot>> dotListList = hbaseOperations2.find(HBa        Tables.APPLI    ATION_TRACE_INDEX, scan, tr    ceIdRowKeyDistributor, limit, ma    per);
		    	    List<Dot> result = new ArrayList<Dot>();
		for(List<Dot> dotList : dotListList) {
			result.addAll(dotList);
		}
		
		return result;
	}
	
	/       *
     * make the hbas        filter for selecting values of y-axis(response time) in order       to select transactions in scatter chart.
     * 4 bytes for e       apsed time should be attached for the prefix of column qu       lifier for to use this filter.
	 *
	 * @param area
	 * @par       m offsetTransactionId
	 * @param offsetTransactionElapsed
	 * @return
	 */
	private Filter makeResponseTimeF       lter(final SelectedScatterArea area, final TransactionId offsetTransactionId, int offsetTransactionElap             ed) {
       	// filter by response time
		          esponseTimeRange responseTimeRange = are          .getResponseTimeRange();
		byte          ] responseFrom = Bytes.toBytes(responseTimeRange.getF          om());
		byte[] responseTo = Bytes.toBytes(respons          TimeRange.getTo());
		FilterList filterList = new Filt          rList(Operator.MUST_PASS_ALL);
		filter          ist.addFilter(new QualifierFilter(CompareOp.GREATER_OR_EQUAL, new BinaryPrefixComparator(responseFro             )));
		filte    List.addFilter(new QualifierFilter(CompareOp.LESS_OR_EQUAL, new BinaryPrefixComparator(responseTo)));
		
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
