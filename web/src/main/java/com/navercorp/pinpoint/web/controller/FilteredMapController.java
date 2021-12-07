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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWrap;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.FilteredMapServiceOption;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.GetMapping;
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
public class FilteredMapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final FilteredMapService filteredMapService;
    private final FilterBuilder<List<SpanBo>> filterBuilder;
    private final ServiceTypeRegistryService registry;

    public FilteredMapController(FilteredMapService filteredMapService, FilterBuilder<List<SpanBo>> filterBuilder, ServiceTypeRegistryService registry) {
        this.filteredMapService = Objects.requireNonNull(filteredMapService, "filteredMapService");
        this.filterBuilder = Objects.requireNonNull(filterBuilder, "filterBuilder");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @GetMapping(value = "/getFilteredServerMapDataMadeOfDotGroup", params = "serviceTypeCode")
    public FilterMapWrap getFilteredServerMapDataMadeOfDotGroup(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("originTo") long originTo,
            @RequestParam("xGroupUnit") int xGroupUnit,
            @RequestParam("yGroupUnit") int yGroupUnit,
            @RequestParam(value = "filter", required = false) String filterText,
            @RequestParam(value = "hint", required = false) String filterHint,
            @RequestParam(value = "limit", required = false, defaultValue = "10000") int limit,
            @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        String serviceTypeName = registry.findServiceType(serviceTypeCode).getName();
        return getFilteredServerMapDataMadeOfDotGroup(applicationName, serviceTypeName, from, to, originTo, xGroupUnit, yGroupUnit, filterText, filterHint, limit, viewVersion, useStatisticsAgentState, useLoadHistogramFormat);
    }

    @GetMapping(value = "/getFilteredServerMapDataMadeOfDotGroup", params = "serviceTypeName")
    public FilterMapWrap getFilteredServerMapDataMadeOfDotGroup(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("originTo") long originTo,
            @RequestParam("xGroupUnit") int xGroupUnit,
            @RequestParam("yGroupUnit") int yGroupUnit,
            @RequestParam(value = "filter", required = false) String filterText,
            @RequestParam(value = "hint", required = false) String filterHint,
            @RequestParam(value = "limit", required = false, defaultValue = "10000") int limit,
            @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        if (xGroupUnit <= 0) {
            throw new IllegalArgumentException("xGroupUnit(" + xGroupUnit + ") must be positive number");
        }
        if (yGroupUnit <= 0) {
            throw new IllegalArgumentException("yGroupUnit(" + yGroupUnit + ") must be positive number");
        }

        limit = LimitUtils.checkRange(limit);
        final Filter<List<SpanBo>> filter = filterBuilder.build(filterText, filterHint);
        final Range range = Range.newRange(from, to);
        final LimitedScanResult<List<TransactionId>> limitedScanResult = filteredMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit);

        final long lastScanTime = limitedScanResult.getLimitedTime();
        // original range: needed for visual chart data sampling
        final Range originalRange = Range.newRange(from, originTo);
        // needed to figure out already scanned ranged
        final Range scannerRange = Range.newRange(lastScanTime, to);
        logger.debug("originalRange:{} scannerRange:{} ", originalRange, scannerRange);
        final FilteredMapServiceOption option = new FilteredMapServiceOption.Builder(limitedScanResult.getScanData(), originalRange, scannerRange, xGroupUnit, yGroupUnit, filter, viewVersion).setUseStatisticsAgentState(useStatisticsAgentState).build();
        final ApplicationMap map = filteredMapService.selectApplicationMapWithScatterData(option);

        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData range scan(limit:{}) range:{} lastFetchedTimestamp:{}", limit, range.prettyToString(), DateTimeFormatUtils.format(lastScanTime));
        }

        FilterMapWrap mapWrap;
        if (useLoadHistogramFormat) {
            mapWrap = new FilterMapWrap(map, TimeHistogramFormat.V2);
        } else {
            mapWrap = new FilterMapWrap(map, TimeHistogramFormat.V1);
        }
        mapWrap.setLastFetchedTimestamp(lastScanTime);
        return mapWrap;
    }
}
