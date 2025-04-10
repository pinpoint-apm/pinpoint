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

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapView;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping(path = {"/api", "/api/servermap"})
@Validated
public class MapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapService mapService;
    private final RangeValidator rangeValidator;
    private final ApplicationFactory applicationFactory;

    private static final String DEFAULT_SEARCH_DEPTH = "1";
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 4;

    public MapController(
            MapService mapService,
            ApplicationFactory applicationFactory,
            Duration limitDay
    ) {
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.rangeValidator = new ForwardRangeValidator(Objects.requireNonNull(limitDay, "limitDay"));
    }

    private SearchOption.Builder searchOptionBuilder() {
        return SearchOption.newBuilder(DEFAULT_MAX_SEARCH_DEPTH);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeCode serviceTypeCode
     * @param from            from (timestamp)
     * @param to              to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/serverMap", params = "serviceTypeCode")
    public MapView getServerMapData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") int serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final SearchOption searchOption = searchOptionBuilder().build(callerRange, calleeRange, bidirectional, wasOnly);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, range, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        logger.info("Select applicationMap. option={}", option);
        final ApplicationMap map = this.mapService.selectApplicationMap(option);
        TimeWindow timeWindow = new TimeWindow(range);
        return new MapView(map, timeWindow, MapViews.Basic.class, TimeHistogramFormat.V3);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeName serviceTypeName
     * @param from            from (timestamp)
     * @param to              to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/serverMap", params = "serviceTypeName")
    public MapView getServerMapData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final SearchOption searchOption = searchOptionBuilder().build(callerRange, calleeRange, bidirectional, wasOnly);

        final Application application =
                applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, range, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        logger.info("Select applicationMap. option={}", option);
        final ApplicationMap map = this.mapService.selectApplicationMap(option);
        TimeWindow timeWindow = new TimeWindow(range);
        return new MapView(map, timeWindow, MapViews.Basic.class, TimeHistogramFormat.V3);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeCode serviceTypeCode
     * @param from            from (timestamp)
     * @param to              to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeCode")
    public MapView getServerMapDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") int serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final SearchOption searchOption = searchOptionBuilder().build(callerRange, calleeRange, bidirectional, wasOnly);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, range, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return selectApplicationMap(application, option, MapViews.Basic.class, format);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeName serviceTypeName
     * @param from            from (timestamp)
     * @param to              to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeName")
    public MapView getServerMapDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH, required = false)
            @Positive int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final SearchOption searchOption = searchOptionBuilder().build(callerRange, calleeRange, bidirectional, wasOnly);

        final Application application =
                applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, range, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return selectApplicationMap(application, option, MapViews.Basic.class, format);
    }

    private MapView selectApplicationMap(
            Application application,
            MapServiceOption mapServiceOption,
            Class<?> activeView,
            TimeHistogramFormat format
    ) {
        Objects.requireNonNull(application, "application");
        Objects.requireNonNull(mapServiceOption, "mapServiceOption");

        logger.info("Select applicationMap. option={}", mapServiceOption);
        final ApplicationMap map = this.mapService.selectApplicationMap(mapServiceOption);

        return new MapView(map, activeView, format);
    }

    @GetMapping(value = "/getSimpleServerMapData", params = "serviceTypeCode")
    public MapView getSimpleServerMapData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") int serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH)
            @Positive int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);
        SearchOption searchOption = searchOptionBuilder().build(callerRange, calleeRange, bidirectional, wasOnly);

        final MapServiceOption mapServiceOption = new MapServiceOption
                .Builder(application, range, searchOption)
                .setSimpleResponseHistogram(true)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        return selectApplicationMap(application, mapServiceOption, MapViews.Simplified.class, TimeHistogramFormat.V2);
    }

    @GetMapping(value = "/getSimpleServerMapData", params = "serviceTypeName")
    public MapView getSimpleServerMapData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH)
            @Positive int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        SearchOption searchOption = searchOptionBuilder().build(callerRange, calleeRange, bidirectional, wasOnly);

        final MapServiceOption mapServiceOption = new MapServiceOption
                .Builder(application, range, searchOption)
                .setSimpleResponseHistogram(true)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        return selectApplicationMap(application, mapServiceOption, MapViews.Simplified.class, TimeHistogramFormat.V2);
    }
}
