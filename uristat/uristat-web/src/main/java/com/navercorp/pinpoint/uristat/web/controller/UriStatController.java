/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.uristat.web.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.uristat.web.chart.UriStatChartType;
import com.navercorp.pinpoint.uristat.web.chart.UriStatChartTypeFactory;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.service.UriStatChartService;
import com.navercorp.pinpoint.uristat.web.service.UriStatSummaryService;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;
import com.navercorp.pinpoint.uristat.web.view.UriStatView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/uriStat")
public class UriStatController {
    private final UriStatSummaryService uriStatService;
    private final TenantProvider tenantProvider;
    private final UriStatChartService uriStatChartService;
    private final UriStatChartTypeFactory chartTypeFactory;
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);
    private final RangeValidator rangeValidator;

    public UriStatController(UriStatSummaryService uriStatService,
                             UriStatChartService uriStatChartService,
                             TenantProvider tenantProvider,
                             UriStatChartTypeFactory chartTypeFactory,
                             @Qualifier("rangeValidator30d") RangeValidator rangeValidator) {
        this.uriStatService = Objects.requireNonNull(uriStatService);
        this.uriStatChartService = Objects.requireNonNull(uriStatChartService);
        this.chartTypeFactory = Objects.requireNonNull(chartTypeFactory);
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.rangeValidator = Objects.requireNonNull(rangeValidator, "rangeValidator");
    }

    private Range checkTimeRange(long from, long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());
        return range;
    }

    @GetMapping("summary")
    public List<UriStatSummary> getUriStatPagedSummary(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("orderby") String column,
            @RequestParam("isDesc") boolean isDesc,
            @RequestParam("count") int count
    ) {
        Range range = checkTimeRange(from, to);
        UriStatSummaryQueryParameter query = new UriStatSummaryQueryParameter.Builder()
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setRange(range)
                .setOrderby(column)
                .setDesc(isDesc)
                .setLimit(count)
                .build();

        if (query.isApplicationStat()) {
            return uriStatService.getUriStatApplicationPagedSummary(query);
        } else {
            return uriStatService.getUriStatAgentPagedSummary(query);
        }
    }

    @GetMapping("top50")
    @Deprecated
    public List<UriStatSummary> getUriStatSummary(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to
    ) {
        Range range = checkTimeRange(from, to);
        UriStatSummaryQueryParameter query = new UriStatSummaryQueryParameter.Builder()
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setRange(range)
                .build();

        if (query.isApplicationStat()) {
            return uriStatService.getUriStatApplicationSummary(query);
        } else {
            return uriStatService.getUriStatAgentSummary(query);
        }
    }

    @Deprecated
    @GetMapping("/chart")
    public UriStatView getCollectedUriStat(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("uri") String uri,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "type", required = false) String type
    ) {
        Range range = checkTimeRange(from, to);
        TimeWindow timeWindow = new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER);
        UriStatChartQueryParameter query = new UriStatChartQueryParameter.Builder()
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setAgentId(agentId)
                .setUri(uri)
                .setRange(timeWindow.getWindowRange())
                .setTimeSize((int) timeWindow.getWindowSlotSize())
                .setTimePrecision(TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize()))
                .build();

        UriStatChartType chartType = chartTypeFactory.valueOf(type.toLowerCase());
        List<UriStatChartValue> uriStats = getChartData(chartType, query);
        return new UriStatView(uri, timeWindow, uriStats, chartType);
    }

    private List<UriStatChartValue> getChartData(UriStatChartType chartType, UriStatChartQueryParameter query) {
        if (query.isApplicationStat()) {
            return uriStatChartService.getUriStatChartDataApplication(chartType, query);
        } else {
            return uriStatChartService.getUriStatChartDataAgent(chartType, query);
        }
    }
}
