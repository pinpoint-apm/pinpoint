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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapView;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapViewV3;
import com.navercorp.pinpoint.web.applicationmap.MapView;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.LinkRender;
import com.navercorp.pinpoint.web.applicationmap.view.NodeRender;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanFilters;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.service.SpanResult;
import com.navercorp.pinpoint.web.service.SpanService;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.LogLinkBuilder;
import com.navercorp.pinpoint.web.view.LogLinkView;
import com.navercorp.pinpoint.web.view.TraceViewerDataViewModel;
import com.navercorp.pinpoint.web.view.TransactionCallTreeViewModel;
import com.navercorp.pinpoint.web.view.TransactionServerMapViewModel;
import com.navercorp.pinpoint.web.view.TransactionTimelineInfoViewModel;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final String SERVER_PREFIX = "api";

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
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPAN_ID) long spanId,
            @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false")
            boolean useStatisticsAgentState
    ) {
        return getTransaction0(traceId, focusTimestamp, agentId, spanId, useStatisticsAgentState, TimeHistogramFormat.V3);
    }

    /**
     * info lookup for a selected transaction
     *
     * @param traceId        traceId
     * @param focusTimestamp focusTimestamp
     * @return TransactionInfoViewModel
     */
    @GetMapping(value = "/transactionInfo")
    public TransactionCallTreeViewModel transactionInfo(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPAN_ID) long spanId,
            @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false")
            boolean useStatisticsAgentState
    ) {
        TimeHistogramFormat format = TimeHistogramFormat.V1;
        return getTransaction0(traceId, focusTimestamp, agentId, spanId, useStatisticsAgentState, format);
    }

    private TransactionCallTreeViewModel getTransaction0(String traceId,
                                                         long focusTimestamp,
                                                         String agentId,
                                                         long spanId,
                                                         boolean useStatisticsAgentState,
                                                         TimeHistogramFormat format) {
        logger.debug("GET /trace params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, format={}}",
                traceId, focusTimestamp, agentId, spanId, format);
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

    /**
     * info lookup for a selected transaction
     *
     * @param traceId        traceId
     * @param focusTimestamp focusTimestamp
     * @return TransactionTimelineInfoViewModel
     */
    @GetMapping(value = "/transactionTimelineInfo")
    public TransactionTimelineInfoViewModel transactionTimelineInfo(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false) @NullOrNotBlank String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPAN_ID) long spanId
    ) {
        logger.debug("GET /transactionTimelineInfo params {traceId={}, focusTimestamp={}, agentId={}, spanId={}}",
                traceId, focusTimestamp, agentId, spanId);
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);

        // select spans
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.callTree();

        final String traceViewerDataURL = ServletUriComponentsBuilder.fromPath(SERVER_PREFIX + "/traceViewerData")
                .queryParam("traceId", URLEncoder.encode(traceId, StandardCharsets.UTF_8))
                .queryParam("focusTimestamp", focusTimestamp)
                .queryParam("agentId", URLEncoder.encode(agentId, StandardCharsets.UTF_8))
                .queryParam("spanId", spanId)
                .build()
                .toUriString();

        final RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);
        return new TransactionTimelineInfoViewModel(transactionId, recordSet, traceViewerDataURL);
    }

    @GetMapping(value = "/traceViewerData")
    public TraceViewerDataViewModel traceViewerData(
            @RequestParam("traceId") @NotBlank String traceIdParam,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false) @NullOrNotBlank String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = "-1") long spanId
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
        return new TraceViewerDataViewModel(recordSet);
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
        TimeHistogramFormat format = TimeHistogramFormat.V1;
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);

        Range scanRange = Range.between(focusTimestamp, focusTimestamp + 1);
        // application map
        final FilteredMapServiceOption option = new FilteredMapServiceOption.Builder(transactionId, scanRange, columnGetCount)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final ApplicationMap map = filteredMapService.selectApplicationMap(option);
        MapView mapView = getApplicationMap(map, format);

        return new TransactionServerMapViewModel(transactionId, spanId, mapView);
    }

    private MapView getApplicationMap(ApplicationMap map, TimeHistogramFormat format) {
        if (format == TimeHistogramFormat.V3) {
            TimeWindow timeWindow = new TimeWindow(map.getRange());

            NodeRender nodeRender = NodeRender.detailedRender(format, hyperLinkFactory);
            LinkRender linkRender = LinkRender.detailedRender(format);

            return new ApplicationMapViewV3(map, timeWindow, nodeRender, linkRender);
        }

        NodeRender nodeRender = NodeRender.detailedRender(format, hyperLinkFactory);
        LinkRender linkRender = LinkRender.detailedRender(format);

        return new ApplicationMapView(map, nodeRender, linkRender);
    }

}