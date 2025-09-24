/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyBuilder;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexMetaScatterMapper;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.ElpasedTimeDotPredicate;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.hadoop.hbase.Cell;
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

// HbaseApplicationTraceIndexDao V2
@Repository
public class HbaseTraceIndexDao implements TraceIndexDao {

    private static final int TRACE_INDEX_NUM_PARTITIONS = 8;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily INDEX = HbaseTables.TRACE_INDEX;
    private static final HbaseColumnFamily META = HbaseTables.TRACE_INDEX_META;

    private final ScatterChartProperties scatterChartProperties;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final FuzzyRowKeyBuilder fuzzyRowKeyBuilder = FuzzyRowKeyBuilder.createBuilderV2();

    private final RowMapper<List<TransactionId>> traceIndexMapperV2;
    private final RowMapper<List<Dot>> traceIndexScatterMapperV2;

    private final RowKeyDistributor traceIndexDistributor;

    private int scanCacheSize = 256;

    public HbaseTraceIndexDao(ScatterChartProperties scatterChartProperties,
                              HbaseOperations hbaseOperations,
                              TableNameProvider tableNameProvider,
                              @Qualifier("transactionIdMapper") RowMapper<List<TransactionId>> traceIndexMapperV2,
                              @Qualifier("traceIndexScatterMapper") RowMapper<List<Dot>> traceIndexScatterMapperV2,
                              @Qualifier("traceIndexDistributor") RowKeyDistributor traceIndexDistributor) {
        this.scatterChartProperties = Objects.requireNonNull(scatterChartProperties, "scatterChartProperties");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.traceIndexMapperV2 = Objects.requireNonNull(traceIndexMapperV2, "traceIndexMapperV2");
        this.traceIndexScatterMapperV2 = Objects.requireNonNull(traceIndexScatterMapperV2, "traceIndexScatterMapperV2");
        this.traceIndexDistributor = Objects.requireNonNull(traceIndexDistributor, "traceIndexDistributor");
    }

    public void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }

    @Override
    public boolean hasTraceIndex(int serviceUid, String applicationName, int serviceTypeCode, Range range, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        logger.debug("hasTraceIndex {}", range);
        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, backwardDirection, 1);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<TransactionId>> traceIndexList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, 1, traceIndexMapperV2, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = ListListUtils.toList(traceIndexList);
        return !transactionIdSum.isEmpty();
    }

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(int serviceUid, final String applicationName, int serviceTypeCode, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex {}", range);
        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, scanBackward, -1);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<TransactionId>> traceIndexList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, limit, traceIndexMapperV2, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);

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

    private Scan createScan(int serviceUid, String applicationName, int serviceTypeCode, Range range, boolean scanBackward, int limit) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        applyLimitForScan(scan, limit);

        byte[] traceIndexStartKey = TraceIndexRowUtils.encodeRowKey(serviceUid, applicationName, serviceTypeCode, range.getFrom());
        byte[] traceIndexEndKey = TraceIndexRowUtils.encodeRowKey(serviceUid, applicationName, serviceTypeCode, range.getTo());

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
        scan.setId(INDEX.getTable().getName() + "scan");

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
    public LimitedScanResult<List<Dot>> scanTraceScatterData(int serviceUid, String applicationName, int serviceTypeCode, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatterDataMadeOfDotGroup");
        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, scanBackward, -1);

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<Dot>> listList = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, this.traceIndexScatterMapperV2, TRACE_INDEX_NUM_PARTITIONS);
        List<Dot> dots = ListListUtils.toList(listList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, dots);
        return new LimitedScanResult<>(lastTime, dots);
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
    public LimitedScanResult<List<DotMetaData>> scanScatterDataV2(int serviceUid, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        Predicate<Dot> filter = buildDotPredicate(dragAreaQuery);
        RowMapper<List<DotMetaData>> mapper = new TraceIndexMetaScatterMapper(INDEX, META, filter);

        return scanScatterData0(serviceUid, applicationName, serviceTypeCode, dragAreaQuery, limit, true, mapper);
    }

    private <R> LimitedScanResult<List<R>> scanScatterData0(int serviceUid, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit,
                                                            boolean metadataScan, RowMapper<List<R>> mapper) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        DragArea dragArea = dragAreaQuery.getDragArea();
        Range range = Range.unchecked(dragArea.getXLow(), dragArea.getXHigh());
        logger.debug("scanTraceScatterData-range:{}", range);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();

        Scan scan = newFuzzyScanner(serviceUid, applicationName, serviceTypeCode, dragArea, range);
        if (metadataScan) {
            scan.addFamily(META.getName());
        }

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<R>> dotListList = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, mapper, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);
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
                if (!(this.dotStatus == dot.getStatus())) {
                    return false;
                }
            }
            return true;
        }
    }

    private Scan newFuzzyScanner(int serviceUid, String applicationName, int serviceTypeCode, DragArea dragArea, Range range) {
        final Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, true, -1);
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

    private class LastRowAccessor implements LimitEventHandler {
        private Long lastRowTimestamp = -1L;
        private TransactionId lastTransactionId = null;
        private int lastTransactionElapsed = -1;

        @Override
        public void handleLastResult(Result lastResult) {
            if (lastResult == null) {
                return;
            }
            final Cell last = CellUtils.lastCell(lastResult.rawCells(), INDEX.getName());
            this.lastRowTimestamp = TraceIndexRowUtils.extractAcceptTime(last.getRowArray(), last.getRowOffset());

            byte[] qualifier = last.getQualifierArray();
            int qualifierOffset = last.getQualifierOffset();
            int qualifierLength = last.getQualifierLength();
            this.lastTransactionId = SpanUtils.parseVarTransactionId(qualifier, qualifierOffset, qualifierLength);
            this.lastTransactionElapsed = ByteArrayUtils.bytesToInt(qualifier, qualifierOffset);

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
}
