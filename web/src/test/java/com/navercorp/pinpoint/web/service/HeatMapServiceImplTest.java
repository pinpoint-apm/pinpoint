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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import com.navercorp.pinpoint.web.trace.dao.TraceDao;
import com.navercorp.pinpoint.web.trace.service.SpanService;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.common.server.uid.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HeatMapServiceImplTest {

    @Mock
    SpanService spanService;
    @Mock
    DragAreaQuery dragAreaQuery;

    private static final Service SERVICE = Service.DEFAULT;
    private static final String APPLICATION_NAME = "applicationName";
    private static final int SERVICE_TYPE_CODE = ServiceType.TEST.getCode();
    private static final int LIMIT = 50;
    private static final long WINDOW_MILLIS = 300_000L;
    private static final long BUDGET_MILLIS = 3_000L;
    private static final ServerTraceId TRANSACTION_ID_1 = new PinpointServerTraceId("txAgent1", 10, 100);
    private static final ServerTraceId TRANSACTION_ID_2 = new PinpointServerTraceId("txAgent2", 20, 200);

    @Test
    public void legacyCompatibilityCheckPassTest() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, dotMataData());
        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), eq(dragAreaQuery), isNull(), eq(LIMIT)))
                .thenReturn(scanResult);

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, false, WINDOW_MILLIS, BUDGET_MILLIS);
        Assertions.assertSame(scanResult, heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckTest() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, legacyDotMataData());
        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), eq(dragAreaQuery), isNull(), eq(LIMIT)))
                .thenReturn(scanResult);
        when(traceDao.selectSpans(any())).thenReturn(matchingSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, false, WINDOW_MILLIS, BUDGET_MILLIS);
        heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, dragAreaQuery, LIMIT);
        Assertions.assertNotSame(scanResult, heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckMoreSpanTest() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, legacyDotMataData());
        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), eq(dragAreaQuery), isNull(), eq(LIMIT)))
                .thenReturn(scanResult);
        when(traceDao.selectSpans(any())).thenReturn(moreSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, false, WINDOW_MILLIS, BUDGET_MILLIS);
        heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, dragAreaQuery, LIMIT);
        Assertions.assertNotSame(scanResult, heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckErrorTest() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, legacyDotMataData());
        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), eq(dragAreaQuery), isNull(), eq(LIMIT)))
                .thenReturn(scanResult);
        when(traceDao.selectSpans(any())).thenReturn(lessSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, false, WINDOW_MILLIS, BUDGET_MILLIS);
        Assertions.assertThrows(IllegalStateException.class, () -> heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, dragAreaQuery, LIMIT));
    }


    @Test
    public void chunkedScanStopsAtFirstWindowWhenLimitIsMet() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        // 1 hour range, 5 minute windows -> 12 windows if it had to walk the whole range
        long to = 3_600_000L;
        DragAreaQuery query = new DragAreaQuery(DragArea.normalize(0, to, 0, Integer.MAX_VALUE));

        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), eq(LIMIT)))
                .thenReturn(new LimitedScanResult<>(99, dotMataData(LIMIT)));

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, true, WINDOW_MILLIS, BUDGET_MILLIS);
        LimitedScanResult<List<DotMetaData>> result =
                heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, query, LIMIT);

        Assertions.assertEquals(LIMIT, result.scanData().size());
        Assertions.assertEquals(99, result.limitedTime());
        Assertions.assertFalse(result.truncated());
        verify(traceIndexDao, times(1))
                .scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), eq(LIMIT));
    }

    @Test
    public void chunkedScanWalksEveryWindowAndReportsCompleteWhenNothingMatches() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        long to = 3_600_000L; // 12 windows of 5 minutes
        DragAreaQuery query = new DragAreaQuery(DragArea.normalize(0, to, 0, Integer.MAX_VALUE));

        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), anyInt()))
                .thenReturn(new LimitedScanResult<>(0, List.of()));

        // generous budget so the walk is not cut short
        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, true, WINDOW_MILLIS, 60_000L);
        LimitedScanResult<List<DotMetaData>> result =
                heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, query, LIMIT);

        Assertions.assertTrue(result.scanData().isEmpty());
        Assertions.assertFalse(result.truncated(), "a completed walk must not be flagged truncated");
        Assertions.assertEquals(0, result.limitedTime(), "a completed walk resumes at the range start");
        verify(traceIndexDao, times(12))
                .scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), anyInt());
    }

    @Test
    public void chunkedScanStopsOnBudgetAndFlagsTruncated() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        long to = 3_600_000L;
        DragAreaQuery query = new DragAreaQuery(DragArea.normalize(0, to, 0, Integer.MAX_VALUE));

        // every window is empty and slow, so the budget runs out before the range does
        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), anyInt()))
                .thenAnswer(invocation -> {
                    Thread.sleep(20);
                    return new LimitedScanResult<>(0, List.of());
                });

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, true, WINDOW_MILLIS, 10L);
        LimitedScanResult<List<DotMetaData>> result =
                heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, query, LIMIT);

        Assertions.assertTrue(result.scanData().isEmpty());
        Assertions.assertTrue(result.truncated(), "budget exhausted mid-range must be flagged truncated");
        Assertions.assertEquals(to - WINDOW_MILLIS + 1, result.limitedTime(),
                "the window read (windowFrom, to], so the resume cursor must still cover windowFrom");
        verify(traceIndexDao, times(1))
                .scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), anyInt());
    }

    @Test
    public void chunkedScanWindowsLeaveNoGapAtTheBoundary() {
        TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);

        long to = 3_600_000L; // 12 windows of 5 minutes
        DragAreaQuery query = new DragAreaQuery(DragArea.normalize(0, to, 0, Integer.MAX_VALUE));

        when(traceIndexDao.scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), any(), isNull(), anyInt()))
                .thenReturn(new LimitedScanResult<>(0, List.of()));

        HeatMapService heatMapService = new HeatMapServiceImpl(traceIndexDao, spanService, traceDao, true, WINDOW_MILLIS, 60_000L);
        heatMapService.dragTraceIndex(SERVICE, APPLICATION_NAME, SERVICE_TYPE_CODE, query, LIMIT);

        ArgumentCaptor<DragAreaQuery> captor = ArgumentCaptor.forClass(DragAreaQuery.class);
        verify(traceIndexDao, times(12))
                .scanScatterDataV2(eq(SERVICE), eq(APPLICATION_NAME), eq(SERVICE_TYPE_CODE), captor.capture(), isNull(), anyInt());

        List<DragAreaQuery> windows = captor.getAllValues();
        Assertions.assertEquals(to, windows.get(0).getDragArea().getXHigh(), "the first window must start at the requested range end");
        for (int i = 1; i < windows.size(); i++) {
            // the scan stop row is exclusive, so a window covers (xLow, xHigh]; the next window must
            // end exactly on the previous xLow, otherwise that millisecond is never read
            Assertions.assertEquals(windows.get(i - 1).getDragArea().getXLow(), windows.get(i).getDragArea().getXHigh(),
                    "window " + i + " must continue from the previous window start");
        }
        Assertions.assertEquals(0, windows.get(windows.size() - 1).getDragArea().getXLow(),
                "the last window must reach the requested range start");
    }

    private List<DotMetaData> dotMataData(int count) {
        List<DotMetaData> list = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DotMetaData.Builder builder = new DotMetaData.Builder();
            builder.setDot(new Dot(TRANSACTION_ID_1, i, 2, 0, "dotAgentId" + i));
            builder.setStartTime(i + 1);
            list.add(builder.build());
        }
        return list;
    }

    private List<DotMetaData> dotMataData() {
        Dot dot1 = new Dot(TRANSACTION_ID_1, 1, 2, 0, "dotAgentId1");
        Dot dot2 = new Dot(TRANSACTION_ID_2, 3, 4, 0, "dotAgentId2");

        return List.of(
                new DotMetaData(dot1, null, null, null, null, 1000, 1),
                new DotMetaData(dot2, null, null, null, null, 2000, 1)
        );
    }

    private List<DotMetaData> legacyDotMataData() {
        Dot dot1 = new Dot(TRANSACTION_ID_1, 1, 2, 0, "dotAgentId1");
        Dot dot2 = new Dot(TRANSACTION_ID_2, 3, 4, 0, "dotAgentId2");

        //startTime == 0  true
        return List.of(
                new DotMetaData(dot1, null, null, null, null, 1000, 0),
                new DotMetaData(dot2, null, null, null, null, 2000, 0)
        );
    }

    private List<List<SpanBo>> matchingSpanData() {
        List<SpanBo> spanList1 = List.of(createSpan(TRANSACTION_ID_1));
        List<SpanBo> spanList2 = List.of(createSpan(TRANSACTION_ID_2));

        return List.of(spanList1, spanList2);
    }

    private List<List<SpanBo>> moreSpanData() {
        List<SpanBo> spanList1 = List.of(
                createSpan(TRANSACTION_ID_1),
                createSpan(TRANSACTION_ID_1));
        List<SpanBo> spanList2 = List.of(
                createSpan(TRANSACTION_ID_2),
                createSpan(TRANSACTION_ID_2));

        return List.of(spanList1, spanList2);
    }

    private List<List<SpanBo>> lessSpanData() {
        return List.of(List.of(), List.of());
    }

    private SpanBo createSpan(ServerTraceId transactionId) {
        SpanBo newSpan = new SpanBo();
        newSpan.setTransactionId(transactionId);
        return newSpan;
    }
}