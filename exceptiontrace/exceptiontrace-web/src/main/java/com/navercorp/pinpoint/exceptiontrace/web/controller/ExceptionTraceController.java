/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.exceptiontrace.web.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.exceptiontrace.web.model.ErrorSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionGroupSummary;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import com.navercorp.pinpoint.exceptiontrace.web.service.ExceptionTraceService;
import com.navercorp.pinpoint.exceptiontrace.web.util.ExceptionTraceQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.util.GroupByAttributes;
import com.navercorp.pinpoint.exceptiontrace.web.view.ErrorSummaryView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionDetailView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartView;
import com.navercorp.pinpoint.exceptiontrace.web.util.TimeSeriesUtils;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(value = "/api/errors")
@Validated
public class ExceptionTraceController {

    private static final TimePrecision DETAILED_TIME_PRECISION = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 1);
    private static final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);
    private static final TimeWindowSampler ROUGH_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 10);
    private final Logger logger = LogManager.getLogger(this.getClass());


    private final ExceptionTraceService exceptionTraceService;
    private final TenantProvider tenantProvider;
    private final RangeValidator rangeValidator;

    @Value("${pinpoint.modules.web.exceptiontrace.table:exceptionTrace}")
    private String tableName;

    public ExceptionTraceController(
            ExceptionTraceService exceptionTraceService,
            TenantProvider tenantProvider,
            @Qualifier("rangeValidator7d") RangeValidator rangeValidator
    ) {
        this.exceptionTraceService = Objects.requireNonNull(exceptionTraceService, "exceptionTraceService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.rangeValidator = Objects.requireNonNull(rangeValidator, "rangeValidator");
    }

    @GetMapping("/transactionInfo")
    public List<ExceptionDetailView> getListOfExceptionMetaDataFromTransactionId(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("transactionId") @NotBlank String transactionId,
            @RequestParam("spanId") long spanId,
            @RequestParam("exceptionId") long exceptionId
    ) {
        ExceptionTraceQueryParameter queryParameter = new ExceptionTraceQueryParameter.Builder()
                .setTableName(tableName)
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setTransactionId(transactionId)
                .setSpanId(spanId)
                .setExceptionId(exceptionId)
                .setTimePrecision(DETAILED_TIME_PRECISION)
                .build();
        return exceptionTraceService.getDetailExceptions(
                queryParameter
        );
    }

    @GetMapping("/summary")
    public List<ErrorSummaryView> getErrorSummaries(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range, ROUGH_TIME_WINDOW_SAMPLER);


        ExceptionTraceQueryParameter queryParameter = new ExceptionTraceQueryParameter.Builder()
                .setTableName(tableName)
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setRange(range)
                .setTimePrecision(TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize()))
                .setTimeWindowRangeCount(timeWindow.getWindowRangeCount())
                .setGroupByAttributes(List.of(
                        GroupByAttributes.ERROR_MESSAGE_LOG_TYPE,
                        GroupByAttributes.ERROR_CLASS_NAME,
                        GroupByAttributes.STACK_TRACE
                ))
                .build();

        return exceptionTraceService.getErrorSummaries(
                queryParameter
        ).stream().map(
                (ErrorSummary e) -> new ErrorSummaryView(e, timeWindow)
        ).toList();
    }

    @GetMapping("/errorList")
    public List<ExceptionDetailView> getListOfExceptionMetaDataByGivenRange(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,

            @RequestParam(value = "filters", required = false) List<String> filters,
            @RequestParam("orderBy") String orderBy,
            @RequestParam("isDesc") boolean isDesc,
            @RequestParam("count") int count
    ) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range);

        ExceptionTraceQueryParameter queryParameter = new ExceptionTraceQueryParameter.Builder()
                .setTableName(tableName)
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setRange(range)
                .setTimePrecision(DETAILED_TIME_PRECISION)
                .setHardLimit(count)
                .addAllFilters(filters)
                .setOrderBy(orderBy)
                .setIsDesc(isDesc)
                .build();
        return exceptionTraceService.getSummarizedExceptions(
                queryParameter
        );
    }

    @GetMapping("/errorList/groupBy")
    public List<ExceptionGroupSummary> getListOfExceptionMetaDataWithDynamicGroupBy(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,

            @RequestParam("groupBy") List<String> groupByList
    ) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range);

        ExceptionTraceQueryParameter queryParameter = new ExceptionTraceQueryParameter.Builder()
                .setTableName(tableName)
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setRange(Range.between(from, to))
                .setTimePrecision(DETAILED_TIME_PRECISION)
                .addAllGroupByList(groupByList)
                .build();
        return exceptionTraceService.getGroupSummaries(
                queryParameter
        );
    }

    @GetMapping("/chart")
    public ExceptionChartView getCollectedExceptionMetaDataByGivenRange(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,

            @RequestParam(value = "groupBy", required = false) List<String> groupByList
    ) {
        String groupName = (groupByList == null) ? "total error occurs" : "top5 error occurs";

        Range range = Range.between(from, to);
        rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER);

        ExceptionTraceQueryParameter queryParameter = new ExceptionTraceQueryParameter.Builder()
                .setTableName(tableName)
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setRange(timeWindow.getWindowRange())
                .setTimePrecision(TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize()))
                .setTimeWindowRangeCount(timeWindow.getWindowRangeCount())
                .addAllGroupByList(groupByList)
                .build();
        List<ExceptionChartValueView> exceptionChartValueViews = exceptionTraceService.getChartViews(
                queryParameter
        );

        return TimeSeriesUtils.newChartView(groupName, timeWindow, exceptionChartValueViews);
    }

    @GetMapping("/groups")
    public List<String> getGroups() {
        return Arrays.stream(new GroupByAttributes[]{
                        GroupByAttributes.ERROR_MESSAGE_LOG_TYPE, GroupByAttributes.ERROR_CLASS_NAME,
                        GroupByAttributes.STACK_TRACE, GroupByAttributes.URI_TEMPLATE
                }).map(GroupByAttributes::getName)
                .collect(Collectors.toList());
    }
}
