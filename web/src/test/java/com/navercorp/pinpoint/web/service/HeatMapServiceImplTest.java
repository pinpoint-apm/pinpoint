package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
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
        ApplicationTraceIndexService applicationTraceIndexService = mock(ApplicationTraceIndexService.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, dotMataData());
        when(applicationTraceIndexService.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT))
                .thenReturn(scanResult);

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexService, spanService, traceDao);
        Assertions.assertSame(scanResult, heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckTest() {
        ApplicationTraceIndexService applicationTraceIndexService = mock(ApplicationTraceIndexService.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, legacyDotMataData());
        when(applicationTraceIndexService.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT))
                .thenReturn(scanResult);
        when(traceDao.selectSpans(any())).thenReturn(matchingSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexService, spanService, traceDao);
        heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT);
        Assertions.assertNotSame(scanResult, heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckMoreSpanTest() {
        ApplicationTraceIndexService applicationTraceIndexService = mock(ApplicationTraceIndexService.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, legacyDotMataData());
        when(applicationTraceIndexService.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT))
                .thenReturn(scanResult);
        when(traceDao.selectSpans(any())).thenReturn(moreSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexService, spanService, traceDao);
        heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT);
        Assertions.assertNotSame(scanResult, heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
    }

    @Test
    public void legacyCompatibilityCheckErrorTest() {
        ApplicationTraceIndexService applicationTraceIndexService = mock(ApplicationTraceIndexService.class);
        TraceDao traceDao = mock(TraceDao.class);

        LimitedScanResult<List<DotMetaData>> scanResult = new LimitedScanResult<>(1, legacyDotMataData());
        when(applicationTraceIndexService.scanScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT)).thenReturn(scanResult);
        when(traceDao.selectSpans(any())).thenReturn(lessSpanData());

        HeatMapService heatMapService = new HeatMapServiceImpl(applicationTraceIndexService, spanService, traceDao);
        Assertions.assertThrows(IllegalStateException.class, () -> heatMapService.dragScatterDataV2(APPLICATION_NAME, dragAreaQuery, LIMIT));
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

    private SpanBo createSpan(TransactionId transactionId) {
        SpanBo newSpan = new SpanBo();
        newSpan.setTransactionId(transactionId);
        return newSpan;
    }
}
