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

package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapViewV3;
import com.navercorp.pinpoint.web.applicationmap.FilterMapViewV3;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWithScatter;
import com.navercorp.pinpoint.web.applicationmap.controller.form.ApplicationForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.FilterForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.GroupForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.service.TraceIndexService;
import com.navercorp.pinpoint.web.applicationmap.view.ScatterDataMapView;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 */
@RestController
@RequestMapping(path = {"/api", "/api/servermap"})
@Validated
public class FilteredMapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final FilteredMapService filteredMapService;
    private final TraceIndexService traceIndexService;
    private final FilterBuilder<List<SpanBo>> filterBuilder;
    private final HyperLinkFactory hyperLinkFactory;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final boolean defaultTraceIndexReadV2;

    public FilteredMapController(
            FilteredMapService filteredMapService,
            TraceIndexService traceIndexService,
            FilterBuilder<List<SpanBo>> filterBuilder,
            HyperLinkFactory hyperLinkFactory,
            ServiceTypeRegistryService serviceTypeRegistryService,
            @Value("${pinpoint.web.trace.index.read.v2:false}") boolean defaultTraceIndexReadV2) {
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.traceIndexService = Objects.requireNonNull(traceIndexService, "traceIndexService");
        this.filterBuilder = Objects.requireNonNull(filterBuilder, "filterBuilder");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.defaultTraceIndexReadV2 = defaultTraceIndexReadV2;
    }

    @GetMapping(value = "/filterServerMap")
    public FilterMapViewV3 getFilterServer(
            @Valid @ModelAttribute
                    ApplicationForm appForm,
            @Valid @ModelAttribute
                    RangeForm rangeForm,
            @RequestParam("originTo") long originTo,
            @Valid @ModelAttribute
                    GroupForm groupForm,
            @Valid @ModelAttribute
                    FilterForm filterForm,
            @RequestParam(value = "limit", required = false, defaultValue = "10000")
            @PositiveOrZero int limitParam,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "true", required = false)
                    boolean useStatisticsAgentState,
            @RequestParam(value = "traceIndexReadV2", required = false) Optional<Boolean> traceIndexReadV2) {
        final String applicationName = appForm.getApplicationName();

        final int limit = Math.min(limitParam, LimitUtils.MAX);
        final Filter<List<SpanBo>> filter = newFilter(filterForm);
        final Range range = toRange(rangeForm);
        final boolean useTraceIndexV2 = traceIndexReadV2.orElse(defaultTraceIndexReadV2);

        final LimitedScanResult<List<TransactionId>> limitedScanResult;
        if (!useTraceIndexV2) {
            limitedScanResult = traceIndexService.getTraceIndex(applicationName, range, limit);
        } else {
            final ServiceType serviceType = findServiceType(appForm.getServiceTypeCode(), appForm.getServiceTypeName());
            limitedScanResult = traceIndexService.getTraceIndexV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceType.getCode(), range, limit);
        }

        final long lastScanTime = limitedScanResult.limitedTime();
        // original range: needed for visual chart data sampling
        final Range originalRange = Range.between(rangeForm.getFrom(), originTo);
        // needed to figure out already scanned ranged
        final Range scannerRange = Range.between(lastScanTime, range.getTo());
        logger.debug("originalRange:{} scannerRange:{} ", originalRange, scannerRange);
        final FilteredMapServiceOption option = newFilteredOption(limitedScanResult.scanData(), originalRange, groupForm, filter, useStatisticsAgentState);
        final FilterMapWithScatter map = filteredMapService.selectApplicationMapWithScatterData(option);

        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData range scan(limit:{}) range:{} lastFetchedTimestamp:{}", limit, range.prettyToString(), DateTimeFormatUtils.format(lastScanTime));
        }

        TimeWindow timeWindow = new TimeWindow(scannerRange);
        ApplicationMapViewV3 applicationMapView = new ApplicationMapViewV3(map.getApplicationMap(), timeWindow, MapViews.ofDetailed(), hyperLinkFactory);
        ScatterDataMapView scatterDataMapView = new ScatterDataMapView(map.getScatterDataMap());
//        FilteredHistogramView filteredHistogramView = new FilteredHistogramView(map.getApplicationMap(), timeWindow, hyperLinkFactory);
        return new FilterMapViewV3(applicationMapView, scatterDataMapView, null, lastScanTime);
    }

    private FilteredMapServiceOption newFilteredOption(List<TransactionId> transactionIdList,
                                                       Range originalRange,
                                                       GroupForm groupForm,
                                                       Filter<List<SpanBo>> filter,
                                                       boolean useStatisticsAgentState) {
        return new FilteredMapServiceOption
                .Builder(transactionIdList, originalRange, groupForm.getXGroupUnit(), groupForm.geYGroupUnit(), filter)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
    }

    private Filter<List<SpanBo>> newFilter(FilterForm filterForm) {
        return filterBuilder.build(filterForm.getFilter(), filterForm.getHint());
    }

    private Range toRange(RangeForm rangeForm) {
        return Range.between(rangeForm.getFrom(), rangeForm.getTo());
    }

    private ServiceType findServiceType(Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            ServiceType serviceTypeFromCode = serviceTypeRegistryService.findServiceType(serviceTypeCode);
            if (serviceTypeFromCode != null && !ServiceType.UNDEFINED.equals(serviceTypeFromCode)) {
                return serviceTypeFromCode;
            }
        }
        if (serviceTypeName != null) {
            ServiceType serviceTypeFromName = serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
            if (serviceTypeFromName != null && !ServiceType.UNDEFINED.equals(serviceTypeFromName)) {
                return serviceTypeFromName;
            }
        }
        throw new IllegalArgumentException("application serviceType not found. code:" + serviceTypeCode + ", name:" + serviceTypeName);
    }

}
