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
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper3;
import com.navercorp.pinpoint.web.mapper.TransactionIdMapper;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

    private static final int APPLICATION_TRACE_INDEX_NUM_PARTITIONS = 32;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseOperations2;

    private final RowMapper<List<TransactionId>> traceIndexMapper;

    private final RowMapper<List<Dot>> traceIndexScatterMapper;

    private final AbstractRowKeyDistributor traceIdRowKeyDistributor;

    private int scanCacheSize = 256;

    public HbaseApplicationTraceIndexDao(HbaseOperations2 hbaseOperations2,
                                         TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> descriptor,
                                         @Qualifier("transactionIdMapper") RowMapper<List<TransactionId>> traceIndexMapper,
                                         @Qualifier("traceIndexScatterMapper") RowMapper<List<Dot>> traceIndexScatterMapper,
                                         @Qualifier("applicationTraceIndexDistributor") AbstractRowKeyDistributor traceIdRowKeyDistributor) {
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
        logger.debug("scanTraceIndex");
        Scan scan = createScan(applicationName, range, scanBackward);

        LastRowAccessor lastRowAccessor = new LastRowAccessor();
        TableName applicationTraceIndexTableName = descriptor.getTableName();
        List<List<TransactionId>> traceIndexList = hbaseOperations2.findParallel(applicationTraceIndexTableName,
                scan, traceIdRowKeyDistributor, limit, traceIndexMapper, lastRowAccessor, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        List<TransactionId> transactionIdSum = ListListUtils.toList(traceIndexList);
        final long lastTime = getLastTime(range, limit, lastRowAccessor, transactionIdSum);

        return new LimitedScanResult<>(lastTime, transactionIdSum);
    }

    private long getLastTime(Range range, int limit, LastRowAccessor lastRowAccessor, List<TransactionId> transactionIdSum) {
        if (transactionIdSum.size() >= limit) {
            Long lastRowTimestamp = lastRowAccessor.getLastRowTimestamp();
            if (logger.isDebugEnabled()) {
                logger.debug("lastRowTimestamp lastTime:{}", DateUtils.longToDateStr(lastRowTimestamp));
            }
            return lastRowTimestamp;
        } else {
            long from = range.getFrom();
            if (logger.isDebugEnabled()) {
                logger.debug("scanner start lastTime:{}", DateUtils.longToDateStr(from));
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


    private Scan createScan(String applicationName, Range range, boolean scanBackward) {
        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);

        byte[] bApplicationName = Bytes.toBytes(applicationName);
        byte[] traceIndexStartKey = SpanUtils.getApplicationTraceIndexRowKey(bApplicationName, range.getFrom());
        byte[] traceIndexEndKey = SpanUtils.getApplicationTraceIndexRowKey(bApplicationName, range.getTo());

        if (scanBackward) {
            // start key is replaced by end key because key has been reversed
            scan.setStartRow(traceIndexEndKey);
            scan.setStopRow(traceIndexStartKey);
        } else {
            scan.setReversed(true);
            scan.setStartRow(traceIndexStartKey);
            scan.setStopRow(traceIndexEndKey);
        }

        scan.addFamily(descriptor.getColumnFamilyName());
        scan.setId("ApplicationTraceIndexScan");

        // toString() method of Scan converts a message to json format so it is slow for the first time.
        logger.trace("create scan:{}", scan);
        return scan;
    }


    @Override
    public ScatterData scanTraceScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean scanBackward) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        logger.debug("scanTraceScatterDataMadeOfDotGroup");
        Scan scan = createScan(applicationName, range, scanBackward);

        RowMapper<ScatterData> mapper = new TraceIndexScatterMapper3(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);

        TableName applicationTraceIndexTableName = descriptor.getTableName();
        List<ScatterData> dotGroupList = hbaseOperations2.findParallel(applicationTraceIndexTableName, scan, traceIdRowKeyDistributor, limit, mapper, APPLICATION_TRACE_INDEX_NUM_PARTITIONS);

        if (CollectionUtils.isEmpty(dotGroupList)) {
            ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
            return builder.build();
        } else {
            ScatterData firstScatterData = dotGroupList.get(0);
            ScatterDataBuilder builder = new ScatterDataBuilder(firstScatterData.getFrom(), firstScatterData.getTo(), xGroupUnit, yGroupUnit);
            for (ScatterData scatterData : dotGroupList) {
                builder.merge(scatterData);
            }
            return builder.build();
        }
    }



}
