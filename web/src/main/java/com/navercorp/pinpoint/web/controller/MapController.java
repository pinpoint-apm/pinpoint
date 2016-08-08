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

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapWrap;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.MapService;
import com.navercorp.pinpoint.web.util.Limiter;
import com.navercorp.pinpoint.web.util.TimeUtils;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author emeroad
 * @author netspid
 * @author jaehong.kim
 */
@Controller
public class MapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MapService mapService;

    @Autowired
    private Limiter dateLimit;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private ApplicationFactory applicationFactory;

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
        final Range range = new Range(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return selectApplicationMap(application, range, searchOption);
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
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return selectApplicationMap(application, range, searchOption);
    }

    private MapWrap selectApplicationMap(Application application, Range range, SearchOption searchOption) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (searchOption == null) {
            throw new NullPointerException("searchOption must not be null");
        }

        logger.info("getServerMap() application:{} range:{} searchOption:{}", application, range, searchOption);

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
                                        @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
                                        @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange) {

        long to = TimeUtils.getDelayLastTime();
        long from = to - period;

        final Range range = new Range(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return selectApplicationMap(application, range, searchOption);
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
                                        @RequestParam(value = "callerRange", defaultValue = DEFAULT_SEARCH_DEPTH) int callerRange,
                                        @RequestParam(value = "calleeRange", defaultValue = DEFAULT_SEARCH_DEPTH) int calleeRange) {

        long to = TimeUtils.getDelayLastTime();
        long from = to - period;

        final Range range = new Range(from, to);
        this.dateLimit.limit(range);

        SearchOption searchOption = new SearchOption(callerRange, calleeRange);
        assertSearchOption(searchOption);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        return selectApplicationMap(application, range, searchOption);
    }

    @RequestMapping(value = "/getResponseTimeHistogramData", method = RequestMethod.GET, params = "serviceTypeName")
    @ResponseBody
    public ApplicationTimeHistogramViewModel getResponseTimeHistogramData(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = new Range(from, to);
        dateLimit.limit(range);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        ApplicationTimeHistogramViewModel applicationTimeHistogramViewModel = mapService.selectResponseTimeHistogramData(application, range);

        return applicationTimeHistogramViewModel;
    }

}
