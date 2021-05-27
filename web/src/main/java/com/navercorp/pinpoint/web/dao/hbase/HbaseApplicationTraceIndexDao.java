/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyBuilder;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.config.ScatterChartConfig;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper;
import com.navercorp.pinpoint.web.mapper.TransactionIdMapper;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.ElpasedTimeDotPredicate;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author emeroad
 * @author netspider
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

    private static final int APPLICATION_TRACE_INDEX_NUM_PARTITIONS = 32;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ScatterChartConfig scatterChartConfig;

    private final HbaseOperations2 hbaseOperations2;

    private final FuzzyRowKeyBuilder fuzzyRowKeyBuilder = new FuzzyRowKeyBuilder();

    private final RowMapper<List<TransactionId>> traceIndexMapper;

    private final RowMapper<List<Dot>> traceIndexScatterMapper;

    private final AbstractRowKeyDistributor traceIdRowKeyDistributor;

    private int scanCacheSize = 256;

    public HbaseApplicationTraceIndexDao(ScatterChartConfig scatterChartConfig,
                                         HbaseOperations2 hbaseOperations2,
                                         TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> descriptor,
                                         @Qualifier("transactionIdMapper") RowMapper<List<TransactionId>> traceIndexMapper,
                                         @Qualifier("traceIndexScatterMapper") RowMapper<List<Dot>> traceIndexScatterMapper,
                                         @Qualifier("applicationTraceIndexDistributor") AbstractRowKeyDistributor traceIdRowKeyDistributor) {
        this.scatterChartConfig = Objects.requireNonNull(scatterChartConfig, "scatterChartConfig");
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.traceIndexMapper = Objects.requireNonNull(traceIndexMapper, "traceIndexMapper");
        this.traceIndexScatterMapper = Objects.requireNonNull(traceIndexScatterMapper, "traceIndexScatterMapper");
        this.traceIdRowKeyDistributor = Objects.requireNonNull(traceIdRowKeyDistributor, "traceIdRowKeyDistributor");
    }

    public void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }

    private final TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> descriptor;

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(final String applicationName, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex {}", range);
        Scan scan = createScan(applicationName, range, scanBackward);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = descriptor.getTableName();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.findParallel(applicationTraceIndexTableName,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = ListListUtils.toList(traceIndexList);
        final long lastTime = getLastTime(range, limit, lastRowAccessor, transactionIdSum);

        return new LimitedScanResult<>(lastTime, transactionIdSum);
    }

    private <T> long getLastTime(Range range, int limit, LastRowAccessor lastRowAccessor, List<T> list) {
        if (list.size() >= limit) {
            Long lastRowTimestamp = lastRowAccessor.getLastRowTimestamp();
            if (logger.isDebugEnabled()) {
                logger.debug("lastRowTimestamp lastTime:{}", DateTimeFormatUtils.format(lastRowTimestamp));
            }
            return lastRowTimestamp;
        } else {
            long from = range.getFrom();
            if (logger.isDebugEnabled()) {
                logger.debug("scanner start lastTime:{}", DateTimeFormatUtils.format(from));
            }
            return from;
        }
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
                logger.debug("lastRowTimestamp={}, lastTransactionId={}, lastTransactionElapsed={}", DateTimeFormatUtils.format(lastRowTimestamp), lastTransactionId, lastTransactionElapsed);
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


    private Scan createScan(String applicationName, Range range, boolean scanBackward) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);

        byte[] bApplicationName = Bytes.toBytes(applicationName);
        byte[] traceIndexStartKey = SpanUtils.getApplicationTraceIndexRowKey(bApplicationName, range.getFrom());
        byte[] traceIndexEndKey = SpanUtils.getApplicationTraceIndexRowKey(bApplicationName, range.getTo());

        if (scanBackward) {
            // start key is replaced by end key because key has been reversed
            scan.withStartRow(traceIndexEndKey);
            scan.withStopRow(traceIndexStartKey);
        } else {
            scan.setReversed(true);
            scan.withStartRow(traceIndexStartKey);
            scan.withStopRow(traceIndexEndKey);
        }

        scan.addFamily(descriptor.getColumnFamilyName());
        scan.setId("ApplicationTraceIndexScan");

        // toString() method of Scan converts a message to json format so it is slow for the first time.
        logger.trace("create scan:{}", scan);
        return scan;
    }

    @Override
    public LimitedScanResult<List<Dot>> scanTraceScatterData(String applicationName, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatterDataMadeOfDotGroup");
        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        Scan scan = createScan(applicationName, range, scanBackward);

        TableName applicationTraceIndexTableName = descriptor.getTableName();
        List<List<Dot>> listList = hbaseOperations2.findParallel(applicationTraceIndexTableName, scan, traceIdRowKeyDistributor, limit, this.traceIndexScatterMapper, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);
        List<Dot> dots = ListListUtils.toList(listList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, dots);
        return new LimitedScanResult<>(lastTime, dots);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, DragArea dragArea, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragArea, "dragArea");

        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        final Range range = Range.newUncheckedRange(dragArea.getXLow(), dragArea.getXHigh());
        logger.debug("scanTraceIndex range:{}", range);
        final Scan scan = newFuzzyScanner(applicationName, dragArea, range);


        // TODO
//        Predicate<Dot> filter = ElpasedTimeDotPredicate.newDragAreaDotPredicate(dragArea);

        final TableName applicationTraceIndexTableName = descriptor.getTableName();
        List<List<TransactionId>> listList = this.hbaseOperations2.findParallel(applicationTraceIndexTableName,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = ListListUtils.toList(listList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, transactionIdSum);

        return new LimitedScanResult<>(lastTime, transactionIdSum);
    }

    @Override
    public LimitedScanResult<List<Dot>> scanScatterData(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        DragArea dragArea = dragAreaQuery.getDragArea();
        Range range = Range.newUncheckedRange(dragArea.getXLow(), dragArea.getXHigh());
        logger.debug("scanTraceScatterData-range:{}", range);

        Scan scan = newFuzzyScanner(applicationName, dragArea, range);

        Predicate<Dot> filter = ElpasedTimeDotPredicate.newDragAreaDotPredicate(dragArea);
        Predicate<Dot> dotStatusPredicate = buildDotStatusFilter(dragAreaQuery);
        if (dotStatusPredicate != null) {
            filter = filter.and(dotStatusPredicate);
        }

        RowMapper<List<Dot>> mapper = new TraceIndexScatterMapper(filter);

        TableName applicationTraceIndexTableName = descriptor.getTableName();
        List<List<Dot>> dotListList = hbaseOperations2.findParallel(applicationTraceIndexTableName, scan, traceIdRowKeyDistributor, limit, mapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);
        List<Dot> dots = ListListUtils.toList(dotListList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, dots);

        return new LimitedScanResult<>(lastTime, dots);
    }

    private Predicate<Dot> buildDotStatusFilter(DragAreaQuery dragAreaQuery) {
        if (dragAreaQuery.getAgentId() != null || dragAreaQuery.getDotStatus() != null) {
            return new DotStatusFilter(dragAreaQuery.getAgentId(), dragAreaQuery.getDotStatus());
        }
        return null;
    }

    static class DotStatusFilter implements Predicate<Dot> {
        // @Nullable
        private final String agentId;
        // @Nullable
        private final Dot.Status dotStatus;

        public DotStatusFilter(String agentId, Dot.Status dotStatus) {
            this.agentId = agentId;
            this.dotStatus = dotStatus;
        }

        @Override
        public boolean test(Dot dot) {
            if (agentId != null) {
                if (!agentId.equals(dot.getAgentId())) {
                    return false;
                }
            }
            if (this.dotStatus != null) {
                Dot.Status status = toDotStatus(this.dotStatus);
                if (!(status == dot.getStatus())) {
                    return false;
                }
            }
            return true;
        }

        private Dot.Status toDotStatus(Dot.Status dotStatus) {
            if (Dot.Status.SUCCESS == dotStatus) {
                return Dot.Status.SUCCESS;
            }
            return Dot.Status.FAILED;
        }
    }

    private Scan newFuzzyScanner(String applicationName, DragArea dragArea, Range range) {
        final Scan scan = createScan(applicationName, range, true);
        if (scatterChartConfig.isEnableFuzzyRowFilter()) {
            Filter filter = newFuzzyFilter(dragArea);
            scan.setFilter(filter);
        }
        return scan;
    }

    private Filter newFuzzyFilter(DragArea dragArea) {
        long yHigh = dragArea.getYHigh();
        long yLow = dragArea.getYLow();
        return this.fuzzyRowKeyBuilder.build(yHigh, yLow);
    }


}
