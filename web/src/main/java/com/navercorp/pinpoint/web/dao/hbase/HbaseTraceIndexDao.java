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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexFilterBuilder;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexRowKey;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexMetaMapper;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
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
    private static final int TRACE_INDEX_NUM_PARTITIONS = HbaseTableConstants.TRACE_INDEX_DISTRIBUTOR_MOD;
    private static final HbaseColumnFamily INDEX = HbaseTables.TRACE_INDEX;
    private static final HbaseColumnFamily META = HbaseTables.TRACE_INDEX_META;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ScatterChartProperties scatterChartProperties;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<Dot>> traceIndexScatterMapperV2;
    private final RowKeyDistributor traceIndexDistributor;

    private int scanCacheSize = 256;

    public HbaseTraceIndexDao(ScatterChartProperties scatterChartProperties,
                              HbaseOperations hbaseOperations,
                              TableNameProvider tableNameProvider,
                              @Qualifier("traceIndexDotMapper") RowMapper<List<Dot>> traceIndexScatterMapperV2,
                              @Qualifier("traceIndexDistributor") RowKeyDistributor traceIndexDistributor) {
        this.scatterChartProperties = Objects.requireNonNull(scatterChartProperties, "scatterChartProperties");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
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

        RowMapper<Boolean> existsMapper = (result, rowNum) -> {
            if (result.isEmpty()) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        };
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<Boolean> existsList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, 1, existsMapper, TRACE_INDEX_NUM_PARTITIONS);
        return existsList.contains(Boolean.TRUE);
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> scanTraceIndex(int serviceUid, final String applicationName, int serviceTypeCode, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex {}", range);
        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, scanBackward, -1);
        scan.addFamily(META.getName()); //for txId

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<DotMetaData>> traceIndexList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, limit, new TraceIndexMetaMapper(null, null, null), lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);

        List<DotMetaData> transactionIdSum = ListListUtils.toList(traceIndexList);
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

        byte[] traceIndexStartKey = TraceIndexRowKey.createScanRowKey(serviceUid, applicationName, serviceTypeCode, range.getFrom());
        byte[] traceIndexEndKey = TraceIndexRowKey.createScanRowKey(serviceUid, applicationName, serviceTypeCode, range.getTo());

        if (scanBackward) {
            // start key is replaced by end key because key has been reversed
            scan.withStartRow(traceIndexEndKey);
            scan.withStopRow(traceIndexStartKey);
        } else {
            scan.setReversed(true);
            scan.withStartRow(traceIndexStartKey);
            scan.withStopRow(traceIndexEndKey);
        }

        scan.addColumn(INDEX.getName(), INDEX.getName());
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

    @Override
    public LimitedScanResult<List<DotMetaData>> scanScatterDataV2(int serviceUid, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        Predicate<Integer> elapsedTimePredicate = buildElapsedTimePredicate(dragAreaQuery);
        RowMapper<List<DotMetaData>> mapper;
        if (scatterChartProperties.isEnableIndexValueFilter()) {
            mapper = new TraceIndexMetaMapper(elapsedTimePredicate, null, null);
        } else {
            Predicate<Integer> exceptionCodePredicate = buildExceptionCodePredicate(dragAreaQuery);
            Predicate<String> agentIdPredicate = buildAgentIdPredicate(dragAreaQuery);
            mapper = new TraceIndexMetaMapper(elapsedTimePredicate, exceptionCodePredicate, agentIdPredicate);
        }
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

        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, true, -1);
        if (metadataScan) {
            scan.addFamily(META.getName());
        }

        //set filter
        TraceIndexFilterBuilder filterBuilder = new TraceIndexFilterBuilder(applicationName);
        filterBuilder.setElapsedMinMax(new LongPair(dragArea.getYLow(), dragArea.getYHigh()));
        filterBuilder.setAgentId(dragAreaQuery.getAgentId());
        if (dragAreaQuery.getDotStatus() != null) {
            filterBuilder.setSuccess(dragAreaQuery.getDotStatus() == Dot.Status.SUCCESS);
        }
        scan.setFilter(filterBuilder.build(scatterChartProperties.isEnableFuzzyRowFilter(), scatterChartProperties.isEnableIndexValueFilter()));

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<R>> dotListList = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, mapper, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);
        List<R> dots = ListListUtils.toList(dotListList);

        final long lastTime = getLastTime(range, limit, lastRowAccessor, dots);
        return new LimitedScanResult<>(lastTime, dots);
    }

    private Predicate<Integer> buildElapsedTimePredicate(DragAreaQuery dragAreaQuery) {
        DragArea dragArea = dragAreaQuery.getDragArea();
        final long high = dragArea.getYHigh();
        final long low = dragArea.getYLow();
        return elapsedTime -> elapsedTime <= high && elapsedTime >= low;
    }

    private Predicate<String> buildAgentIdPredicate(DragAreaQuery dragAreaQuery) {
        String agentId = dragAreaQuery.getAgentId();
        if (agentId == null) {
            return null;
        }
        return agentId::equals;
    }

    private Predicate<Integer> buildExceptionCodePredicate(DragAreaQuery dragAreaQuery) {
        Dot.Status status = dragAreaQuery.getDotStatus();
        if (status == null) {
            return null;
        }
        return status == Dot.Status.SUCCESS
                ? exceptionCode -> exceptionCode == Dot.EXCEPTION_NONE
                : exceptionCode -> exceptionCode != Dot.EXCEPTION_NONE;
    }

    private class LastRowAccessor implements LimitEventHandler {
        private Long lastRowTimestamp = -1L;

        @Override
        public void handleLastResult(Result lastResult) {
            if (lastResult == null) {
                return;
            }
            final Cell last = CellUtils.lastCell(lastResult.rawCells(), INDEX.getName());
            this.lastRowTimestamp = TraceIndexRowUtils.extractAcceptTime(last.getRowArray(), last.getRowOffset());
            if (logger.isDebugEnabled()) {
                final Cell lastMeta = CellUtils.lastCell(lastResult.rawCells(), META.getName());
                if (lastMeta != null) {
                    long spanId = ByteArrayUtils.bytesToLong(lastMeta.getRowArray(), lastMeta.getRowOffset() + lastMeta.getRowLength() - ByteArrayUtils.LONG_BYTE_LENGTH);
                    Buffer buffer = new OffsetFixedBuffer(lastMeta.getValueArray(), lastMeta.getValueOffset(), lastMeta.getValueLength());
                    buffer.readInt(); // elapsed
                    buffer.readByte(); // txId version
                    TransactionId lastTransactionId = SpanUtils.readTransactionIdV1(buffer);
                    logger.debug("lastRowTimestamp={}, txId={}, spanId={}", DateTimeFormatUtils.format(lastRowTimestamp), lastTransactionId, spanId);
                } else {
                    logger.debug("lastRowTimestamp={}", DateTimeFormatUtils.format(lastRowTimestamp));
                }
            }
        }

        private Long getLastRowTimestamp() {
            return lastRowTimestamp;
        }
    }
}
