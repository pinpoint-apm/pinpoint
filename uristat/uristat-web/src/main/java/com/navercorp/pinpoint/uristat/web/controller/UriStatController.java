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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.uristat.web.model.UriStatHistogram;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.service.UriStatService;
import com.navercorp.pinpoint.uristat.web.util.UriStatQueryParameter;
import com.navercorp.pinpoint.uristat.web.view.UriStatView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/uriStat")
public class UriStatController {
    private final UriStatService uriStatService;
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);
    private final TenantProvider tenantProvider;

    public UriStatController(UriStatService uriStatService, TenantProvider tenantProvider) {
        this.uriStatService = Objects.requireNonNull(uriStatService);
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
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
        UriStatQueryParameter.Builder builder = new UriStatQueryParameter.Builder()
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setRange(Range.newRange(from, to))
                .setOrderby(column)
                .setDesc(isDesc)
                .setLimit(count);

        if (StringUtils.isEmpty(agentId)) {
            return uriStatService.getUriStatApplicationPagedSummary(builder.build());
        } else {
            builder.setAgentId(agentId);
            return uriStatService.getUriStatAgentPagedSummary(builder.build());
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
        UriStatQueryParameter.Builder builder = new UriStatQueryParameter.Builder()
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setRange(Range.newRange(from, to));

        if (StringUtils.isEmpty(agentId)) {
            return uriStatService.getUriStatApplicationSummary(builder.build());
        } else {
            builder.setAgentId(agentId);
            return uriStatService.getUriStatAgentSummary(builder.build());
        }
    }

    @GetMapping("chart")
    public UriStatView getCollectedUriStat(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam("uri") String uri,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "type", required = false) String type
    ) {
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        UriStatQueryParameter.Builder builder = new UriStatQueryParameter.Builder()
                .setTenantId(tenantProvider.getTenantId())
                .setApplicationName(applicationName)
                .setUri(uri)
                .setRange(timeWindow.getWindowRange())
                .setTimeSize((int) timeWindow.getWindowSlotSize())
                .setTimePrecision(TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize()));

        List<UriStatHistogram> uriStats;
        if (!StringUtils.isEmpty(type) && type.equalsIgnoreCase("failure")) {
            uriStats = getFailedHistogramChartData(builder, agentId);
        } else {
            uriStats = getTotalHistogramChartData(builder, agentId);
        }

        return new UriStatView(uri, timeWindow, uriStats);
    }

    private List<UriStatHistogram> getTotalHistogramChartData(UriStatQueryParameter.Builder builder, String agentId) {
        if (StringUtils.isEmpty(agentId)) {
            return uriStatService.getCollectedUriStatApplication(builder.build());
        } else {
            builder.setAgentId(agentId);
            return uriStatService.getCollectedUriStatAgent(builder.build());
        }
    }

    private List<UriStatHistogram> getFailedHistogramChartData(UriStatQueryParameter.Builder builder, String agentId) {
        if (StringUtils.isEmpty(agentId)) {
            return uriStatService.getFailedUriStatApplication(builder.build());
        } else {
            builder.setAgentId(agentId);
            return uriStatService.getFailedUriStatAgent(builder.build());
        }
    }

}
