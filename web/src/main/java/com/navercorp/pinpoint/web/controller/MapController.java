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

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapWrap;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.MapService;
import com.navercorp.pinpoint.web.service.MapServiceOption;
import com.navercorp.pinpoint.web.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.util.Limiter;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class MapController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MapService mapService;
    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final Limiter dateLimit;
    private final ApplicationFactory applicationFactory;

    private static final String DEFAULT_SEARCH_DEPTH = "8";
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 8;

    public MapController(MapService mapService, ResponseTimeHistogramService responseTimeHistogramService,
                         Limiter dateLimit, ApplicationFactory applicationFactory) {
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.responseTimeHistogramService = Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.dateLimit = Objects.requireNonNull(dateLimit, "dateLimit");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName
     * @param serviceTypeCode
     * @param from
     * @param to
     * @return
     */
    @GetMapping(value = "/getServerMapData", params = "serviceTypeCode")
    public MapWrap getServerMapData(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly) {
        final Range range = Range.newRange(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return selectApplicationMap(application, range, searchOption, NodeType.DETAILED, LinkType.DETAILED, false, false);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName
     * @param serviceTypeName
     * @param from
     * @param to
     * @return
     */
    @GetMapping(value = "/getServerMapData", params = "serviceTypeName")
    public MapWrap getServerMapData(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly) {
        final Range range = Range.newRange(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return selectApplicationMap(application, range, searchOption, NodeType.DETAILED, LinkType.DETAILED, false, false);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName
     * @param serviceTypeCode
     * @param from
     * @param to
     * @return
     */
    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeCode")
    public MapWrap getServerMapDataV2(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        final Range range = Range.newRange(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return selectApplicationMap(application, range, searchOption, NodeType.BASIC, LinkType.BASIC, useStatisticsAgentState, useLoadHistogramFormat);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param applicationName
     * @param serviceTypeName
     * @param from
     * @param to
     * @return
     */

    @GetMapping(value = "/getServerMapDataV2", params = "serviceTypeName")
    public MapWrap getServerMapDataV2(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
            @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        final Range range = Range.newRange(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange, bidirectional, wasOnly);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        return selectApplicationMap(application, range, searchOption, NodeType.BASIC, LinkType.BASIC, useStatisticsAgentState, useLoadHistogramFormat);
    }

    private MapWrap selectApplicationMap(Application application, Range range, SearchOption searchOption, NodeType nodeType, LinkType linkType, boolean useStatisticsAgentState, boolean useLoadHistogramFormat) {
        Objects.requireNonNull(application, "application");
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(searchOption, "searchOption");

        final MapServiceOption mapServiceOption = new MapServiceOption.Builder(application, range, searchOption, nodeType, linkType).setUseStatisticsAgentState(useStatisticsAgentState).build();
        logger.info("Select applicationMap. option={}", mapServiceOption);
        final ApplicationMap map = mapService.selectApplicationMap(mapServiceOption);

        if (useLoadHistogramFormat) {
            return new MapWrap(map, TimeHistogramFormat.V2);
        }
        return new MapWrap(map, TimeHistogramFormat.V1);
    }

    private void assertSearchOption(SearchOption searchOption) {
        int callerSearchDepth = searchOption.getCallerSearchDepth();
        assertSearchDepth(callerSearchDepth, "invalid caller depth:" + callerSearchDepth);

        int calleeSearchDepth = searchOption.getCalleeSearchDepth();
        assertSearchDepth(calleeSearchDepth, "invalid callee depth:" + calleeSearchDepth);
    }

    private void assertSearchDepth(int depth, String message) {
        if (depth < 0) {
            throw new IllegalArgumentException(message);
        }
        if (depth > DEFAULT_MAX_SEARCH_DEPTH) {
            throw new IllegalArgumentException(message);
        }
    }

    @GetMapping(value = "/getResponseTimeHistogramData", params = "serviceTypeName")
    public ApplicationTimeHistogramViewModel getResponseTimeHistogramData(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.newRange(from, to);
        dateLimit.limit(range);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        ApplicationTimeHistogramViewModel applicationTimeHistogramViewModel = responseTimeHistogramService.selectResponseTimeHistogramData(application, range);

        return applicationTimeHistogramViewModel;
    }

    @PostMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummary postResponseTimeHistogramDataV2(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestBody ApplicationPairs applicationPairs,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        final Range range = Range.newRange(from, to);
        dateLimit.limit(range);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        List<Application> fromApplications = mapApplicationPairsToApplications(applicationPairs.getFromApplications());
        List<Application> toApplications = mapApplicationPairsToApplications(applicationPairs.getToApplications());
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(application, range, fromApplications, toApplications).setUseStatisticsAgentState(useStatisticsAgentState).build();
        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        if (useLoadHistogramFormat) {
            nodeHistogramSummary.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return nodeHistogramSummary;
    }

    @GetMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummary getResponseTimeHistogramDataV2(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "fromApplicationNames", defaultValue = "", required = false) List<String> fromApplicationNames,
            @RequestParam(value = "fromServiceTypeCodes", defaultValue = "", required = false) List<Short> fromServiceTypeCodes,
            @RequestParam(value = "toApplicationNames", defaultValue = "", required = false) List<String> toApplicationNames,
            @RequestParam(value = "toServiceTypeCodes", defaultValue = "", required = false) List<Short> toServiceTypeCodes,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false) boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        final Range range = Range.newRange(from, to);
        dateLimit.limit(range);

        if (fromApplicationNames.size() != fromServiceTypeCodes.size()) {
            throw new IllegalArgumentException("fromApplicationNames and fromServiceTypeCodes must have the same number of elements");
        }
        if (toApplicationNames.size() != toServiceTypeCodes.size()) {
            throw new IllegalArgumentException("toApplicationNames and toServiceTypeCodes must have the same number of elements");
        }

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        List<Application> fromApplications = new ArrayList<>(fromApplicationNames.size());
        for (int i = 0; i < fromApplicationNames.size(); i++) {
            Application fromApplication = applicationFactory.createApplication(fromApplicationNames.get(i), fromServiceTypeCodes.get(i));
            fromApplications.add(fromApplication);
        }
        List<Application> toApplications = new ArrayList<>(toApplicationNames.size());
        for (int i = 0; i < toApplicationNames.size(); i++) {
            Application toApplication = applicationFactory.createApplication(toApplicationNames.get(i), toServiceTypeCodes.get(i));
            toApplications.add(toApplication);
        }
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(application, range, fromApplications, toApplications).setUseStatisticsAgentState(useStatisticsAgentState).build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        if (useLoadHistogramFormat) {
            nodeHistogramSummary.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return nodeHistogramSummary;
    }

    private List<Application> mapApplicationPairsToApplications(List<ApplicationPair> applicationPairs) {
        if (CollectionUtils.isEmpty(applicationPairs)) {
            return Collections.emptyList();
        }
        List<Application> applications = new ArrayList<>(applicationPairs.size());
        for (ApplicationPair applicationPair : applicationPairs) {
            String applicationName = applicationPair.getApplicationName();
            short serviceTypeCode = applicationPair.getServiceTypeCode();
            Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);
            applications.add(application);
        }
        return applications;
    }

    @GetMapping(value = "/getLinkTimeHistogramData")
    public LinkHistogramSummary getLinkTimeHistogramData(
            @RequestParam(value = "fromApplicationName", required = false) String fromApplicationName,
            @RequestParam(value = "fromServiceTypeCode", required = false) Short fromServiceTypeCode,
            @RequestParam(value = "toApplicationName", required = false) String toApplicationName,
            @RequestParam(value = "toServiceTypeCode", required = false) Short toServiceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false) boolean useLoadHistogramFormat) {
        final Range range = Range.newRange(from, to);
        dateLimit.limit(range);

        Application fromApplication = null;
        if (StringUtils.hasLength(fromApplicationName)) {
            fromApplication = applicationFactory.createApplication(fromApplicationName, fromServiceTypeCode);
        }

        Application toApplication = null;
        if (StringUtils.hasLength(toApplicationName)) {
            toApplication = applicationFactory.createApplication(toApplicationName, toServiceTypeCode);
        }

        LinkHistogramSummary linkHistogramSummary = responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);
        if (useLoadHistogramFormat) {
            linkHistogramSummary.setTimeHistogramFormat(TimeHistogramFormat.V2);
        }
        return linkHistogramSummary;
    }
}
