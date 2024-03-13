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
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyBuilder;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexMetaScatterMapper;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper;
import com.navercorp.pinpoint.web.mapper.TransactionIdMapper;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.ElpasedTimeDotPredicate;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.ApplicationTraceIndexTrace INDEX = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE;
    private static final HbaseColumnFamily.ApplicationTraceIndexTrace META = HbaseColumnFamily.APPLICATION_TRACE_INDEX_META;

    private final ScatterChartProperties scatterChartProperties;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final FuzzyRowKeyBuilder fuzzyRowKeyBuilder = new FuzzyRowKeyBuilder();

    private final RowMapper<List<TransactionId>> traceIndexMapper;

    private final RowMapper<List<Dot>> traceIndexScatterMapper;

    private final AbstractRowKeyDistributor traceIdRowKeyDistributor;

    private int scanCacheSize = 256;

    private final ApplicationNameRowKeyEncoder rowKeyEncoder = new ApplicationNameRowKeyEncoder();

    public HbaseApplicationTraceIndexDao(ScatterChartProperties scatterChartProperties,
                                         HbaseOperations hbaseOperations,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("transactionIdMapper") RowMapper<List<TransactionId>> traceIndexMapper,
                                         @Qualifier("traceIndexScatterMapper") RowMapper<List<Dot>> traceIndexScatterMapper,
                                         @Qualifier("applicationTraceIndexDistributor") AbstractRowKeyDistributor traceIdRowKeyDistributor) {
        this.scatterChartProperties = Objects.requireNonNull(scatterChartProperties, "scatterChartProperties");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.traceIndexMapper = Objects.requireNonNull(traceIndexMapper, "traceIndexMapper");
        this.traceIndexScatterMapper = Objects.requireNonNull(traceIndexScatterMapper, "traceIndexScatterMapper");
        this.traceIdRowKeyDistributor = Objects.requireNonNull(traceIdRowKeyDistributor, "traceIdRowKeyDistributor");
    }

    public void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }

    @Override
    public boolean hasTraceIndex(String applicationName, Range range, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        logger.debug("hasTraceIndex {}", range);
        Scan scan = createScan(applicationName, range, backwardDirection, 1);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<TransactionId>> traceIndexList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIdRowKeyDistributor, 1, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = ListListUtils.toList(traceIndexList);
        return !transactionIdSum.isEmpty();
    }

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(final String applicationName, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex {}", range);
        Scan scan = createScan(applicationName, range, scanBackward, -1);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<TransactionId>> traceIndexList = hbaseOperations.findParallel(applicationTraceIndexTableName,
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

            final Cell last = CellUtils.lastCell(lastResult.rawCells(), HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE.getName());
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


    private Scan createScan(String applicationName, Range range, boolean scanBackward, int limit) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        applyLimitForScan(scan, limit);

        byte[] traceIndexStartKey = rowKeyEncoder.encodeRowKey(applicationName, range.getFrom());
        byte[] traceIndexEndKey = rowKeyEncoder.encodeRowKey(applicationName, range.getTo());

        if (scanBackward) {
            // start key is replaced by end key because key has been reversed
            scan.withStartRow(traceIndexEndKey);
            scan.withStopRow(traceIndexStartKey);
        } else {
            scan.setReversed(true);
            scan.withStartRow(traceIndexStartKey);
            scan.withStopRow(traceIndexEndKey);
        }

        scan.addFamily(INDEX.getName());
        scan.setId("ApplicationTraceIndexScan");

        // toString() method of Scan converts a message to json format so it is slow for the first time.
        logger.trace("create scan:{}", scan);
        return scan;
    }

    private void applyLimitForScan(Scan scan, int limit) {
        if (limit == 1) {
            scan.setOneRowLimit();
        } else if (limit > 1) {
            scan.setLimit(limit);
        }
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

        Scan scan = createScan(applicationName, range, scanBackward, -1);

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<Dot>> listList = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIdRowKeyDistributor, limit, this.traceIndexScatterMapper, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);
        List<Dot> dots = ListListUtils.toList(listList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, dots);
        return new LimitedScanResult<>(lastTime, dots);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, DragArea dragArea, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragArea, "dragArea");

        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        final Range range = Range.unchecked(dragArea.getXLow(), dragArea.getXHigh());
        logger.debug("scanTraceIndex range:{}", range);
        final Scan scan = newFuzzyScanner(applicationName, dragArea, range);


        // TODO
//        Predicate<Dot> filter = ElpasedTimeDotPredicate.newDragAreaDotPredicate(dragArea);

        final TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<TransactionId>> listList = this.hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = ListListUtils.toList(listList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, transactionIdSum);

        return new LimitedScanResult<>(lastTime, transactionIdSum);
    }

    @Deprecated
    @Override
    public LimitedScanResult<List<Dot>> scanScatterData(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        Predicate<Dot> filter = buildDotPredicate(dragAreaQuery);

        RowMapper<List<Dot>> mapper = new TraceIndexScatterMapper(filter);

        return scanScatterData0(applicationName, dragAreaQuery, limit, false, mapper);
    }

    private Predicate<Dot> buildDotPredicate(DragAreaQuery dragAreaQuery) {
        DragArea dragArea = dragAreaQuery.getDragArea();
        Predicate<Dot> filter = ElpasedTimeDotPredicate.newDragAreaDotPredicate(dragArea);
        Predicate<Dot> dotStatusPredicate = buildDotStatusFilter(dragAreaQuery);
        if (dotStatusPredicate != null) {
            filter = filter.and(dotStatusPredicate);
        }
        return filter;
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> scanScatterDataV2(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        Predicate<Dot> filter = buildDotPredicate(dragAreaQuery);

        RowMapper<List<DotMetaData>> mapper = new TraceIndexMetaScatterMapper(filter);

        return scanScatterData0(applicationName, dragAreaQuery, limit, true, mapper);
    }

    private <R> LimitedScanResult<List<R>> scanScatterData0(String applicationName, DragAreaQuery dragAreaQuery, int limit,
                                                         boolean metadataScan, RowMapper<List<R>> mapper) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        DragArea dragArea = dragAreaQuery.getDragArea();
        Range range = Range.unchecked(dragArea.getXLow(), dragArea.getXHigh());
        logger.debug("scanTraceScatterData-range:{}", range);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        Scan scan = newFuzzyScanner(applicationName, dragArea, range);
        if (metadataScan) {
            scan.addFamily(META.getName());
        }

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<R>> dotListList = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIdRowKeyDistributor, limit, mapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);
        List<R> dots = ListListUtils.toList(dotListList);

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
                return this.dotStatus == dot.getStatus();
            }
            return true;
        }
    }

    private Scan newFuzzyScanner(String applicationName, DragArea dragArea, Range range) {
        final Scan scan = createScan(applicationName, range, true, -1);
        if (scatterChartProperties.isEnableFuzzyRowFilter()) {
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
