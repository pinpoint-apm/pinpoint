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
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapView;
import com.navercorp.pinpoint.web.applicationmap.MapView;
import com.navercorp.pinpoint.web.applicationmap.config.MapProperties;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.LinkRender;
import com.navercorp.pinpoint.web.applicationmap.view.NodeRender;
import com.navercorp.pinpoint.web.applicationmap.view.TimeHistogramView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.service.ScatterChartService;
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
import com.navercorp.pinpoint.web.view.transactionlist.DotMetaDataView;
import com.navercorp.pinpoint.web.view.transactionlist.TransactionMetaDataViewModel;
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

import java.util.ArrayList;
import java.util.List;
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

    private final MapProperties mapProperties;
    private final SpanService spanService;
    private final TransactionInfoService transactionInfoService;
    private final FilteredMapService filteredMapService;
    private final HyperLinkFactory hyperLinkFactory;
    private final LogLinkBuilder logLinkBuilder;
    private final ScatterChartService scatterChartService;

    @Value("${web.callstack.selectSpans.limit:-1}")
    private int callstackSelectSpansLimit;


    public TransactionController(MapProperties mapProperties,
                                 SpanService spanService,
                                 TransactionInfoService transactionInfoService,
                                 FilteredMapService filteredMapService,
                                 HyperLinkFactory hyperLinkFactory,
                                 LogLinkBuilder logLinkBuilder,
                                 ScatterChartService scatterChartService) {
        this.mapProperties = Objects.requireNonNull(mapProperties, "mapProperties");
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.transactionInfoService = Objects.requireNonNull(transactionInfoService, "transactionInfoService");
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.logLinkBuilder = Objects.requireNonNull(logLinkBuilder, "logLinkBuilder");
        this.scatterChartService = Objects.requireNonNull(scatterChartService, "scatterChartService");
    }

    @GetMapping(value = "/trace")
    public TransactionCallTreeViewModel getTrace(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false)
            String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = SpanId.NULL_STRING)
            long spanId,
            @RequestParam(value = "linkTraceId", required = false) @NullOrNotBlank
            String linkTraceId,
            @RequestParam(value = "linkSpanId", required = false, defaultValue = SpanId.NULL_STRING)
            long linkSpanId
    ) {
        logger.debug("GET /trace params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, linkTraceId={}, linkSpanId={}}",
                traceId, focusTimestamp, agentId, spanId, linkTraceId, linkSpanId);
        ServerTraceId serverTraceId = ServerTraceId.of(traceId);
        final long focusSpanId = focusSpanId(spanId, linkTraceId, linkSpanId);
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(focusSpanId, agentId, focusTimestamp);
        return getTransactionCallTree(serverTraceId, spanMatchFilter, spanId, linkTraceId, linkSpanId);
    }

    @GetMapping(value = "/trace/link")
    public TransactionCallTreeViewModel getTraceLink(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "spanId", required = false, defaultValue = SpanId.NULL_STRING)
            long spanId,
            @RequestParam(value = "linkTraceId", required = false) @NullOrNotBlank
            String linkTraceId,
            @RequestParam(value = "linkSpanId", required = false, defaultValue = SpanId.NULL_STRING)
            long linkSpanId
    ) {
        logger.debug("GET /trace/link params {traceId={}, focusTimestamp={}, spanId={}, linkTraceId={}, linkSpanId={}}",
                traceId, focusTimestamp, spanId, linkTraceId, linkSpanId);
        ServerTraceId serverTraceId = ServerTraceId.of(traceId);
        final long focusSpanId = focusSpanId(spanId, linkTraceId, linkSpanId);
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(focusSpanId, null, focusTimestamp);
        return getTransactionCallTree(serverTraceId, spanMatchFilter, spanId, linkTraceId, linkSpanId);
    }

    private TransactionCallTreeViewModel getTransactionCallTree(
            ServerTraceId serverTraceId,
            Predicate<SpanBo> spanMatchFilter,
            long spanId,
            String linkTraceId,
            long linkSpanId
    ) {
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);
        final SpanResult spanResult;
        if (linkTraceId != null && linkSpanId != SpanId.NULL) {
            final ServerTraceId linkServerTraceId = ServerTraceId.of(linkTraceId);
            spanResult = this.spanService.selectSpanAndLink(serverTraceId, spanMatchFilter, spanId, linkServerTraceId, columnGetCount);
        } else {
            spanResult = this.spanService.selectSpan(serverTraceId, spanMatchFilter, columnGetCount);
        }
        final CallTreeIterator callTreeIterator = spanResult.callTree();
        final RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);

        final String traceIdStr = serverTraceId.toString();
        final LogLinkView logLinkView = logLinkBuilder.build(traceIdStr, spanId, recordSet.getApplicationName(), recordSet.getStartTime());
        return new TransactionCallTreeViewModel(traceIdStr, spanId, recordSet, spanResult.traceState(), logLinkView);
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
            long spanId,
            @RequestParam(value = "linkTraceId", required = false) @NullOrNotBlank
            String linkTraceId,
            @RequestParam(value = "linkSpanId", required = false, defaultValue = SpanId.NULL_STRING)
            long linkSpanId
    ) {
        logger.debug("GET /traceViewerData params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, linkTraceId={}, linkSpanId={}}",
                traceIdParam, focusTimestamp, agentId, spanId, linkTraceId, linkSpanId);

        ServerTraceId serverTraceId = ServerTraceId.of(traceIdParam);
        final long focusSpanId = focusSpanId(spanId, linkTraceId, linkSpanId);
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(focusSpanId, agentId, focusTimestamp);
        return buildTraceViewerData(serverTraceId, spanMatchFilter, spanId, linkTraceId, linkSpanId);
    }

    @GetMapping(value = "/traceViewerData/link")
    public TraceViewerDataView traceViewerDataLink(
            @RequestParam("traceId") @NotBlank
            String traceIdParam,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "spanId", required = false, defaultValue = "-1")
            long spanId,
            @RequestParam(value = "linkTraceId", required = false) @NullOrNotBlank
            String linkTraceId,
            @RequestParam(value = "linkSpanId", required = false, defaultValue = SpanId.NULL_STRING)
            long linkSpanId
    ) {
        logger.debug("GET /traceViewerData/link params {traceId={}, focusTimestamp={}, spanId={}, linkTraceId={}, linkSpanId={}}",
                traceIdParam, focusTimestamp, spanId, linkTraceId, linkSpanId);

        ServerTraceId serverTraceId = ServerTraceId.of(traceIdParam);
        final long focusSpanId = focusSpanId(spanId, linkTraceId, linkSpanId);
        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(focusSpanId, null, focusTimestamp);
        return buildTraceViewerData(serverTraceId, spanMatchFilter, spanId, linkTraceId, linkSpanId);
    }

    private TraceViewerDataView buildTraceViewerData(
            ServerTraceId serverTraceId,
            Predicate<SpanBo> spanMatchFilter,
            long spanId,
            String linkTraceId,
            long linkSpanId
    ) {
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);
        final SpanResult spanResult;
        if (linkTraceId != null && linkSpanId != SpanId.NULL) {
            final ServerTraceId linkServerTraceId = ServerTraceId.of(linkTraceId);
            spanResult = this.spanService.selectSpanAndLink(serverTraceId, spanMatchFilter, spanId, linkServerTraceId, columnGetCount);
        } else {
            spanResult = this.spanService.selectSpan(serverTraceId, spanMatchFilter, columnGetCount);
        }
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
            @RequestParam(value = "spanId", required = false, defaultValue = SpanId.NULL_STRING) long spanId,
            @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false")
            boolean useStatisticsAgentState,
            @RequestParam(value = "linkTraceId", required = false) @NullOrNotBlank
            String linkTraceId,
            @RequestParam(value = "linkSpanId", required = false, defaultValue = SpanId.NULL_STRING)
            long linkSpanId
    ) {
        ServerTraceId serverTraceId = ServerTraceId.of(traceId);
        Range scanRange = Range.between(focusTimestamp, focusTimestamp + 1);
        return buildTransactionServerMap(serverTraceId, scanRange, spanId, useStatisticsAgentState, linkTraceId, linkSpanId);
    }

    @GetMapping(value = "/transaction/traceServerMap/link")
    public TransactionServerMapViewModel transactionServerMapLink(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "spanId", required = false, defaultValue = SpanId.NULL_STRING) long spanId,
            @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false")
            boolean useStatisticsAgentState,
            @RequestParam(value = "linkTraceId", required = false) @NullOrNotBlank
            String linkTraceId,
            @RequestParam(value = "linkSpanId", required = false, defaultValue = SpanId.NULL_STRING)
            long linkSpanId
    ) {
        logger.debug("GET /transaction/traceServerMap/link params {traceId={}, focusTimestamp={}, spanId={}, useStatisticsAgentState={}, linkTraceId={}, linkSpanId={}}",
                traceId, focusTimestamp, spanId, useStatisticsAgentState, linkTraceId, linkSpanId);
        ServerTraceId serverTraceId = ServerTraceId.of(traceId);
        Range scanRange = Range.between(focusTimestamp, focusTimestamp + 1);
        return buildTransactionServerMap(serverTraceId, scanRange, spanId, useStatisticsAgentState, linkTraceId, linkSpanId);
    }

    private TransactionServerMapViewModel buildTransactionServerMap(
            ServerTraceId serverTraceId,
            Range scanRange,
            long spanId,
            boolean useStatisticsAgentState,
            String linkTraceId,
            long linkSpanId
    ) {
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);
        final FilteredMapServiceOption.Builder builder;
        if (linkTraceId != null && linkSpanId != SpanId.NULL) {
            final ServerTraceId linkServerTraceId = ServerTraceId.of(linkTraceId);
            builder = new FilteredMapServiceOption.Builder(
                    List.of(serverTraceId, linkServerTraceId), scanRange, columnGetCount);
        } else {
            builder = new FilteredMapServiceOption.Builder(serverTraceId, scanRange, columnGetCount);
        }
        final FilteredMapServiceOption option = builder
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final ApplicationMap map = filteredMapService.selectApplicationMap(option);
        MapView mapView = getApplicationMap(map);
        return new TransactionServerMapViewModel(serverTraceId.toString(), spanId, mapView);
    }

    @GetMapping(value = "/transaction/metadata")
    public MetadataView getTransactionMetadata(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "spanId", required = false, defaultValue = SpanId.NULL_STRING) long spanId
    ) {
        logger.debug("GET /transaction/metadata params {traceId={}, spanId={}}", traceId, spanId);

        final ServerTraceId serverTraceId = ServerTraceId.of(traceId);
        List<SpanBo> spans = scatterChartService.selectTransactionMetadata(serverTraceId);

        if (spanId != SpanId.NULL) {
            spans = filterSpanId(spans, spanId);
        }

        final TransactionMetaDataViewModel viewModel = new TransactionMetaDataViewModel(spans);
        return new MetadataView(viewModel.getMetadata());
    }

    private List<SpanBo> filterSpanId(List<SpanBo> spans, long spanId) {
        List<SpanBo> list = new ArrayList<>();
        for (SpanBo span : spans) {
            if (span.getSpanId() == spanId) {
                list.add(span);
            }
        }
        return list;
    }

    public record MetadataView(List<? extends DotMetaDataView> metadata, boolean complete, long resultFrom) {
        public MetadataView(List<? extends DotMetaDataView> metadata) {
            this(metadata, true, 0);
        }
    }

    private static long focusSpanId(long spanId, String linkTraceId, long linkSpanId) {
        // When the OTel link is followed, the merged tree's main is the upstream trace
        // but the user originated from the downstream side (where the Link annotation lives).
        // Focus on linkSpanId so the highlighted row lands on the originating downstream span.
        if (linkTraceId != null && linkSpanId != SpanId.NULL) {
            return linkSpanId;
        }
        return spanId;
    }

    private MapView getApplicationMap(ApplicationMap map) {
        TimeHistogramView view = TimeHistogramView.ResponseTime;
        NodeRender nodeRender = NodeRender.detailedRender(view, hyperLinkFactory, mapProperties);
        LinkRender linkRender = LinkRender.detailedRender(view, mapProperties);

        return new ApplicationMapView(map, nodeRender, linkRender);
    }

}