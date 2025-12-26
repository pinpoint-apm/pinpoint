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

package com.navercorp.pinpoint.web.scatter.dao.hbase;

import com.navercorp.pinpoint.common.hbase.DefaultLastRowHandler;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.LastRowHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexFilterBuilder;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexRowKeyUtils;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.dao.LastTimeListExtractor;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.dao.mapper.TraceIndexMetaMapper;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import org.apache.hadoop.hbase.TableName;
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

    private final RowMapper<Boolean> existMapper;
    private final RowMapper<List<Dot>> traceIndexDotMapper;
    private final RowKeyDistributor traceIndexDistributor;

    private int scanCacheSize = 512; // increase default caching since v2 table uses 2 cells per row

    public HbaseTraceIndexDao(ScatterChartProperties scatterChartProperties,
                              HbaseOperations hbaseOperations,
                              TableNameProvider tableNameProvider,
                              @Qualifier("existMapper") RowMapper<Boolean> existMapper,
                              @Qualifier("traceIndexDotMapper") RowMapper<List<Dot>> traceIndexDotMapper,
                              @Qualifier("traceIndexDistributor") RowKeyDistributor traceIndexDistributor) {
        this.scatterChartProperties = Objects.requireNonNull(scatterChartProperties, "scatterChartProperties");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.existMapper = Objects.requireNonNull(existMapper, "existMapper");
        this.traceIndexDotMapper = Objects.requireNonNull(traceIndexDotMapper, "traceIndexDotMapper");
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

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<Boolean> existsList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, 1, existMapper, TRACE_INDEX_NUM_PARTITIONS);
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

        LastRowHandler<List<DotMetaData>> lastRowAccessor = new DefaultLastRowHandler<>();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<DotMetaData>> traceIndexList = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, limit, new TraceIndexMetaMapper(null, null, null), lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);

        List<DotMetaData> transactionIdSum = ListListUtils.toList(traceIndexList);

        boolean overflow = LastTimeListExtractor.isOverflow(transactionIdSum, limit);
        final long lastTime = LastTimeListExtractor.getLastTime(overflow, lastRowAccessor, value -> value.getDot().getAcceptedTime(), range.getFrom());

        return new LimitedScanResult<>(lastTime, transactionIdSum);
    }

    @Override
    public LimitedScanResult<List<Dot>> scanTraceScatterData(int serviceUid, String applicationName, int serviceTypeCode, Range range, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatterDataMadeOfDotGroup");
        LastRowHandler<List<Dot>> lastRowAccessor = new DefaultLastRowHandler<>();

        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, scanBackward, -1);

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<Dot>> listList = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, this.traceIndexDotMapper, TRACE_INDEX_NUM_PARTITIONS);
        List<Dot> dots = ListListUtils.toList(listList);

        boolean overflow = LastTimeListExtractor.isOverflow(dots, limit);
        final long lastTime = LastTimeListExtractor.getLastTime(overflow, lastRowAccessor, Dot::getAcceptedTime, range.getFrom());
        return new LimitedScanResult<>(lastTime, dots);
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> scanScatterDataV2(int serviceUid, String applicationName, int serviceTypeCode,
                                                                  DragAreaQuery dragAreaQuery, String rpcRegex, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");
        DragArea dragArea = dragAreaQuery.getDragArea();
        Range range = Range.unchecked(dragArea.getXLow(), dragArea.getXHigh());
        logger.debug("scanTraceScatterData-range:{}", range);

        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range, true, -1);
        scan.addFamily(META.getName());
        setHbaseFilter(scan, applicationName, dragAreaQuery, rpcRegex);

        RowMapper<List<DotMetaData>> mapper = createDotMetaMapper(dragAreaQuery);
        LastRowHandler<List<DotMetaData>> lastRowAccessor = new DefaultLastRowHandler<>();

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<DotMetaData>> scanResult = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, mapper, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);
        List<DotMetaData> dotMetaDataList = ListListUtils.toList(scanResult);

        boolean overflow = LastTimeListExtractor.isOverflow(dotMetaDataList, limit);
        final long lastTime = LastTimeListExtractor.getLastTime(overflow, lastRowAccessor, value -> value.getDot().getAcceptedTime(), range.getFrom());
        return new LimitedScanResult<>(lastTime, dotMetaDataList);
    }

    private Scan createScan(int serviceUid, String applicationName, int serviceTypeCode, Range range, boolean scanBackward, int limit) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        applyLimitForScan(scan, limit);

        byte[] traceIndexStartKey = TraceIndexRowKeyUtils.createScanRowKey(serviceUid, applicationName, serviceTypeCode, range.getFrom());
        byte[] traceIndexEndKey = TraceIndexRowKeyUtils.createScanRowKey(serviceUid, applicationName, serviceTypeCode, range.getTo());

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
        return scan;
    }

    private void applyLimitForScan(Scan scan, int limit) {
        if (limit == 1) {
            scan.setOneRowLimit();
        } else if (limit > 1) {
            scan.setLimit(limit);
        }
    }

    private void setHbaseFilter(Scan scan, String applicationName, DragAreaQuery dragAreaQuery, String rpcRegex) {
        TraceIndexFilterBuilder filterBuilder = new TraceIndexFilterBuilder(applicationName);
        filterBuilder.setElapsedMinMax(new LongPair(dragAreaQuery.getDragArea().getYLow(), dragAreaQuery.getDragArea().getYHigh()));
        filterBuilder.setAgentId(dragAreaQuery.getAgentId());
        filterBuilder.setRpcRegex(rpcRegex);
        if (dragAreaQuery.getDotStatus() != null) {
            filterBuilder.setSuccess(dragAreaQuery.getDotStatus() == Dot.Status.SUCCESS);
        }
        scan.setFilter(filterBuilder.build(scatterChartProperties.isEnableFuzzyRowFilter(), scatterChartProperties.isEnableIndexValueFilter()));
    }

    private RowMapper<List<DotMetaData>> createDotMetaMapper(DragAreaQuery dragAreaQuery) {
        if (scatterChartProperties.isEnableIndexValueFilter()) {
            return new TraceIndexMetaMapper(null, null, null);
        } else {
            Predicate<Integer> elapsedTimePredicate = buildElapsedTimePredicate(dragAreaQuery);
            Predicate<Integer> exceptionCodePredicate = buildExceptionCodePredicate(dragAreaQuery);
            Predicate<String> agentIdPredicate = buildAgentIdPredicate(dragAreaQuery);
            return new TraceIndexMetaMapper(elapsedTimePredicate, exceptionCodePredicate, agentIdPredicate);
        }
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

}
