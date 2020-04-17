package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMap;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMapBuilder;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class HeatMapServiceImpl implements HeatMapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final TraceDao traceDao;

    public HeatMapServiceImpl(ApplicationTraceIndexDao applicationTraceIndexDao, @Qualifier("hbaseTraceDaoFactory") TraceDao traceDao) {
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
    }

    @Override
    public LimitedScanResult<List<SpanBo>> dragScatterData(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");


        LimitedScanResult<List<Dot>> scanResult = applicationTraceIndexDao.scanScatterData(applicationName, dragAreaQuery, limit);

        logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scanResult);
//        boolean requestComplete = scatterData.getDotSize() < limit;
        List<GetTraceInfo> query = buildQuery(scanResult.getScanData());

        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(query);

        List<SpanBo> spanList = ListListUtils.toList(selectedSpans, selectedSpans.size());

        logger.debug("dragScatterArea span:{}", spanList.size());
        return new LimitedScanResult<>(scanResult.getLimitedTime(), spanList);
    }

    @Override
    public LimitedScanResult<HeatMap> getHeatMap(String applicationName, Range range, long maxY, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");


        LimitedScanResult<List<Dot>> scanResult = applicationTraceIndexDao.scanTraceScatterData(applicationName, range, limit, true);

        final int slotSize = 100;
        HeatMapBuilder builder = HeatMapBuilder.newBuilder(range.getFrom(), range.getTo(), slotSize, 0, maxY, slotSize);
        for (Dot dot : scanResult.getScanData()) {
            final boolean success = dot.getExceptionCode() == Dot.EXCEPTION_NONE;
            builder.addDataPoint(dot.getAcceptedTime(), dot.getElapsedTime(), success);
        }
        HeatMap heatMap = builder.build();
        logger.debug("getHeatMap applicationName:{} dots:{} heatMap:{}", applicationName, scanResult.getScanData().size(), heatMap);

        return new LimitedScanResult<>(scanResult.getLimitedTime(), heatMap);
    }

    private List<GetTraceInfo> buildQuery(List<Dot> dots) {
        if (CollectionUtils.isEmpty(dots)) {
            return Collections.emptyList();
        }
        List<GetTraceInfo> result = new ArrayList<>();
        for (Dot dot : dots) {
            TransactionId transactionId = dot.getTransactionId();

            final GetTraceInfo getTraceInfo = new GetTraceInfo(transactionId);
            result.add(getTraceInfo);
        }
        return result;
    }
}
