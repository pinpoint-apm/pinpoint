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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapWrap;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.service.MapService;
import com.navercorp.pinpoint.web.util.Limiter;
import com.navercorp.pinpoint.web.util.TimeUtils;
import com.navercorp.pinpoint.web.view.ResponseTimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author emeroad
 * @author netspider
 */
@Controller
public class MapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MapService mapService;

    @Autowired
    private Limiter dateLimit;

    private static final String DEFAULT_SEARCH_DEPTH = "8";
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 8;


    /**
   * Server map data query within from ~ to timeframe
     *
     * @param applicationName
     * @param serviceTypeCode
     * @param from
     * @param to
     * @return
     */
    @RequestMapping(value = "/getServerMapData", method = RequestMethod.GET, params="serviceTypeCode")
    @ResponseBody
    public MapWrap getServerMapData(
                                    @RequestParam("applicationName") String applicationName,
                                    @RequestParam("serviceTypeCode") short serviceTypeCode,
                                    @RequestParam("from") long from,
                                    @RequestParam("to") long to,
                                    @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
                                    @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange) {
        ServiceType serviceType = ServiceType.findServiceType(serviceTypeCode);
        String serviceTypeName = serviceType.name();
        return getServerMapData(applicationName, serviceTypeName, from, to, callerRange, calleeRange);
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
    @RequestMapping(value = "/getServerMapData", method = RequestMethod.GET, params="serviceTypeName")
    @ResponseBody
    public MapWrap getServerMapData(
                                    @RequestParam("applicationName") String applicationName,
                                    @RequestParam("serviceTypeName") String serviceTypeName,
                                    @RequestParam("from") long from,
                                    @RequestParam("to") long to,
                                    @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
                                    @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange) {
        final Range range = new Range(from, to);
        this.dateLimit.limit(from, to);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange);
        assertSearchOption(searchOption);

        logger.info("getServerMap() applicationName:{} range:{} searchOption:{}", applicationName, TimeUnit.MILLISECONDS.toMinutes(range.getRange()), searchOption);

        ServiceType serviceType = ServiceType.valueOf(serviceTypeName);
        Application application = new Application(applicationName, serviceType);

        ApplicationMap map = mapService.selectApplicationMap(application, range, searchOption);

        return new MapWrap(map);
    }

    private void assertSearchOption(SearchOption searchOption) {
        int callerSearchDepth = searchOption.getCalleeSearchDepth();
        assertSearchDepth(callerSearchDepth, "invalid caller depth:" + callerSearchDepth);

        int calleeSearchDepth = searchOption.getCalleeSearchDepth();
        assertSearchDepth(searchOption.getCallerSearchDepth(), "invalid callee depth:" + calleeSearchDepth);
    }

    private void assertSearchDepth(int depth, String message) {
        if (depth < 0) {
            throw new IllegalArgumentException(message);
        }
        if (depth > DEFAULT_MAX_SEARCH_DEPTH) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
   * Server map data query for the last "Period" timeframe
     *
     * @param applicationName
     * @param serviceTypeCode
     * @param period
     * @return
     */
    @RequestMapping(value = "/getLastServerMapData", method = RequestMethod.GET, params="serviceTypeCode")
    @ResponseBody
    public MapWrap getLastServerMapData(
                                        @RequestParam("applicationName") String applicationName,
                                        @RequestParam("serviceTypeCode") short serviceTypeCode,
                                        @RequestParam("period") long period,
                                        @RequestParam(value = "callerRange", defaultValue = "64") int callerRange,
                                        @RequestParam(value = "calleeRange", defaultValue = "64") int calleeRange) {

        long to = TimeUtils.getDelayLastTime();
        long from = to - period;
        return getServerMapData(applicationName, serviceTypeCode, from, to, callerRange, calleeRange);
    }

    /**
   * Server map data query for the last "Period" timeframe
     *
     * @param applicationName
     * @param serviceTypeName
     * @param period
     * @return
     */
    @RequestMapping(value = "/getLastServerMapData", method = RequestMethod.GET, params="serviceTypeName")
    @ResponseBody
    public MapWrap getLastServerMapData(
                                        @RequestParam("applicationName") String applicationName,
                                        @RequestParam("serviceTypeName") String serviceTypeName,
                                        @RequestParam("period") long period,
                                        @RequestParam(value = "callerRange", defaultValue = "64") int callerRange,
                                        @RequestParam(value = "calleeRange", defaultValue = "64") int calleeRange) {

        long to = TimeUtils.getDelayLastTime();
        long from = to - period;
        return getServerMapData(applicationName, serviceTypeName, from, to, callerRange, calleeRange);
    }

    /**
     * Possible deprecation expected when UI change push forward to pick a map first from UI
     * Unfiltered server map request data query
     *
     * @param model
     * @param from
     * @param to
     * @param sourceApplicationName
     * @param sourceServiceType
     * @param targetApplicationName
     * @param targetServiceType
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/linkStatistics", method = RequestMethod.GET, params={"sourceServiceType", "targetServiceType"})
    public String getLinkStatistics(Model model,
                                    @RequestParam("from") long from,
                                    @RequestParam("to") long to,
                                    @RequestParam("sourceApplicationName") String sourceApplicationName,
                                    @RequestParam("sourceServiceType") short sourceServiceType,
                                    @RequestParam("targetApplicationName") String targetApplicationName,
                                    @RequestParam("targetServiceType") short targetServiceType) {
        String sourceServiceTypeName = ServiceType.findServiceType(sourceServiceType).name();
        String targetServiceTypeName = ServiceType.findServiceType(targetServiceType).name();
        return getLinkStatistics(model, from, to, sourceApplicationName, sourceServiceTypeName, targetApplicationName, targetServiceTypeName);
    }

    /**
     * Possible deprecation expected when UI change push forward to pick a map first from UI
     * Unfiltered server map request data query
     *
     * @param model
     * @param from
     * @param to
     * @param sourceApplicationName
     * @param sourceServiceTypeName
     * @param targetApplicationName
     * @param sourceServiceTypeName
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/linkStatistics", method = RequestMethod.GET, params={"sourceServiceTypeName", "targetServiceTypeName"})
    public String getLinkStatistics(Model model,
                                    @RequestParam("from") long from,
                                    @RequestParam("to") long to,
                                    @RequestParam("sourceApplicationName") String sourceApplicationName,
                                    @RequestParam("sourceServiceTypeName") String sourceServiceTypeName,
                                    @RequestParam("targetApplicationName") String targetApplicationName,
                                    @RequestParam("targetServiceTypeName") String targetServiceTypeName) {

    final Application sourceApplication = new Application(sourceApplicationName, ServiceType.valueOf(sourceServiceTypeName));
    final Application destinationApplication = new Application(targetApplicationName, ServiceType.valueOf(targetServiceTypeName));
    final Range range = new Range(from, to);

    NodeHistogram nodeHistogram = mapService.linkStatistics(sourceApplication, destinationApplication, range);

        model.addAttribute("range", range);

    model.addAttribute("sourceApplication", sourceApplication);

    model.addAttribute("targetApplication", destinationApplication);

    Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        model.addAttribute("linkStatistics", applicationHistogram);


    List<ResponseTimeViewModel> applicationTimeSeriesHistogram = nodeHistogram.getApplicationTimeHistogram();
    String applicationTimeSeriesHistogramJson = null;
    try {
        applicationTimeSeriesHistogramJson = MAPPER.writeValueAsString(applicationTimeSeriesHistogram);
    } catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
    }
    model.addAttribute("timeSeriesHistogram", applicationTimeSeriesHistogramJson);

    // looks like we need to specify "from, to" to the result. but data got passed thru as it is.
        model.addAttribute("resultFrom", from);
        model.addAttribute("resultTo", to);


        return "linkStatistics";
    }

    private final static ObjectMapper MAPPER = new ObjectMapper();
}
