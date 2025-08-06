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

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapView;
import com.navercorp.pinpoint.web.applicationmap.MapViewV3;
import com.navercorp.pinpoint.web.applicationmap.controller.form.ApplicationForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.SearchOptionForm;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final HyperLinkFactory hyperLinkFactory;
    private final ApplicationValidator applicationValidator;

    private static final int DEFAULT_MAX_SEARCH_DEPTH = 4;

    public MapController(
            MapService mapService,
            ApplicationValidator applicationValidator,
            HyperLinkFactory hyperLinkFactory,
            Duration limitDay) {
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.applicationValidator = Objects.requireNonNull(applicationValidator, "applicationValidator");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.rangeValidator = new ForwardRangeValidator(Objects.requireNonNull(limitDay, "limitDay"));
    }

    private SearchOption.Builder searchOptionBuilder() {
        return SearchOption.newBuilder(DEFAULT_MAX_SEARCH_DEPTH);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param appForm   applicationForm
     * @param rangeForm rangeForm
     * @return MapWrap
     */
    @GetMapping(value = "/serverMap")
    public MapViewV3 getServerMapData(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        final TimeWindow timeWindow = newTimeWindow(rangeForm);

        final SearchOption searchOption = searchOptionBuilder()
                .build(searchForm.getCallerRange(), searchForm.getCalleeRange(), searchForm.isBidirectional(), searchForm.isWasOnly());

        final Application application = getApplication(appForm);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, timeWindow, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        logger.info("Select applicationMap {}. option={}", TimeHistogramFormat.V3, option);
        final ApplicationMap map = this.mapService.selectApplicationMap(option);

        return new MapViewV3(map, timeWindow, MapViews.ofBasic(), hyperLinkFactory);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param appForm   applicationForm
     * @param rangeForm rangeForm
     * @return MapWrap
     */
    @GetMapping(value = "/getServerMapDataV2")
    public MapView getServerMapDataV2(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        final TimeWindow timeWindow = newTimeWindow(rangeForm);

        final SearchOption searchOption = searchOptionBuilder()
                .build(searchForm.getCallerRange(), searchForm.getCalleeRange(), searchForm.isBidirectional(), searchForm.isWasOnly());
        final Application application = getApplication(appForm);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, timeWindow, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        TimeHistogramFormat format = TimeHistogramFormat.V1;
        logger.info("Select ApplicationMap {} option={}", format, option);
        final ApplicationMap map = this.mapService.selectApplicationMap(option);

        return new MapView(map, MapViews.ofBasic(), hyperLinkFactory, format);

    }

    @GetMapping(value = "/simpleServerMapData")
    public MapView getSimpleServerMapData(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState) {
        final TimeWindow timeWindow = newTimeWindow(rangeForm);

        final Application application = getApplication(appForm);
        SearchOption searchOption = searchOptionBuilder()
                .build(searchForm.getCallerRange(), searchForm.getCalleeRange(), searchForm.isBidirectional(), searchForm.isWasOnly());

        final MapServiceOption option = new MapServiceOption
                .Builder(application, timeWindow, searchOption)
                .setSimpleResponseHistogram(true)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        logger.info("Select simpleApplicationMap. option={}", option);
        final ApplicationMap map = this.mapService.selectApplicationMap(option);

        return new MapView(map, MapViews.ofSimpled(), hyperLinkFactory, TimeHistogramFormat.V3);
    }

    private Application getApplication(ApplicationForm appForm) {
        return applicationValidator.newApplication(appForm.getApplicationName(), appForm.getServiceTypeCode(), appForm.getServiceTypeName());
    }

    private TimeWindow newTimeWindow(RangeForm rangeForm) {
        Range between = Range.between(rangeForm.getFrom(), rangeForm.getTo());
        this.rangeValidator.validate(between);
        return new TimeWindow(between);
    }
}
