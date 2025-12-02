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

package com.navercorp.pinpoint.web.trace.controller;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapView;
import com.navercorp.pinpoint.web.applicationmap.MapView;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.LinkRender;
import com.navercorp.pinpoint.web.applicationmap.view.NodeRender;
import com.navercorp.pinpoint.web.applicationmap.view.TimeHistogramView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.trace.callstacks.RecordSet;
import com.navercorp.pinpoint.web.trace.model.TraceViewerData;
import com.navercorp.pinpoint.web.trace.service.SpanResult;
import com.navercorp.pinpoint.web.trace.service.SpanService;
import com.navercorp.pinpoint.web.trace.service.TransactionInfoService;
import com.navercorp.pinpoint.web.trace.span.CallTreeIterator;
import com.navercorp.pinpoint.web.trace.span.SpanFilters;
import com.navercorp.pinpoint.web.trace.view.TraceViewerDataView;
import com.navercorp.pinpoint.web.trace.view.TransactionCallTreeViewModel;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.LogLinkBuilder;
import com.navercorp.pinpoint.web.view.LogLinkView;
import com.navercorp.pinpoint.web.view.TransactionServerMapViewModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author emeroad
 * @author jaehong.kim
 * @author Taejin Koo
 */
@RestController
@RequestMapping(path = {"/api", "/api/transaction"})
@Validated
public class TransactionController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String DEFAULT_FOCUS_TIMESTAMP = "0";
    public static final String DEFAULT_SPAN_ID = "-1"; // SpanId.NULL

    private final SpanService spanService;
    private final TransactionInfoService transactionInfoService;
    private final FilteredMapService filteredMapService;
    private final HyperLinkFactory hyperLinkFactory;
    private final LogLinkBuilder logLinkBuilder;

    @Value("${web.callstack.selectSpans.limit:-1}")
    private int callstackSelectSpansLimit;


    public TransactionController(SpanService spanService,
                                 TransactionInfoService transactionInfoService,
                                 FilteredMapService filteredMapService,
                                 HyperLinkFactory hyperLinkFactory,
                                 LogLinkBuilder logLinkBuilder) {
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.transactionInfoService = Objects.requireNonNull(transactionInfoService, "transactionInfoService");
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.logLinkBuilder = Objects.requireNonNull(logLinkBuilder, "logLinkBuilder");
    }

    @GetMapping(value = "/trace")
    public TransactionCallTreeViewModel getTrace(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false)
            String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPAN_ID)
            long spanId
    ) {
        logger.debug("GET /trace params {traceId={}, focusTimestamp={}, agentId={}, spanId={}}",
                traceId, focusTimestamp, agentId, spanId);
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        // select spans
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.callTree();
        final RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);
        final LogLinkView logLinkView = logLinkBuilder.build(transactionId, spanId, recordSet.getApplicationName(), recordSet.getStartTime());

        return new TransactionCallTreeViewModel(transactionId, spanId, recordSet, spanResult.traceState(), logLinkView);
    }


    @GetMapping(value = "/traceViewerData")
    public TraceViewerDataView traceViewerData(
            @RequestParam("traceId") @NotBlank
            String traceIdParam,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false) @NullOrNotBlank
            String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = "-1")
            long spanId
    ) {
        logger.debug("GET /traceViewerData params {traceId={}, focusTimestamp={}, agentId={}, spanId={}}",
                traceIdParam, focusTimestamp, agentId, spanId);

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceIdParam);

        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);

        // select spans
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.callTree();

        final RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);
        TraceViewerData traceViewerData = new TraceViewerData(recordSet);
        return new TraceViewerDataView(traceViewerData.getTraceEvents());
    }

    @GetMapping(value = "/transaction/traceServerMap")
    public TransactionServerMapViewModel transactionServerMap(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPAN_ID) long spanId,
            @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false")
            boolean useStatisticsAgentState
    ) {
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);

        Range scanRange = Range.between(focusTimestamp, focusTimestamp + 1);
        // application map
        final FilteredMapServiceOption option = new FilteredMapServiceOption.Builder(transactionId, scanRange, columnGetCount)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final ApplicationMap map = filteredMapService.selectApplicationMap(option);
        MapView mapView = getApplicationMap(map);

        return new TransactionServerMapViewModel(transactionId, spanId, mapView);
    }

    private MapView getApplicationMap(ApplicationMap map) {
        TimeHistogramView view = TimeHistogramView.ResponseTime;
        NodeRender nodeRender = NodeRender.detailedRender(view, hyperLinkFactory);
        LinkRender linkRender = LinkRender.detailedRender(view);

        return new ApplicationMapView(map, nodeRender, linkRender);
    }

}