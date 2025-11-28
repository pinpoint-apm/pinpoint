/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTableNameProvider;
import com.navercorp.pinpoint.common.hbase.LimitEventHandler;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdV1;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HbaseTraceIndexDaoTest {

    private static final int serviceUid = ServiceUid.DEFAULT_SERVICE_UID_CODE;
    private static final DotMetaData testDotMetaData = new DotMetaData(new Dot(TransactionIdV1.EMPTY_ID, 10, 10, 0, "testAgentId"),
            "testAgentName", "remoteAddr", "rpc", "endpoint", -1, 0);

    @Mock
    private HbaseOperations hbaseOperations;

    @Mock
    private RowKeyDistributor traceIndexRowKeyDistributor;

    @Spy
    private final TableNameProvider tableNameProvider = new HbaseTableNameProvider("default");

    private TraceIndexDao traceIndexDao;

    @AutoClose
    @SuppressWarnings("unused")
    private AutoCloseable openMocks;

    @BeforeEach
    public void beforeEach() {
        openMocks = MockitoAnnotations.openMocks(this);
        ScatterChartProperties scatterChartProperties = new ScatterChartProperties();

        RowMapper<List<Dot>> traceScatterMapper = new TraceIndexScatterMapper();
        this.traceIndexDao = new HbaseTraceIndexDao(scatterChartProperties, hbaseOperations, tableNameProvider, traceScatterMapper, traceIndexRowKeyDistributor);
    }

    @Test
    public void scanTraceIndexExceptionTest() {
        assertThrows(IllegalArgumentException.class, () -> this.traceIndexDao.scanTraceIndex(serviceUid, "app", ServiceType.TEST_STAND_ALONE.getCode(), Range.between(0, 10), -20, false));
    }

    @Test
    public void scanTraceIndexTest() {
        final List<List<DotMetaData>> scannedList = List.of(Collections.nCopies(10, testDotMetaData));
        when(this.hbaseOperations.findParallel(any(TableName.class), any(Scan.class), any(RowKeyDistributor.class),
                anyInt(), any(RowMapper.class), any(LimitEventHandler.class), anyInt())).thenReturn(scannedList);
        LimitedScanResult<List<DotMetaData>> result =
                this.traceIndexDao.scanTraceIndex(serviceUid, "app", ServiceType.TEST_STAND_ALONE.getCode(), Range.between(1000L, 5000L), 20, false);
        Assertions.assertEquals(1000L, result.limitedTime());

        // using last row accessor
        result = this.traceIndexDao.scanTraceIndex(serviceUid, "app", ServiceType.TEST_STAND_ALONE.getCode(), Range.between(1000L, 5000L), 5, true);
        Assertions.assertEquals(-1L, result.limitedTime());

    }

    @Test
    public void scanTraceScatterDataExceptionTest() {
        assertThrows(IllegalArgumentException.class, () -> this.traceIndexDao.scanTraceScatterData(serviceUid, "app", ServiceType.TEST_STAND_ALONE.getCode(), Range.between(1000L, 5000L), -10, false));
    }

    @Test
    public void scanTraceScatterDataEmptyTest() {

        when(this.hbaseOperations.findParallel(any(TableName.class), any(Scan.class), any(RowKeyDistributor.class),
                anyInt(), any(RowMapper.class), any(LimitEventHandler.class), anyInt())).thenReturn(List.of());
        Range range = Range.between(1000L, 5000L);
        LimitedScanResult<List<Dot>> scanResult
                = this.traceIndexDao.scanTraceScatterData(serviceUid, "app", ServiceType.TEST_STAND_ALONE.getCode(), range, 10, false);
        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), 1, 5);
        scanResult.scanData().forEach(builder::addDot);
        ScatterData result = builder.build();

        Assertions.assertEquals(1000L, result.getFrom());
        Assertions.assertEquals(5000L, result.getTo());
        assertThat(result.getScatterData()).isEmpty();
    }

    @Test
    public void scanTraceScatterDataTest() {
        List<List<Dot>> scatterDotList = createScatterDotList();
        when(this.hbaseOperations.findParallel(any(TableName.class), any(Scan.class), any(RowKeyDistributor.class),
                anyInt(), any(RowMapper.class), anyInt())).thenReturn(scatterDotList);
        Range range = Range.between(1000L, 5000L);
        LimitedScanResult<List<Dot>> scanResult
                = this.traceIndexDao.scanTraceScatterData(serviceUid, "app", ServiceType.TEST_STAND_ALONE.getCode(), range, 10, false);
        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), 1, 5);
        scanResult.scanData().forEach(builder::addDot);
        ScatterData result = builder.build();
        Assertions.assertEquals(1000L, result.getFrom());
        Assertions.assertEquals(5000L, result.getTo());
        Assertions.assertEquals(2000L, result.getOldestAcceptedTime());
        Assertions.assertEquals(3000L, result.getLatestAcceptedTime());
    }

    private List<List<Dot>> createScatterDotList() {
        List<List<Dot>> ret = new ArrayList<>();
        TransactionId transactionId = TransactionId.of("A", 1, 1);
        addDot(ret, new Dot(transactionId, 2000L, 1000, 0, "a1"));
        addDot(ret, new Dot(transactionId, 3000L, 5000, 0, "a2"));
        addDot(ret, new Dot(transactionId, 2400L, 3000, 0, "a3"));
        return ret;
    }

    private void addDot(List<List<Dot>> list, Dot dot) {
        list.add(List.of(dot));
    }

    @Test
    public void dotStatusFilterTest() {
        Predicate<Dot> dotStatusFilter = new HbaseApplicationTraceIndexDao.DotStatusFilter("agent", Dot.Status.SUCCESS);
        Dot dot = mock(Dot.class);
        when(dot.getAgentId()).thenReturn("agent");
        when(dot.getStatus()).thenReturn(Dot.Status.SUCCESS);

        Assertions.assertTrue(dotStatusFilter.test(dot));
    }

    @Test
    public void dotStatusFilterTest_fail1() {
        Predicate<Dot> dotStatusFilter = new HbaseApplicationTraceIndexDao.DotStatusFilter("agent", Dot.Status.SUCCESS);
        Dot dot = mock(Dot.class);
        when(dot.getAgentId()).thenReturn("agent");
        when(dot.getStatus()).thenReturn(Dot.Status.FAILED);

        Assertions.assertFalse(dotStatusFilter.test(dot));

    }

    @Test
    public void dotStatusFilterTest_fail2() {
        Predicate<Dot> dotStatusFilter = new HbaseApplicationTraceIndexDao.DotStatusFilter("agent", Dot.Status.SUCCESS);
        Dot dot = mock(Dot.class);
        when(dot.getAgentId()).thenReturn("xxx");
        when(dot.getStatus()).thenReturn(Dot.Status.SUCCESS);

        Assertions.assertFalse(dotStatusFilter.test(dot));

    }

}
