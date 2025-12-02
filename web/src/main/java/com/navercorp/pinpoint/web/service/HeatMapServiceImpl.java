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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.trace.dao.TraceDao;
import com.navercorp.pinpoint.web.trace.service.SpanService;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.SpanHint;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class HeatMapServiceImpl implements HeatMapService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Predicate<DotMetaData> legacyTablePredicate = new Predicate<>() {
        @Override
        public boolean test(DotMetaData dotMetaData) {
            return dotMetaData.getStartTime() == 0;
        }
    };

    private final ApplicationTraceIndexDao applicationTraceIndexDao;
    private final TraceIndexDao traceIndexDao;

    private final TraceDao traceDao;
    private final SpanService spanService;

    public HeatMapServiceImpl(ApplicationTraceIndexDao applicationTraceIndexDao,
                              TraceIndexDao traceIndexDao,
                              SpanService spanService,
                              TraceDao traceDao) {
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "applicationTraceIndexV2Dao");
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
    }


    @Override
    public LimitedScanResult<List<DotMetaData>> dragScatterDataV2(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");


        LimitedScanResult<List<DotMetaData>> scanResult = applicationTraceIndexDao.scanScatterDataV2(applicationName, dragAreaQuery, limit);
        List<DotMetaData> scanData = scanResult.scanData();
        logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scanResult);

        if (hasOldVersion(scanData)) {
            return filterCompatibility(applicationName, scanResult);
        }
        return scanResult;
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> dragScatterDataV3(int serviceUid, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");


        LimitedScanResult<List<DotMetaData>> scanResult = traceIndexDao.scanScatterDataV2(serviceUid, applicationName, serviceTypeCode, dragAreaQuery, limit);
        List<DotMetaData> scanData = scanResult.scanData();
        logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scanResult);

        if (hasOldVersion(scanData)) {
            return filterCompatibility(applicationName, scanResult);
        }
        return scanResult;
    }

    private boolean hasOldVersion(List<DotMetaData> scanData) {
        Optional<DotMetaData> oldVersion = scanData.stream()
                .filter(legacyTablePredicate)
                .findAny();
        return oldVersion.isPresent();
    }

    private LimitedScanResult<List<DotMetaData>> filterCompatibility(String applicationName, LimitedScanResult<List<DotMetaData>> scanResult) {
        List<DotMetaData> scanData = scanResult.scanData();

        List<Dot> dots = filterLegacyTablePredicate(scanData, legacyTablePredicate);

        List<GetTraceInfo> query = buildQuery(applicationName, dots);
        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(query);

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

    private @NonNull List<Dot> filterLegacyTablePredicate(List<DotMetaData> scanData, Predicate<DotMetaData> legacyTablePredicate) {
        List<Dot> dots = new ArrayList<>(scanData.size());
        for (DotMetaData scanDatum : scanData) {
            if (legacyTablePredicate.test(scanDatum)) {
                Dot dot = scanDatum.getDot();
                dots.add(dot);
            }
        }
        return dots;
    }

    private List<GetTraceInfo> buildQuery(String applicationName, List<Dot> dots) {
        if (CollectionUtils.isEmpty(dots)) {
            return Collections.emptyList();
        }
        List<GetTraceInfo> list = new ArrayList<>(dots.size());
        for (Dot dot : dots) {
            GetTraceInfo getTraceInfo = dotToGetTraceInfo(applicationName, dot);
            list.add(getTraceInfo);
        }
        return list;
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
