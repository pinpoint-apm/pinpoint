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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper2;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper3;
import com.navercorp.pinpoint.web.mapper.TransactionIdMapper;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTimeRange;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
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
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 * @author netspider
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

    private static final int APPLICATION_TRACE_INDEX_NUM_PARTITIONS = 32;

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
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(final String applicationName, Range range, int limit, boolean scanBackward) {
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
        Scan scan = createScan(applicationName, range, scanBackward);

        final LimitedScanResult<List<TransactionId>> limitedScanResult = new LimitedScanResult<>();
        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.findParallel(HBaseTables.APPLICATION_TRACE_INDEX,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = new ArrayList<>(128);
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

        final LimitedScanResult<List<TransactionId>> limitedScanResult = new LimitedScanResult<>();
        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.findParallel(HBaseTables.APPLICATION_TRACE_INDEX,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = new ArrayList<>(128);
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

            Cell[] rawCells = lastResult.rawCells();
            Cell last = rawCells[rawCells.length - 1];
            byte[] row = CellUtil.cloneRow(last);
            byte[] originalRow = traceIdRowKeyDistributor.getOriginalKey(row);
            long reverseStartTime = BytesUtils.bytesToLong(originalRow, PinpointConstants.APPLICATION_NAME_MAX_LEN);
            this.lastRowTimestamp = TimeUtils.recoveryTimeMillis(reverseStartTime);
            
            byte[] qualifier = CellUtil.cloneQualifier(last);
            this.lastTransactionId = TransactionIdMapper.parseVarTransactionId(qualifier, 0, qualifier.length);
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
        return createScan(applicationName, range, true);
    }

    private Scan createScan(String applicationName, Range range, boolean scanBackward) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);

        byte[] bApplicationName = Bytes.toBytes(applicationName);
        byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bApplicationName, range.getFrom());
        byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bApplicationName, range.getTo());

        if (scanBackward) {
            // start key is replaced by end key because key has been reversed
            scan.setStartRow(traceIndexEndKey);
            scan.setStopRow(traceIndexStartKey);
        } else {
            scan.setReversed(true);
            scan.setStartRow(traceIndexStartKey);
            scan.setStopRow(traceIndexEndKey);
        }

        scan.addFamily(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE);
        scan.setId("ApplicationTraceIndexScan");

        // toString() method of Scan converts a message to json format so it is slow for the first time.
        logger.trace("create scan:{}", scan);
        return scan;
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
        // not used yet. instead, use another row mapper (testing)
        // scan.setFilter(makeResponseTimeFilter(area, offsetTransactionId, offsetTransactionElapsed));

        // method 2
        ResponseTimeRange responseTimeRange = area.getResponseTimeRange();
        TraceIndexScatterMapper2 mapper = new TraceIndexScatterMapper2(responseTimeRange.getFrom(), responseTimeRange.getTo());

        List<List<Dot>> dotListList = hbaseOperations2.findParallel(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, limit, mapper, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<Dot> result = new ArrayList<>();
        for(List<Dot> dotList : dotListList) {
            result.addAll(dotList);
        }

        return result;
    }

    @Override
    public ScatterData scanTraceScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean scanBackward) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatterDataMadeOfDotGroup");
        Scan scan = createScan(applicationName, range, scanBackward);

        TraceIndexScatterMapper3 mapper = new TraceIndexScatterMapper3(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        List<ScatterData> dotGroupList = hbaseOperations2.findParallel(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, limit, mapper, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        if (CollectionUtils.isEmpty(dotGroupList)) {
            return new ScatterData(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        } else {
            ScatterData firstScatterData = dotGroupList.get(0);
            for (int i = 1; i < dotGroupList.size(); i++) {
                firstScatterData.merge(dotGroupList.get(i));
            }

            return firstScatterData;
        }
    }

    /**
     * make the hbase filter for selecting values of y-axis(response time) in order to select transactions in scatter chart.
     * 4 bytes for elapsed time should be attached for the prefix of column qualifier for to use this filter.
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
            buffer.putInt(offsetTransactionElapsed);
            buffer.putPrefixedString(offsetTransactionId.getAgentId());
            buffer.putSVLong(offsetTransactionId.getAgentStartTime());
            buffer.putVLong(offsetTransactionId.getTransactionSequence());
            byte[] qualifierOffset = buffer.getBuffer();

            filterList.addFilter(new QualifierFilter(CompareOp.GREATER, new BinaryPrefixComparator(qualifierOffset)));
        }
        return filterList;
    }
}
