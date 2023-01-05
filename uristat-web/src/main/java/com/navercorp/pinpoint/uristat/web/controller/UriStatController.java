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

import com.navercorp.pinpoint.common.server.tenant.TenantProvider;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.uristat.common.model.UriStat;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.service.UriStatService;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
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

    @GetMapping("top50")
    public List<UriStatSummary> getUriStatSummary(@RequestParam("applicationName") String applicationName,
                                            @RequestParam(value = "agentId", required = false) String agentId,
                                            @RequestParam("from") long from,
                                            @RequestParam("to") long to) {
        UriStatQueryParameter.Builder builder = new UriStatQueryParameter.Builder();
        builder.setTenantId(tenantProvider.getTenantId());
        builder.setApplicationName(applicationName);
        builder.setRange(Range.newRange(from, to));

        if (StringUtils.isEmpty(agentId)) {
            return uriStatService.getUriStatApplicationSummary(builder.build());
        } else {
            builder.setAgentId(agentId);
            return uriStatService.getUriStatAgentSummary(builder.build());
        }
    }

    @GetMapping("chart")
    public UriStatView getCollectedUriStat(@RequestParam("applicationName") String applicationName,
                                           @RequestParam(value = "agentId", required = false) String agentId,
                                           @RequestParam("uri") String uri,
                                           @RequestParam("from") long from,
                                           @RequestParam("to") long to) {
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        UriStatQueryParameter.Builder builder = new UriStatQueryParameter.Builder();
        builder.setTenantId(tenantProvider.getTenantId());
        builder.setApplicationName(applicationName);
        builder.setUri(uri);
        builder.setRange(timeWindow.getWindowRange());
        builder.setTimeSize((int) timeWindow.getWindowSlotSize());
        builder.setTimePrecision(TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize()));

        List<UriStat> uriStats;
        if (StringUtils.isEmpty(agentId)) {
            uriStats = uriStatService.getCollectedUriStatApplication(builder.build());
        } else {
            builder.setAgentId(agentId);
            uriStats = uriStatService.getCollectedUriStatAgent(builder.build());
        }

        return new UriStatView(uri, timeWindow, uriStats);
    }
}
