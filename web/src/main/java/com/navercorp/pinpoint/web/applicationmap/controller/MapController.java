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

import com.fasterxml.jackson.annotation.JsonView;
import com.navercorp.pinpoint.common.server.util.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapWrap;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.NodeHistogramSummaryView;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.view.LinkHistogramSummaryView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import com.navercorp.pinpoint.web.vo.SearchOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping("/api")
@Validated
public class MapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapService mapService;
    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final RangeValidator rangeValidator;
    private final ApplicationFactory applicationFactory;

    private static final String DEFAULT_SEARCH_DEPTH = "4";
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 4;

    public MapController(
            MapService mapService,
            ResponseTimeHistogramService responseTimeHistogramService,
            ApplicationFactory applicationFactory,
            ConfigProperties configProperties
    ) {
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        Objects.requireNonNull(configProperties, "configProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(configProperties.getServerMapPeriodMax()));
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
    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeCode")
    @JsonView(MapViews.Basic.class)
    public MapWrap getServerMapDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH)
            @Positive int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH)
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

        return selectApplicationMap(application, option, useLoadHistogramFormat);
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
    @JsonView(MapViews.Basic.class)
    public MapWrap getServerMapDataV2(
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

        return selectApplicationMap(application, option, useLoadHistogramFormat);
    }

    private MapWrap selectApplicationMap(
            Application application,
            MapServiceOption mapServiceOption,
            boolean useLoadHistogramFormat
    ) {
        Objects.requireNonNull(application, "application");
        Objects.requireNonNull(mapServiceOption, "mapServiceOption");

        logger.info("Select applicationMap. option={}", mapServiceOption);
        final ApplicationMap map = this.mapService.selectApplicationMap(mapServiceOption);

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return new MapWrap(map, format);
    }

    @GetMapping(value = "/getResponseTimeHistogramData", params = "serviceTypeName")
    public ApplicationTimeHistogramViewModel getResponseTimeHistogramData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final Application application =
                applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        AgentHistogramList responseTimes = responseTimeHistogramService.selectResponseTimeHistogramData(application, range);
        return new ApplicationTimeHistogramViewModel(application, responseTimes);
    }

    @PostMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummaryView postResponseTimeHistogramDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestBody ApplicationPairs applicationPairs,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "true", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        final List<Application> fromApplications =
                mapApplicationPairsToApplications(applicationPairs.getFromApplications());
        final List<Application> toApplications =
                mapApplicationPairsToApplications(applicationPairs.getToApplications());
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption
                .Builder(application, range, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return new NodeHistogramSummaryView(nodeHistogramSummary, nodeHistogramSummary.getServerGroupList(), format);
    }


    @GetMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummaryView getResponseTimeHistogramDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "fromApplicationNames", defaultValue = "", required = false)
            List<String> fromApplicationNames,
            @RequestParam(value = "fromServiceTypeCodes", defaultValue = "", required = false)
            List<Short> fromServiceTypeCodes,
            @RequestParam(value = "toApplicationNames", defaultValue = "", required = false)
            List<String> toApplicationNames,
            @RequestParam(value = "toServiceTypeCodes", defaultValue = "", required = false)
            List<Short> toServiceTypeCodes,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "true", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        if (fromApplicationNames.size() != fromServiceTypeCodes.size()) {
            throw new IllegalArgumentException(
                    "fromApplicationNames and fromServiceTypeCodes must have the same number of elements");
        }
        if (toApplicationNames.size() != toServiceTypeCodes.size()) {
            throw new IllegalArgumentException(
                    "toApplicationNames and toServiceTypeCodes must have the same number of elements");
        }

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        final List<Application> fromApplications = toApplications(fromApplicationNames, fromServiceTypeCodes);
        final List<Application> toApplications = toApplications(toApplicationNames, toServiceTypeCodes);
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption
                .Builder(application, range, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        final TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return new NodeHistogramSummaryView(nodeHistogramSummary, nodeHistogramSummary.getServerGroupList(), format);
    }

    private List<Application> toApplications(List<String> applicationNames, List<Short> serviceTypeCodes) {
        final List<Application> result = new ArrayList<>(applicationNames.size());
        for (int i = 0; i < applicationNames.size(); i++) {
            final Application application =
                    this.applicationFactory.createApplication(applicationNames.get(i), serviceTypeCodes.get(i));
            result.add(application);
        }
        return result;
    }

    private List<Application> mapApplicationPairsToApplications(List<ApplicationPair> applicationPairs) {
        if (CollectionUtils.isEmpty(applicationPairs)) {
            return Collections.emptyList();
        }
        final List<Application> applications = new ArrayList<>(applicationPairs.size());
        for (ApplicationPair applicationPair : applicationPairs) {
            final String applicationName = applicationPair.getApplicationName();
            final short serviceTypeCode = applicationPair.getServiceTypeCode();
            final Application application = this.applicationFactory.createApplication(applicationName, serviceTypeCode);
            applications.add(application);
        }
        return applications;
    }

    @GetMapping(value = "/getLinkTimeHistogramData")
    public LinkHistogramSummaryView getLinkTimeHistogramData(
            @RequestParam(value = "fromApplicationName", required = false) @NullOrNotBlank String fromApplicationName,
            @RequestParam(value = "fromServiceTypeCode", required = false) Short fromServiceTypeCode,
            @RequestParam(value = "toApplicationName", required = false) @NullOrNotBlank String toApplicationName,
            @RequestParam(value = "toServiceTypeCode", required = false) Short toServiceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        final Application fromApplication = this.createApplication(fromApplicationName, fromServiceTypeCode);
        final Application toApplication = this.createApplication(toApplicationName, toServiceTypeCode);
        final LinkHistogramSummary linkHistogramSummary =
                responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return new LinkHistogramSummaryView(linkHistogramSummary, format);
    }

    @Nullable
    private Application createApplication(@Nullable String name, Short serviceTypeCode) {
        if (name == null) {
            return null;
        }
        return this.applicationFactory.createApplication(name, serviceTypeCode);
    }

    @GetMapping(value = "/getServerMapDataV3", params = "serviceTypeCode")
    @JsonView({MapViews.Simplified.class})
    public MapWrap getServerMapDataV3(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
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

        return selectApplicationMap(application, mapServiceOption, false);
    }

    @GetMapping(value = "/getServerMapDataV3", params = "serviceTypeName")
    @JsonView({MapViews.Simplified.class})
    public MapWrap getServerMapDataV3(
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

        return selectApplicationMap(application, mapServiceOption, false);
    }
}
