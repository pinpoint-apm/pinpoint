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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class ScatterChartServiceImpl implements ScatterChartService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationTraceIndexService applicationTraceIndexService;

    private final TraceDao traceDao;

    private final SpanService spanService;

    public ScatterChartServiceImpl(ApplicationTraceIndexService applicationTraceIndexService,
                                   TraceDao traceDao,
                                   SpanService spanService) {
        this.applicationTraceIndexService = Objects.requireNonNull(applicationTraceIndexService, "applicationTraceIndexService");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.spanService = Objects.requireNonNull(spanService, "spanService");
    }

    @Override
    public List<Dot> selectScatterData(List<TransactionId> transactionIdList, String applicationName, Filter<List<SpanBo>> filter) {
        Objects.requireNonNull(transactionIdList, "transactionIdList");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(filter, "filter");

        final List<List<SpanBo>> traceList = traceDao.selectAllSpans(transactionIdList);

        final List<Dot> result = new ArrayList<>();

        for (List<SpanBo> trace : traceList) {
            if (!filter.include(trace)) {
                continue;
            }

            for (SpanBo span : trace) {
                if (applicationName.equals(span.getApplicationName())) {
                    final TransactionId transactionId = span.getTransactionId();
                    final Dot dot = new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId());
                    result.add(dot);
                }
            }
        }

        return result;
    }

    /**
     * Queries for details on dots selected from the scatter chart.
     */
    @Override
    public List<SpanBo> selectTransactionMetadata(final List<GetTraceInfo> getTraceInfoList) {
        Objects.requireNonNull(getTraceInfoList, "getTraceInfoList");

        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(getTraceInfoList);
        populateAgentNameListOfList(selectedSpans);

        return ListListUtils.toList(selectedSpans, getTraceInfoList.size());
    }

    @Override
    public List<SpanBo> selectTransactionMetadata(TransactionId transactionId) {
        final List<SpanBo> selectedSpans = traceDao.selectSpan(transactionId);
        populateAgentName(selectedSpans);
        return selectedSpans;
    }

    @Override
    public ScatterData selectScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        LimitedScanResult<List<Dot>> scanResult = applicationTraceIndexService.scanTraceScatterData(applicationName, range, limit, backwardDirection);

        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        builder.addDot(scanResult.scanData());
        return builder.build();
    }

    @Override
    public ScatterData selectScatterData(List<TransactionId> transactionIdList, String applicationName, Range range, int xGroupUnit, int yGroupUnit, Filter<List<SpanBo>> filter) {
        Objects.requireNonNull(transactionIdList, "transactionIdList");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(filter, "filter");

        final List<List<SpanBo>> traceList = traceDao.selectAllSpans(transactionIdList);
        populateAgentNameListOfList(traceList);

        ScatterDataBuilder scatterData = new ScatterDataBuilder(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        for (List<SpanBo> trace : traceList) {
            if (!filter.include(trace)) {
                continue;
            }

            for (SpanBo span : trace) {
                if (applicationName.equals(span.getApplicationName())) {
                    final TransactionId transactionId = span.getTransactionId();
                    final Dot dot = new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId());
                    scatterData.addDot(dot);
                }
            }
        }

        return scatterData.build();
    }

    private void populateAgentNameListOfList(Collection<List<SpanBo>> listOfList) {
        if (CollectionUtils.isEmpty(listOfList)) {
            return;
        }
        final List<SpanBo> list = listOfList.stream().flatMap(List::stream).collect(Collectors.toList());
        populateAgentName(list);
    }

    private void populateAgentName(List<SpanBo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        spanService.populateAgentName(list);
    }

}
