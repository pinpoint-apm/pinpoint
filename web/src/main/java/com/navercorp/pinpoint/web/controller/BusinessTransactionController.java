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
import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCountFactory;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanFilters;
import com.navercorp.pinpoint.web.config.LogConfiguration;
import com.navercorp.pinpoint.web.query.BindType;
import com.navercorp.pinpoint.web.query.QueryService;
import com.navercorp.pinpoint.web.query.QueryServiceFactory;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.service.SpanResult;
import com.navercorp.pinpoint.web.service.SpanService;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.view.TraceViewerDataViewModel;
import com.navercorp.pinpoint.web.view.TransactionInfoViewModel;
import com.navercorp.pinpoint.web.view.TransactionTimelineInfoViewModel;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
public class BusinessTransactionController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String DEFAULT_FOCUS_TIMESTAMP = "0";
    public static final String DEFAULT_SPANID = "-1"; // SpanId.NULL

    private final SpanService spanService;
    private final TransactionInfoService transactionInfoService;
    private final FilteredMapService filteredMapService;
    private final LogConfiguration logConfiguration;
    private final QueryServiceFactory queryServiceFactory;


    @Value("${web.callstack.selectSpans.limit:-1}")
    private int callstackSelectSpansLimit;


    public BusinessTransactionController(SpanService spanService, TransactionInfoService transactionInfoService,
                                         FilteredMapService filteredMapService, LogConfiguration logConfiguration,
                                         QueryServiceFactory queryServiceFactory) {
        this.spanService = Objects.requireNonNull(spanService, "spanService");
        this.transactionInfoService = Objects.requireNonNull(transactionInfoService, "transactionInfoService");
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.logConfiguration = Objects.requireNonNull(logConfiguration, "logConfiguration");
        this.queryServiceFactory = Objects.requireNonNull(queryServiceFactory, "queryServiceFactory");
    }

    /**
     * info lookup for a selected transaction
     *
     * @param traceId
     * @param focusTimestamp
     * @return
     */
    @GetMapping(value = "/transactionInfo")
    public TransactionInfoViewModel transactionInfo(@RequestParam("traceId") String traceId,
                                                    @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP) long focusTimestamp,
                                                    @RequestParam(value = "agentId", required = false) String agentId,
                                                    @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPANID) long spanId,
                                                    @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
                                                    @RequestParam(value = "useStatisticsAgentState", required = false, defaultValue = "false") boolean useStatisticsAgentState) {
        logger.debug("GET /transactionInfo params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, v={}}", traceId, focusTimestamp, agentId, spanId, viewVersion);
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCountFactory.create(callstackSelectSpansLimit);

        Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        // select spans
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.getCallTree();

        // application map
        FilteredMapServiceOption.Builder optionBuilder = new FilteredMapServiceOption.Builder(transactionId, viewVersion, columnGetCount);
        final FilteredMapServiceOption option = optionBuilder.setUseStatisticsAgentState(useStatisticsAgentState).build();
        ApplicationMap map = filteredMapService.selectApplicationMap(option);

        RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);

        TransactionInfoViewModel result = new TransactionInfoViewModel(transactionId, spanId, map.getNodes(), map.getLinks(), recordSet, spanResult.getTraceState(), logConfiguration);
        return result;
    }

    /**
     * info lookup for a selected transaction
     *
     * @param traceId
     * @param focusTimestamp
     * @return
     */
    @GetMapping(value = "/transactionTimelineInfo")
    public TransactionTimelineInfoViewModel transactionTimelineInfo(@RequestParam("traceId") String traceId,
                                                                    @RequestParam(value = "focusTimestamp", required = false, defaultValue = DEFAULT_FOCUS_TIMESTAMP) long focusTimestamp,
                                                                    @RequestParam(value = "agentId", required = false) String agentId,
                                                                    @RequestParam(value = "spanId", required = false, defaultValue = DEFAULT_SPANID) long spanId) {
        logger.debug("GET /transactionTimelineInfo params {traceId={}, focusTimestamp={}, agentId={}, spanId={}}",
                traceId, focusTimestamp, agentId, spanId);
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceId);
        final ColumnGetCount columnGetCount = ColumnGetCountFactory.create(callstackSelectSpansLimit);

        // select spans
        Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.getCallTree();

        String traceViewerDataURL = ServletUriComponentsBuilder.fromPath("traceViewerData.pinpoint")
                .queryParam("traceId", URLEncoder.encode(traceId, StandardCharsets.UTF_8))
                .queryParam("focusTimestamp", focusTimestamp)
                .queryParam("agentId", URLEncoder.encode(agentId, StandardCharsets.UTF_8))
                .queryParam("spanId", spanId)
                .build()
                .toUriString();

        RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);
        return new TransactionTimelineInfoViewModel(transactionId, spanId, recordSet, traceViewerDataURL, logConfiguration);
    }

    @GetMapping(value = "/traceViewerData")
    public TraceViewerDataViewModel traceViewerData(@RequestParam("traceId") String traceIdParam,
                                                    @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") long focusTimestamp,
                                                    @RequestParam(value = "agentId", required = false) String agentId,
                                                    @RequestParam(value = "spanId", required = false, defaultValue = "-1") long spanId) {
        logger.debug("GET /traceViewerData params {traceId={}, focusTimestamp={}, agentId={}, spanId={}}", traceIdParam, focusTimestamp, agentId, spanId);

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceIdParam);

        final ColumnGetCount columnGetCount = ColumnGetCountFactory.create(callstackSelectSpansLimit);

        // select spans
        Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(spanId, agentId, focusTimestamp);
        SpanResult spanResult = this.spanService.selectSpan(transactionId, spanMatchFilter, columnGetCount);
        final CallTreeIterator callTreeIterator = spanResult.getCallTree();

        RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, spanMatchFilter);
        return new TraceViewerDataViewModel(recordSet);
    }

    @PostMapping(value = "/bind")
    public BindSqlView metaDataBind(@RequestParam("type") String type,
                               @RequestParam("metaData") String metaData,
                               @RequestParam("bind") String bind) {
        if (logger.isDebugEnabled()) {
            logger.debug("POST /bind params {metaData={}, bind={}}", metaData, bind);
        }

        final BindType bindType = BindType.of(type);
        if (bindType == null) {
            throw new IllegalArgumentException("Unknown Type:" + type);
        }

        if (metaData == null) {
            return new BindSqlView("");
        }

        final QueryService service = queryServiceFactory.getService(bindType);
        final String bindedQuery = service.bind(metaData, bind);
        if (logger.isDebugEnabled()) {
            logger.debug("bindedQuery={}", bindedQuery);
        }

        return new BindSqlView(bindedQuery);
    }

    public static class BindSqlView {
        private final String bindedQuery;

        public BindSqlView(String bindedQuery) {
            this.bindedQuery = Objects.requireNonNull(bindedQuery, "bindedQuery");
        }

        public String getBindedQuery() {
            return bindedQuery;
        }
    }
}