package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HeatMapServiceImplTest {

    @Mock
    SpanService spanService;
    @Mock
    DragAreaQuery dragAreaQuery;

    private static final String APPLICATION_NAME = "applicationName";
    private static final int LIMIT = 50;
    private static final TransactionId TRANSACTION_ID_1 = new TransactionId("txAgent1", 10, 100);
    private static final TransactionId TRANSACTION_ID_2 = new TransactionId("txAgent2", 20, 200);

    @Test
    public void legacyCompatibilityCheckPassTest() {
        ApplicationTraceIndexDao applicationTraceIndexDao = mock(ApplicationTraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);
        LimitedScanResult<List<DotMetaData>> scanResult = mock(LimitedScanResult.class);

        when(applicationTraceIndexDao.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT)).thenReturn(scanResult);
        when(scanResult.getScanData()).thenReturn(dotMataData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexDao, spanService, traceDao);
        Assertions.assertSame(scanResult, heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckTest() {
        ApplicationTraceIndexDao applicationTraceIndexDao = mock(ApplicationTraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);
        LimitedScanResult<List<DotMetaData>> scanResult = mock(LimitedScanResult.class);

        when(applicationTraceIndexDao.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT)).thenReturn(scanResult);
        when(scanResult.getScanData()).thenReturn(legacyDotMataData());
        when(traceDao.selectSpans(any())).thenReturn(matchingSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexDao, spanService, traceDao);
        heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT);
        Assertions.assertNotSame(scanResult, heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckMoreSpanTest() {
        ApplicationTraceIndexDao applicationTraceIndexDao = mock(ApplicationTraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);
        LimitedScanResult<List<DotMetaData>> scanResult = mock(LimitedScanResult.class);

        when(applicationTraceIndexDao.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT)).thenReturn(scanResult);
        when(scanResult.getScanData()).thenReturn(legacyDotMataData());
        when(traceDao.selectSpans(any())).thenReturn(moreSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexDao, spanService, traceDao);
        heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT);
        Assertions.assertNotSame(scanResult, heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckErrorTest() {
        ApplicationTraceIndexDao applicationTraceIndexDao = mock(ApplicationTraceIndexDao.class);
        TraceDao traceDao = mock(TraceDao.class);
        LimitedScanResult<List<DotMetaData>> scanResult = mock(LimitedScanResult.class);

        when(applicationTraceIndexDao.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT)).thenReturn(scanResult);
        when(scanResult.getScanData()).thenReturn(legacyDotMataData());
        when(traceDao.selectSpans(any())).thenReturn(lessSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexDao, spanService, traceDao);
        Assertions.assertThrows(IllegalStateException.class, () -> heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    private List<DotMetaData> dotMataData() {
        final List<DotMetaData> result = new ArrayList<>(2);
        Dot dot1 = new Dot(TRANSACTION_ID_1, 1, 2, 0, "dotAgentId1");
        Dot dot2 = new Dot(TRANSACTION_ID_2, 3, 4, 0, "dotAgentId2");

        //startTime == 0  false
        result.add(new DotMetaData(dot1, null, null, null, null, 1000, 1));
        result.add(new DotMetaData(dot2, null, null, null, null, 2000, 1));

        return result;
    }

    private List<DotMetaData> legacyDotMataData() {
        final List<DotMetaData> result = new ArrayList<>(2);
        Dot dot1 = new Dot(TRANSACTION_ID_1, 1, 2, 0, "dotAgentId1");
        Dot dot2 = new Dot(TRANSACTION_ID_2, 3, 4, 0, "dotAgentId2");

        //startTime == 0  true
        result.add(new DotMetaData(dot1, null, null, null, null, 1000, 0));
        result.add(new DotMetaData(dot2, null, null, null, null, 2000, 0));

        return result;
    }

    private List<List<SpanBo>> matchingSpanData() {
        final List<List<SpanBo>> result = new ArrayList<>(2);
        List<SpanBo> spanList1 = new ArrayList<>();
        spanList1.add(createSpan(TRANSACTION_ID_1));

        List<SpanBo> spanList2 = new ArrayList<>();
        spanList2.add(createSpan(TRANSACTION_ID_2));

        result.add(spanList1);
        result.add(spanList2);
        return result;
    }

    private List<List<SpanBo>> moreSpanData() {
        final List<List<SpanBo>> result = new ArrayList<>(4);
        List<SpanBo> spanList1 = new ArrayList<>();
        spanList1.add(createSpan(TRANSACTION_ID_1));
        spanList1.add(createSpan(TRANSACTION_ID_1));

        List<SpanBo> spanList2 = new ArrayList<>();
        spanList2.add(createSpan(TRANSACTION_ID_2));
        spanList2.add(createSpan(TRANSACTION_ID_2));

        result.add(spanList1);
        result.add(spanList2);
        return result;
    }

    private List<List<SpanBo>> lessSpanData() {
        final List<List<SpanBo>> result = new ArrayList<>(0);
        List<SpanBo> spanList1 = new ArrayList<>();
        //spanList1.add(createSpan(TX_ID1));

        List<SpanBo> spanList2 = new ArrayList<>();
        //spanList2.add(createSpan(TX_ID2));

        result.add(spanList1);
        result.add(spanList2);
        return result;
    }

    private SpanBo createSpan(TransactionId transactionId) {
        SpanBo newSpan = new SpanBo();
        newSpan.setTransactionId(transactionId);
        return newSpan;
    }
}
