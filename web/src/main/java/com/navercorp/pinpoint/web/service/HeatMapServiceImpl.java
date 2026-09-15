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
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import com.navercorp.pinpoint.web.trace.dao.TraceDao;
import com.navercorp.pinpoint.web.trace.service.SpanService;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.web.vo.SpanHint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@org.springframework.stereotype.Service
public class HeatMapServiceImpl implements HeatMapService {

    private static final int LEGACY_TX_ID_LOG_LIMIT = 10;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Predicate<DotMetaData> legacyTablePredicate = new Predicate<>() {
        @Override
        public boolean test(DotMetaData dotMetaData) {
            return dotMetaData.getStartTime() == 0;
        }
    };

    private final TraceIndexDao traceIndexDao;

    private final TraceDao traceDao;
    private final SpanService spanService;

    private final boolean chunkedScanEnabled;
    private final long chunkWindowMillis;
    private final long chunkBudgetMillis;

    public HeatMapServiceImpl(TraceIndexDao traceIndexDao,
                              SpanService spanService,
                              TraceDao traceDao,
                              @Value("${web.scatter.chunked-scan.enabled:true}") boolean chunkedScanEnabled,
                              @Value("${web.scatter.chunked-scan.window-millis:300000}") long chunkWindowMillis,
                              @Value("${web.scatter.chunked-scan.budget-millis:3000}") long chunkBudgetMillis) {
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "traceIndexDao");
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        if (chunkWindowMillis <= 0) {
            throw new IllegalArgumentException("non-positive chunkWindowMillis:" + chunkWindowMillis);
        }
        this.chunkedScanEnabled = chunkedScanEnabled;
        this.chunkWindowMillis = chunkWindowMillis;
        this.chunkBudgetMillis = chunkBudgetMillis;
    }


    @Override
    public LimitedScanResult<List<DotMetaData>> dragTraceIndex(Service service, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(dragAreaQuery, "dragAreaQuery");

        LimitedScanResult<List<DotMetaData>> scanResult = scan(service, applicationName, serviceTypeCode, dragAreaQuery, limit);
        List<DotMetaData> scanData = scanResult.scanData();
        logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, scanResult);

        if (hasOldVersion(scanData)) {
            return filterCompatibility(applicationName, serviceTypeCode, scanResult);
        }
        return scanResult;
    }

    private LimitedScanResult<List<DotMetaData>> scan(Service service, String applicationName, int serviceTypeCode,
                                                      DragAreaQuery dragAreaQuery, int limit) {
        if (!chunkedScanEnabled) {
            return traceIndexDao.scanScatterDataV2(service, applicationName, serviceTypeCode, dragAreaQuery, null, limit);
        }
        final DragArea dragArea = dragAreaQuery.getDragArea();
        final long from = dragArea.getXLow();
        final long to = dragArea.getXHigh();

        if ((to - from) <= chunkWindowMillis) {
            return traceIndexDao.scanScatterDataV2(service, applicationName, serviceTypeCode, dragAreaQuery, null, limit);
        }
        return chunkedScan(service, applicationName, serviceTypeCode, dragAreaQuery, limit, from, to);
    }

    private LimitedScanResult<List<DotMetaData>> chunkedScan(Service service, String applicationName, int serviceTypeCode,
                                                             DragAreaQuery dragAreaQuery, int limit, long from, long to) {
        final long deadline = System.currentTimeMillis() + chunkBudgetMillis;

        List<DotMetaData> collected = new ArrayList<>(limit);
        long cursor = to;
        long limitedTime = from;
        boolean truncated = false;
        int windows = 0;

        while (cursor > from) {
            final long windowFrom = Math.max(from, cursor - chunkWindowMillis);
            final DragAreaQuery windowQuery = narrowTo(dragAreaQuery, windowFrom, cursor);
            final int remaining = limit - collected.size();

            LimitedScanResult<List<DotMetaData>> window =
                    traceIndexDao.scanScatterDataV2(service, applicationName, serviceTypeCode, windowQuery, null, remaining);
            collected.addAll(window.scanData());
            windows++;

            if (collected.size() >= limit) {
                limitedTime = window.limitedTime();
                break;
            }

            cursor = windowFrom;

            if (cursor > from && System.currentTimeMillis() >= deadline) {
                truncated = true;
                limitedTime = cursor + 1;
                break;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("chunkedScan applicationName:{} windows:{} dots:{} truncated:{}",
                    applicationName, windows, collected.size(), truncated);
        }
        return new LimitedScanResult<>(limitedTime, collected, truncated);
    }

    private static DragAreaQuery narrowTo(DragAreaQuery origin, long xLow, long xHigh) {
        DragArea dragArea = origin.getDragArea();
        DragArea narrowed = DragArea.normalize(xLow, xHigh, dragArea.getYLow(), dragArea.getYHigh());
        return new DragAreaQuery(narrowed, origin.getAgentId(), origin.getDotStatus());
    }

    private boolean hasOldVersion(List<DotMetaData> scanData) {
        Optional<DotMetaData> oldVersion = scanData.stream()
                .filter(legacyTablePredicate)
                .findAny();
        return oldVersion.isPresent();
    }

    private LimitedScanResult<List<DotMetaData>> filterCompatibility(String applicationName, int serviceTypeCode, LimitedScanResult<List<DotMetaData>> scanResult) {
        List<DotMetaData> scanData = scanResult.scanData();

        List<Dot> dots = filterLegacyTablePredicate(scanData, legacyTablePredicate);

        if (logger.isWarnEnabled()) {
            List<ServerTraceId> legacyTxIds = dots.stream()
                    .map(Dot::getTransactionId)
                    .limit(LEGACY_TX_ID_LOG_LIMIT)
                    .toList();
            logger.warn("Legacy trace-index rows detected (startTime==0); running compatibility backfill. " +
                            "applicationName:{} serviceTypeCode:{} dots:{} legacyTxIds:{}",
                    applicationName, serviceTypeCode, dots.size(), legacyTxIds);
        }

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
                builder.setStartTime(span.getStartTimeMillis());
                builder.setSpanId(span.getSpanId());
                builder.setRpc(span.getRpc());
                result.add(builder.build());
            } else {
                result.add(dotMetaData);
            }
        }
        return new LimitedScanResult<>(scanResult.limitedTime(), result, scanResult.truncated());
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
        ServerTraceId transactionId = dot.getTransactionId();

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
