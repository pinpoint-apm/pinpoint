/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapWrap;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.MapService;
import com.navercorp.pinpoint.web.service.MapServiceOption;
import com.navercorp.pinpoint.web.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.util.Limiter;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
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
@Validated
public class MapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapService mapService;
    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final Limiter dateLimit;
    private final ApplicationFactory applicationFactory;

    private static final String DEFAULT_SEARCH_DEPTH = "8";
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 8;

    public MapController(
            MapService mapService,
            ResponseTimeHistogramService responseTimeHistogramService,
            Limiter dateLimit,
            ApplicationFactory applicationFactory
    ) {
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.dateLimit = Objects.requireNonNull(dateLimit, "dateLimit");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeCode serviceTypeCode
     * @param from from (timestamp)
     * @param to to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/getServerMapData", params = "serviceTypeCode")
    public MapWrap getServerMapData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly) {
        final Range range = Range.between(from, to);
        this.dateLimit.limit(range);

        final SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return selectApplicationMap(
                application, range, searchOption, NodeType.DETAILED, LinkType.DETAILED, false, false
        );
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeName serviceTypeName
     * @param from from (timestamp)
     * @param to to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/getServerMapData", params = "serviceTypeName")
    public MapWrap getServerMapData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly) {
        final Range range = Range.between(from, to);
        this.dateLimit.limit(range);

        final SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        final Application application =
                applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return selectApplicationMap(
                application, range, searchOption, NodeType.DETAILED, LinkType.DETAILED, false, false
        );
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeCode serviceTypeCode
     * @param from from (timestamp)
     * @param to to (timestamp)
     * @return MapWrap
     */
    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeCode")
    public MapWrap getServerMapDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.dateLimit.limit(range);

        final SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return selectApplicationMap(
                application,
                range,
                searchOption,
                NodeType.BASIC,
                LinkType.BASIC,
                useStatisticsAgentState,
                useLoadHistogramFormat
        );
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName applicationName
     * @param serviceTypeName serviceTypeName
     * @param from from (timestamp)
     * @param to to (timestamp)
     * @return MapWrap
     */

    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeName")
    public MapWrap getServerMapDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        this.dateLimit.limit(range);

        final SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        final Application application =
                applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        return selectApplicationMap(
                application,
                range,
                searchOption,
                NodeType.BASIC,
                LinkType.BASIC,
                useStatisticsAgentState,
                useLoadHistogramFormat
        );
    }

    private MapWrap selectApplicationMap(
            Application application,
            Range range,
            SearchOption searchOption,
            NodeType nodeType,
            LinkType linkType,
            boolean useStatisticsAgentState,
            boolean useLoadHistogramFormat
    ) {
        Objects.requireNonNull(application, "application");
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(searchOption, "searchOption");

        final MapServiceOption mapServiceOption = new MapServiceOption.Builder(
                application, range, searchOption, nodeType, linkType
        ).setUseStatisticsAgentState(useStatisticsAgentState).build();
        logger.info("Select applicationMap. option={}", mapServiceOption);
        final ApplicationMap map = this.mapService.selectApplicationMap(mapServiceOption);

        if (useLoadHistogramFormat) {
            return new MapWrap(map, TimeHistogramFormat.V2);
        }
        return new MapWrap(map, TimeHistogramFormat.V1);
    }

    private static void assertSearchOption(SearchOption searchOption) {
        final int callerSearchDepth = searchOption.getCallerSearchDepth();
        assertSearchDepth(callerSearchDepth, "invalid caller depth: " + callerSearchDepth);

        final int calleeSearchDepth = searchOption.getCalleeSearchDepth();
        assertSearchDepth(calleeSearchDepth, "invalid callee depth: " + calleeSearchDepth);
    }

    private static void assertSearchDepth(int depth, String message) {
        if (depth < 0) {
            throw new IllegalArgumentException(message);
        }
        if (depth > DEFAULT_MAX_SEARCH_DEPTH) {
            throw new IllegalArgumentException(message);
        }
    }

    @GetMapping(value = "/getResponseTimeHistogramData", params = "serviceTypeName")
    public ApplicationTimeHistogramViewModel getResponseTimeHistogramData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);

        final Application application =
                applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return responseTimeHistogramService.selectResponseTimeHistogramData(application, range);
    }

    @PostMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummary postResponseTimeHistogramDataV2(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestBody ApplicationPairs applicationPairs,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);

        final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        final List<Application> fromApplications =
                mapApplicationPairsToApplications(applicationPairs.getFromApplications());
        final List<Application> toApplications =
                mapApplicationPairsToApplications(applicationPairs.getToApplications());
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(
                application, range, fromApplications, toApplications
        ).setUseStatisticsAgentState(useStatisticsAgentState).build();
        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        if (useLoadHistogramFormat) {
            nodeHistogramSummary.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return nodeHistogramSummary;
    }

    @GetMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummary getResponseTimeHistogramDataV2(
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
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);

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
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(
                application, range, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        if (useLoadHistogramFormat) {
            nodeHistogramSummary.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return nodeHistogramSummary;
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
    public LinkHistogramSummary getLinkTimeHistogramData(
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
        dateLimit.limit(range);

        final Application fromApplication = this.createApplication(fromApplicationName, fromServiceTypeCode);
        final Application toApplication = this.createApplication(toApplicationName, toServiceTypeCode);
        final LinkHistogramSummary linkHistogramSummary =
                responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);
        if (useLoadHistogramFormat) {
            linkHistogramSummary.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return linkHistogramSummary;
    }

    @Nullable
    private Application createApplication(@Nullable String name, Short serviceTypeCode) {
        if (name == null) {
            return null;
        }
        return this.applicationFactory.createApplication(name, serviceTypeCode);
    }

    @GetMapping(value = "/getServerMapDataV3", params = "serviceTypeCode")
    public MapWrap getServerMapDataV3(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState) {
        final Range range = Range.between(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        MapWrap mapWrap = selectApplicationMap(application, range, searchOption, NodeType.BASIC, LinkType.BASIC, useStatisticsAgentState, true);
        mapWrap.setV3Format(true);
        return mapWrap;
    }

    @GetMapping(value = "/getServerMapDataV3", params = "serviceTypeName")
    public MapWrap getServerMapDataV3(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        final Range range = Range.between(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        MapWrap mapWrap = selectApplicationMap(application, range, searchOption, NodeType.BASIC, LinkType.BASIC, useStatisticsAgentState, true);
        mapWrap.setV3Format(true);
        return mapWrap;
    }
}
