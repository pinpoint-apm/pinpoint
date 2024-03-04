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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanFilters;
import com.navercorp.pinpoint.web.service.SpanResult;
import com.navercorp.pinpoint.web.service.SpanService;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.LogLinkBuilder;
import com.navercorp.pinpoint.web.view.LogLinkView;
import com.navercorp.pinpoint.web.view.TraceViewerDataViewModel;
import com.navercorp.pinpoint.web.view.TransactionInfoViewModel;
import com.navercorp.pinpoint.web.view.TransactionTimelineInfoViewModel;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
@Validated
public class BusinessTransactionController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String DEFAULT_FOCUS_TIMESTAMP = "0";
    public static final String DEFAULT_SPAN_ID = "-1"; // SpanId.NULL
    private static final String SERVER_PREFIX = "api";

    private final SpanService spanService;
    private final TransactionInfoService transactionInfoService;
    private final FilteredMapService filteredMapService;
    private final LogLinkBuilder logLinkBuilder;

    @Value("${web.callstack.selectSpans.limit:-1}")
    private int callstackSelectSpansLimit;


    public BusinessTransactionController(SpanService spanService,
                                         TransactionInfoService transactionInfoService,
                                         FilteredMapService filteredMapService,
                                         LogLinkBuilder logLinkBuilder) {
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.transactionInfoService = Objects.requireNonNull(transactionInfoService, "transactionInfoService");
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.logLinkBuilder = Objects.requireNonNull(logLinkBuilder, "logLinkBuilder");
    }

    /**
     * info lookup for a selected transaction
     *
     * @param traceId traceId
     * @param focusTimestamp focusTimestamp
     * @return TransactionInfoViewModel
     */
    @GetMapping(value = "/transactionInfo")
    public TransactionInfoViewModel transactionInfo(
            @RequestParam("traceId") @NotBlank String traceId,
            @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP)
            @PositiveOrZero
            long focusTimestamp,
            @RequestParam(value = "agentId", required = false) @NotBlank String agentId,
            @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPAN_ID) long spanId,
            @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
            @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false")
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", required = false, defaultValue = "false")
            boolean useLoadHistogramFormat
    ) {
        logger.debug("GET /transactionInfo params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, v={}}",
                traceId, focusTimestamp, agentId, spanId, viewVersion);
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCount.of(callstackSelectSpansLimit);

        final Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        // select spans
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.callTree();

        // application map
        final FilteredMapServiceOption.Builder optionBuilder =
                new FilteredMapServiceOption.Builder(transactionId, viewVersion, columnGetCount);
        final FilteredMapServiceOption option =
                optionBuilder.setUseStatisticsAgentState(useStatisticsAgentState).build();
        final ApplicationMap map = filteredMapService.selectApplicationMap(option);

        final RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);


        final TransactionInfoViewModel result = newTransactionInfo(spanId, transactionId, spanResult, map, recordSet);

        if (useLoadHistogramFormat) {
            result.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return result;
    }

    private TransactionInfoViewModel newTransactionInfo(long spanId,
                                                        TransactionId transactionId,
                                                        SpanResult spanResult,
                                                        ApplicationMap map,
                                                        RecordSet recordSet) {
        final LogLinkView logLinkView = logLinkBuilder.build(
                transactionId,
                spanId,
                recordSet.getApplicationId(),
                recordSet.getStartTime()
        );

        return new TransactionInfoViewModel(
                transactionId,
                spanId,
                map.getNodes(),
                map.getLinks(),
                recordSet,
                spanResult.traceState(),
                logLinkView
        );
    }

    /**
     * info lookup for a selected transaction
     *
     * @param traceId traceId
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


}