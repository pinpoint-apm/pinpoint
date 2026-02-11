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
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.dao.LastTimeListExtractor;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.dao.mapper.TraceIndexDotMapper;
import com.navercorp.pinpoint.web.scatter.dao.mapper.TraceIndexMetaMapper;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
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

    private final RowKeyDistributor traceIndexDistributor;

    private int scanCacheSize = 512; // increase default caching since v2 table uses 2 cells per row

    public HbaseTraceIndexDao(ScatterChartProperties scatterChartProperties,
                              HbaseOperations hbaseOperations,
                              TableNameProvider tableNameProvider,
                              @Qualifier("traceIndexDistributor") RowKeyDistributor traceIndexDistributor) {
        this.scatterChartProperties = Objects.requireNonNull(scatterChartProperties, "scatterChartProperties");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.traceIndexDistributor = Objects.requireNonNull(traceIndexDistributor, "traceIndexDistributor");
    }

    public void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> scanTraceIndex(int serviceUid, final String applicationName, int serviceTypeCode, Range range, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceIndex {}", range);
        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range);
        scan.addFamily(META.getName()); //for txId

        RowMapper<List<DotMetaData>> dotMetaMapper = createDotMetaMapper(applicationName);
        LastRowHandler<List<DotMetaData>> lastRowAccessor = new DefaultLastRowHandler<>();
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<DotMetaData>> scanResult = hbaseOperations.findParallel(applicationTraceIndexTableName,
                scan, traceIndexDistributor, limit, dotMetaMapper, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);
        List<DotMetaData> transactionIdSum = ListListUtils.toList(scanResult);

        boolean overflow = LastTimeListExtractor.isOverflow(transactionIdSum, limit);
        final long lastTime = LastTimeListExtractor.getLastTime(overflow, lastRowAccessor, value -> value.getDot().getAcceptedTime(), range.getFrom());

        return new LimitedScanResult<>(lastTime, transactionIdSum);
    }

    @Override
    public LimitedScanResult<List<Dot>> scanTraceScatterData(int serviceUid, String applicationName, int serviceTypeCode, Range range, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatterDataMadeOfDotGroup");
        LastRowHandler<List<Dot>> lastRowAccessor = new DefaultLastRowHandler<>();

        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range);

        RowMapper<List<Dot>> dotMapper = new TraceIndexDotMapper(TraceIndexRowKeyUtils.createApplicationNamePredicate(applicationName));
        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<Dot>> scanResult = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, dotMapper, TRACE_INDEX_NUM_PARTITIONS);
        List<Dot> dots = ListListUtils.toList(scanResult);

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

        Scan scan = createScan(serviceUid, applicationName, serviceTypeCode, range);
        setHbaseFilter(scan, dragAreaQuery, rpcRegex);
        scan.addFamily(META.getName());

        RowMapper<List<DotMetaData>> mapper = createDotMetaMapper(applicationName, dragAreaQuery);
        LastRowHandler<List<DotMetaData>> lastRowAccessor = new DefaultLastRowHandler<>();

        TableName applicationTraceIndexTableName = tableNameProvider.getTableName(INDEX.getTable());
        List<List<DotMetaData>> scanResult = hbaseOperations.findParallel(applicationTraceIndexTableName, scan,
                traceIndexDistributor, limit, mapper, lastRowAccessor, TRACE_INDEX_NUM_PARTITIONS);
        List<DotMetaData> dotMetaDataList = ListListUtils.toList(scanResult);

        boolean overflow = LastTimeListExtractor.isOverflow(dotMetaDataList, limit);
        final long lastTime = LastTimeListExtractor.getLastTime(overflow, lastRowAccessor, value -> value.getDot().getAcceptedTime(), range.getFrom());
        return new LimitedScanResult<>(lastTime, dotMetaDataList);
    }

    private Scan createScan(int serviceUid, String applicationName, int serviceTypeCode, Range range) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);

        byte[] traceIndexStartKey = TraceIndexRowKeyUtils.createScanRowKey(serviceUid, applicationName, serviceTypeCode, range.getFrom());
        byte[] traceIndexEndKey = TraceIndexRowKeyUtils.createScanRowKey(serviceUid, applicationName, serviceTypeCode, range.getTo());
        // start key is replaced by end key because row timestamp has been reversed
        scan.withStartRow(traceIndexEndKey);
        scan.withStopRow(traceIndexStartKey);

        scan.addColumn(INDEX.getName(), INDEX.getName());
        scan.setId(INDEX.getTable().getName() + "scan");
        return scan;
    }

    private void setHbaseFilter(Scan scan, DragAreaQuery dragAreaQuery, String rpcRegex) {
        TraceIndexFilterBuilder filterBuilder = new TraceIndexFilterBuilder();
        filterBuilder.setElapsedMin(dragAreaQuery.getDragArea().getYLow());
        filterBuilder.setElapsedMax(dragAreaQuery.getDragArea().getYHigh());
        filterBuilder.setAgentId(dragAreaQuery.getAgentId());
        filterBuilder.setRpcRegex(rpcRegex);
        if (dragAreaQuery.getDotStatus() != null) {
            filterBuilder.setSuccess(dragAreaQuery.getDotStatus() == Dot.Status.SUCCESS);
        }
        FilterList filter = filterBuilder.build(scatterChartProperties.isEnableHbaseRowFilter(), scatterChartProperties.isEnableHbaseValueFilter());
        if (!filter.getFilters().isEmpty()) {
            scan.setFilter(filter);
        }
    }

    private TraceIndexMetaMapper createDotMetaMapper(String applicationName) {
        Predicate<byte[]> applicationNamePredicate = TraceIndexRowKeyUtils.createApplicationNamePredicate(applicationName);
        return new TraceIndexMetaMapper(applicationNamePredicate, null, null, null);
    }

    private TraceIndexMetaMapper createDotMetaMapper(String applicationName, DragAreaQuery dragAreaQuery) {
        Predicate<byte[]> applicationNamePredicate = TraceIndexRowKeyUtils.createApplicationNamePredicate(applicationName);
        Predicate<String> agentIdPredicate = buildAgentIdPredicate(dragAreaQuery);
        Predicate<Integer> exceptionCodePredicate = buildExceptionCodePredicate(dragAreaQuery);
        if (!scatterChartProperties.isEnableHbaseValueFilter()) {
            return new TraceIndexMetaMapper(applicationNamePredicate, exceptionCodePredicate, agentIdPredicate, buildElapsedTimePredicate(dragAreaQuery));
        }
        return new TraceIndexMetaMapper(applicationNamePredicate, exceptionCodePredicate, agentIdPredicate, null);
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
