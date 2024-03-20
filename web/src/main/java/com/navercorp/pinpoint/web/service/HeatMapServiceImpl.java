package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMap;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMapBuilder;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.SpanHint;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class HeatMapServiceImpl implements HeatMapService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationTraceIndexService applicationTraceIndexService;

    private final TraceDao traceDao;
    private final SpanService spanService;

    public HeatMapServiceImpl(ApplicationTraceIndexService applicationTraceIndexService,
                              SpanService spanService,
                              TraceDao traceDao) {
        this.applicationTraceIndexService = Objects.requireNonNull(applicationTraceIndexService, "applicationTraceIndexService");
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
    }

    @Override
    public LimitedScanResult<List<SpanBo>> dragScatterData(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");


        LimitedScanResult<List<Dot>> scanResult = applicationTraceIndexService.scanScatterData(applicationName, dragAreaQuery, limit);

        logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scanResult);
//        boolean requestComplete = scatterData.getDotSize() < limit;
        List<GetTraceInfo> query = buildQuery(applicationName, scanResult.scanData());

        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(query);

        List<SpanBo> spanList = ListListUtils.toList(selectedSpans, selectedSpans.size());
        spanService.populateAgentName(spanList);

        logger.debug("dragScatterArea span:{}", spanList.size());
        return new LimitedScanResult<>(scanResult.limitedTime(), spanList);
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> dragScatterDataV2(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");


        LimitedScanResult<List<DotMetaData>> scanResult = applicationTraceIndexService.scanScatterDataV2(applicationName, dragAreaQuery, limit);
        scanResult = legacyCompatibilityCheck(applicationName, scanResult);

        logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scanResult);
        return scanResult;
    }

    private LimitedScanResult<List<DotMetaData>> legacyCompatibilityCheck(String applicationName, LimitedScanResult<List<DotMetaData>> scanResult) {
        Predicate<DotMetaData> legacyTablePredicate = legacyTablePredicate();

        List<DotMetaData> scanData = scanResult.scanData();
        Optional<DotMetaData> oldVersion = scanData.stream()
                .filter(legacyTablePredicate)
                .findAny();
        if (!oldVersion.isPresent()) {
            return scanResult;
        }
        //
        List<Dot> dots = scanData.stream()
                .filter(legacyTablePredicate)
                .map(DotMetaData::getDot)
                .collect(Collectors.toList());

        List<GetTraceInfo> query = buildQuery(applicationName, dots);
        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(query);
        //List<SpanBo> spanList = ListListUtils.toList(selectedSpans, selectedSpans.size());
        List<SpanBo> spanList = pickFirst(selectedSpans);
        spanService.populateAgentName(spanList);

        if (dots.size() != spanList.size()) {
            throw new IllegalStateException("Legacy compatibility error, dots=" + dots.size() + " spanList:" + spanList);
        }

        Iterator<SpanBo> spanIter = spanList.iterator();

        List<DotMetaData> result = new ArrayList<>(scanData.size());
        for (DotMetaData dotMetaData : scanData) {
            if (legacyTablePredicate.test(dotMetaData)) {
                if (!spanIter.hasNext()) {
                    throw new IllegalStateException("Legacy compatibility error");
                }
                SpanBo span = spanIter.next();
                DotMetaData.Builder builder = new DotMetaData.Builder();
                builder.setDot(dotMetaData.getDot());
                builder.setAgentName(span.getAgentName());
                builder.setEndpoint(span.getEndPoint());
                builder.setRemoteAddr(span.getRemoteAddr());
                builder.setStartTime(span.getStartTime());
                builder.setSpanId(span.getSpanId());
                builder.setRpc(span.getRpc());
                result.add(builder.build());
            } else {
                result.add(dotMetaData);
            }
        }
        return new LimitedScanResult<>(scanResult.limitedTime(), result);
    }

    private Predicate<DotMetaData> legacyTablePredicate() {
        return new Predicate<DotMetaData>() {
            @Override
            public boolean test(DotMetaData dotMetaData) {
                return dotMetaData.getStartTime() == 0;
            }
        };
    }

    @Override
    public LimitedScanResult<HeatMap> getHeatMap(String applicationName, Range range, long maxY, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");


        LimitedScanResult<List<Dot>> scanResult = applicationTraceIndexService.scanTraceScatterData(applicationName, range, limit, true);

        final int slotSize = 100;
        HeatMapBuilder builder = HeatMapBuilder.newBuilder(range.getFrom(), range.getTo(), slotSize, 0, maxY, slotSize);
        for (Dot dot : scanResult.scanData()) {
            final boolean success = dot.getExceptionCode() == Dot.EXCEPTION_NONE;
            builder.addDataPoint(dot.getAcceptedTime(), dot.getElapsedTime(), success);
        }
        HeatMap heatMap = builder.build();
        logger.debug("getHeatMap applicationName:{} dots:{} heatMap:{}", applicationName, scanResult.scanData().size(), heatMap);

        return new LimitedScanResult<>(scanResult.limitedTime(), heatMap);
    }

    private List<GetTraceInfo> buildQuery(String applicationName, List<Dot> dots) {
        if (CollectionUtils.isEmpty(dots)) {
            return Collections.emptyList();
        }
        return dots.stream()
                .map(dot -> dotToGetTraceInfo(applicationName, dot))
                .collect(Collectors.toList());
    }

    private GetTraceInfo dotToGetTraceInfo(String applicationName, Dot dot) {
        TransactionId transactionId = dot.getTransactionId();

        SpanHint spanHint = new SpanHint(dot.getAcceptedTime(),
                dot.getElapsedTime(), applicationName, dot.getAgentId());

        return new GetTraceInfo(transactionId, spanHint);
    }

    private List<SpanBo> pickFirst(List<List<SpanBo>> spanLists) {
        List<SpanBo> result = new ArrayList<>(spanLists.size());
        for (List<SpanBo> candidates : spanLists) {
            final SpanBo first = org.springframework.util.CollectionUtils.firstElement(candidates);
            if (first != null) {
                result.add(first);

                if (candidates.size() > 1 && logger.isDebugEnabled()) {
                    logger.debug("heuristically avoid Legacy compatibility error, spanCandidate:{}", candidates);
                }
            }
        }
        return result;
    }
}
