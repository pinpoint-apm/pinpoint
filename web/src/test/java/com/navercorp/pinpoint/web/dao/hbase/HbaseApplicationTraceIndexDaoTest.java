/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.*;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class HbaseApplicationTraceIndexDaoTest {

    @Mock
    private HbaseOperations2 hbaseOperations2;

    @Mock
    private RowMapper<List<TransactionId>> traceIndexMapper;

    @Mock
    private RowMapper<List<Dot>> traceIndexScatterMapper;

    @Mock
    private AbstractRowKeyDistributor traceIdRowKeyDistributor;

    @Spy
    private final TableNameProvider tableNameProvider = new TableNameProvider() {

        @Override
        public TableName getTableName(HbaseTable hBaseTable) {
            return getTableName(hBaseTable.getName());
        }

        @Override
        public TableName getTableName(String tableName) {
            return TableName.valueOf(tableName);
        }

        @Override
        public boolean hasDefaultNameSpace() {
            return true;
        }
    };

    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TableDescriptorConfig tableDescriptorConfig = new TableDescriptorConfig(tableNameProvider);
        TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> descriptor = tableDescriptorConfig.getApplicationTraceIndexTrace();
        this.applicationTraceIndexDao = new HbaseApplicationTraceIndexDao(hbaseOperations2, descriptor, traceIndexMapper, traceIndexScatterMapper, traceIdRowKeyDistributor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scanTraceIndexExceptionTest() {
        this.applicationTraceIndexDao.scanTraceIndex("app", Range.newRange(0, 10), -20, false);
    }

    @Test
    public void scanTraceIndexTest() {
        final List<List<TransactionId>> scannedList = createTraceIndexList();
        when(this.hbaseOperations2.findParallel(any(TableName.class), any(Scan.class), any(AbstractRowKeyDistributor.class),
                anyInt(), any(RowMapper.class), any(LimitEventHandler.class), anyInt())).thenReturn(scannedList);
        LimitedScanResult<List<TransactionId>> result =
                this.applicationTraceIndexDao.scanTraceIndex("app", Range.newRange(1000L, 5000L), 20, false);
        Assert.assertEquals(1000L, result.getLimitedTime());
        Assert.assertEquals(ListListUtils.toList(scannedList), result.getScanData());

        // using last row accessor
        result = this.applicationTraceIndexDao.scanTraceIndex("app", Range.newRange(1000L, 5000L), 5, true);
        Assert.assertEquals(-1L, result.getLimitedTime());
        Assert.assertEquals(ListListUtils.toList(scannedList), result.getScanData());

    }

    @Test(expected = IllegalArgumentException.class)
    public void scanTraceScatterDataExceptionTest() {
        this.applicationTraceIndexDao.scanTraceScatterData("app", Range.newRange(1000L, 5000L), -10, false);
    }

    @Test
    public void scanTraceScatterDataEmptyTest() {
        List<ScatterData> scannedList = new ArrayList<>();
        when(this.hbaseOperations2.findParallel(any(TableName.class), any(Scan.class), any(AbstractRowKeyDistributor.class),
                anyInt(), any(RowMapper.class), any(LimitEventHandler.class), anyInt())).thenReturn(scannedList);
        Range range = Range.newRange(1000L, 5000L);
        LimitedScanResult<List<Dot>> scanResult
                = this.applicationTraceIndexDao.scanTraceScatterData("app", range,10, false);
        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), 1, 5);
        scanResult.getScanData().forEach(builder::addDot);
        ScatterData result = builder.build();

        Assert.assertEquals(1000L, result.getFrom());
        Assert.assertEquals(5000L, result.getTo());
        Assert.assertEquals(0, result.getScatterData().size());
    }

    @Test
    public void scanTraceScatterDataTest() {
        List<List<Dot>> scatterDotList = createScatterDotList();
        when(this.hbaseOperations2.findParallel(any(TableName.class), any(Scan.class), any(AbstractRowKeyDistributor.class),
                anyInt(), any(RowMapper.class), anyInt())).thenReturn(scatterDotList);
        Range range = Range.newRange(1000L, 5000L);
        LimitedScanResult<List<Dot>> scanResult
                = this.applicationTraceIndexDao.scanTraceScatterData("app", range,10, false);
        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), 1, 5);
        scanResult.getScanData().forEach(builder::addDot);
        ScatterData result = builder.build();
        Assert.assertEquals(1000L, result.getFrom());
        Assert.assertEquals(5000L, result.getTo());
        Assert.assertEquals(2000L, result.getOldestAcceptedTime());
        Assert.assertEquals(3000L, result.getLatestAcceptedTime());
    }

    private List<List<Dot>> createScatterDotList() {
        List<List<Dot>> ret = new ArrayList<>();
        TransactionId transactionId = new TransactionId("A", 1, 1);
        addDot(ret, new Dot(transactionId, 2000L, 1000, 0, "a1"));
        addDot(ret, new Dot(transactionId, 3000L, 5000, 0, "a2"));
        addDot(ret, new Dot(transactionId, 2400L, 3000, 0, "a3"));
        return ret;
    }

    private void addDot(List<List<Dot>> list, Dot dot) {
        list.add(Collections.singletonList(dot));
    }


    private List<List<TransactionId>> createTraceIndexList() {
        List<List<TransactionId>> ret = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ret.add(new ArrayList<>());
            for (int j = 0; j < 2; j++) {
                ret.get(i).add(j, new TransactionId("agentId" + i, 1000L * (i + 1), j));
            }
        }
        return ret;
    }

}
