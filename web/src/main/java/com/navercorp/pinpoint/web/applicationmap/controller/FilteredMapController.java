/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.FilterMapView;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWithScatter;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.FilteredHistogramView;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 */
@RestController
@RequestMapping(path = { "/api", "/api/servermap"})
@Validated
public class FilteredMapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final FilteredMapService filteredMapService;
    private final FilterBuilder<List<SpanBo>> filterBuilder;
    private final HyperLinkFactory hyperLinkFactory;

    public FilteredMapController(
            FilteredMapService filteredMapService,
            FilterBuilder<List<SpanBo>> filterBuilder,
            HyperLinkFactory hyperLinkFactory
    ) {
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.filterBuilder = Objects.requireNonNull(filterBuilder, "filterBuilder");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }


    @GetMapping(value = "/getFilteredServerMapDataMadeOfDotGroup")
    public FilterMapView getFilteredServerMapDataMadeOfDotGroup(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @RequestParam("originTo") long originTo,
            @RequestParam("xGroupUnit") @Positive int xGroupUnit,
            @RequestParam("yGroupUnit") @Positive int yGroupUnit,
            @RequestParam(value = "filter", required = false) @NullOrNotBlank String filterText,
            @RequestParam(value = "hint", required = false) @NullOrNotBlank String filterHint,
            @RequestParam(value = "limit", required = false, defaultValue = "10000") @PositiveOrZero int limitParam,
            @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final String applicationName = appForm.getApplicationName();

        final int limit = Math.min(limitParam, LimitUtils.MAX);
        final Filter<List<SpanBo>> filter = filterBuilder.build(filterText, filterHint);
        final Range range = toRange(rangeForm);
        final LimitedScanResult<List<TransactionId>> limitedScanResult =
                filteredMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit);

        final long lastScanTime = limitedScanResult.limitedTime();
        // original range: needed for visual chart data sampling
        final Range originalRange = Range.between(rangeForm.getFrom(), originTo);
        // needed to figure out already scanned ranged
        final Range scannerRange = Range.between(lastScanTime, rangeForm.getTo());
        logger.debug("originalRange: {}, scannerRange: {}", originalRange, scannerRange);
        final FilteredMapServiceOption option = new FilteredMapServiceOption
                .Builder(limitedScanResult.scanData(), originalRange, xGroupUnit, yGroupUnit, filter, viewVersion)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final FilterMapWithScatter scatter = filteredMapService.selectApplicationMapWithScatterData(option);
        ApplicationMap map = scatter.getApplicationMap();

        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData range scan(limit:{}) range:{} lastFetchedTimestamp:{}",
                    limit, range.prettyToString(), DateTimeFormatUtils.format(lastScanTime));
        }

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        final FilterMapView mapWrap = new FilterMapView(map, MapViews.ofDetailed(), hyperLinkFactory, format);
        mapWrap.setLastFetchedTimestamp(lastScanTime);
        mapWrap.setScatterDataMap(scatter.getScatterDataMap());
        return mapWrap;
    }


    @GetMapping(value = "/getFilteredServerMapDataMadeOfDotGroupV3")
    public FilterMapView getFilteredServerMapDataMadeOfDotGroupV3(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @RequestParam("originTo") long originTo,
            @RequestParam("xGroupUnit") @Positive int xGroupUnit,
            @RequestParam("yGroupUnit") @Positive int yGroupUnit,
            @RequestParam(value = "filter", required = false) @NullOrNotBlank String filterText,
            @RequestParam(value = "hint", required = false) @NullOrNotBlank String filterHint,
            @RequestParam(value = "limit", required = false, defaultValue = "10000") @PositiveOrZero int limitParam,
            @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState) {
        final String applicationName = appForm.getApplicationName();

        final int limit = Math.min(limitParam, LimitUtils.MAX);
        final Filter<List<SpanBo>> filter = filterBuilder.build(filterText, filterHint);
        final Range range = toRange(rangeForm);
        final LimitedScanResult<List<TransactionId>> limitedScanResult = filteredMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit);

        final long lastScanTime = limitedScanResult.limitedTime();
        // original range: needed for visual chart data sampling
        final Range originalRange = Range.between(rangeForm.getFrom(), originTo);
        // needed to figure out already scanned ranged
        final Range scannerRange = Range.between(lastScanTime, range.getTo());
        logger.debug("originalRange:{} scannerRange:{} ", originalRange, scannerRange);
        final FilteredMapServiceOption option = new FilteredMapServiceOption
                .Builder(limitedScanResult.scanData(), originalRange, xGroupUnit, yGroupUnit, filter, viewVersion)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final FilterMapWithScatter map = filteredMapService.selectApplicationMapWithScatterData(option);

        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData range scan(limit:{}) range:{} lastFetchedTimestamp:{}", limit, range.prettyToString(), DateTimeFormatUtils.format(lastScanTime));
        }

        FilterMapView mapWrap = new FilterMapView(map.getApplicationMap(), MapViews.ofSimpled(), hyperLinkFactory, TimeHistogramFormat.V1);
        mapWrap.setLastFetchedTimestamp(lastScanTime);
        mapWrap.setFilteredHistogram(this::filteredHistogramView);
        mapWrap.setScatterDataMap(map.getScatterDataMap());
        return mapWrap;
    }

    private Range toRange(RangeForm rangeForm) {
        return Range.between(rangeForm.getFrom(), rangeForm.getTo());
    }

    private FilteredHistogramView filteredHistogramView(ApplicationMap applicationMap) {
        return new FilteredHistogramView(applicationMap, hyperLinkFactory);
    }
}
