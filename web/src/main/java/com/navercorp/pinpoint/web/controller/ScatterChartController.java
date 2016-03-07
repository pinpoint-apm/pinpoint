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

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.ScatterChartService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.util.TimeUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.view.ServerTime;
import com.navercorp.pinpoint.web.view.TransactionMetaDataViewModel;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.TransactionMetadataQuery;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.ScatterIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
@Controller
public class ScatterChartController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ScatterChartService scatter;

    @Autowired
    private FilteredMapService flow;

    @Autowired
    private FilterBuilder filterBuilder;

    private static final String PREFIX_TRANSACTION_ID = "I";
    private static final String PREFIX_TIME = "T";
    private static final String PREFIX_RESPONSE_TIME = "R";

    @Deprecated
    @RequestMapping(value = "/scatterpopup", method = RequestMethod.GET)
    public String scatterPopup(Model model,
                               @RequestParam("application") String applicationName,
                               @RequestParam("from") long from,
                               @RequestParam("to") long to,
                               @RequestParam("period") long period,
                               @RequestParam("usePeriod") boolean usePeriod,
                               @RequestParam(value = "filter", required = false) String filterText) {
        model.addAttribute("applicationName", applicationName);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("period", period);
        model.addAttribute("usePeriod", usePeriod);
        model.addAttribute("filter", filterText);
        return "scatterPopup";
    }

    /**
     * @param applicationName
     * @param from
     * @param to
     * @param limit           max number of data return. if the requested data exceed this limit, we need additional calls to
     *                        fetch the rest of the data
     * @return
     */
    @RequestMapping(value = "/getScatterData", method = RequestMethod.GET)
    public ModelAndView getScatterData(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("limit") int limit,
            @RequestParam(value = "backwardDirection", required = false, defaultValue = "true") boolean backwardDirection,
            @RequestParam(value = "filter", required = false) String filterText,
            @RequestParam(value = "_callback", required = false) String jsonpCallback,
            @RequestParam(value = "v", required = false, defaultValue = "2") int version) {
        limit = LimitUtils.checkRange(limit);

        StopWatch watch = new StopWatch();
        watch.start("selectScatterData");

        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.createUncheckedRange(from, to);
        logger.debug("fetch scatter data. {}, LIMIT={}, FILTER={}, BACKWARD_DIRECTION={}", range, limit, filterText, backwardDirection);

        ModelAndView mv;
        if (filterText == null) {
            mv = selectScatterData(applicationName, range, limit, backwardDirection, jsonpCallback, version);
        } else {
            mv = selectFilterScatterDataData(applicationName, range, filterText, limit, backwardDirection, jsonpCallback, version);
        }

        watch.stop();

        logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

        return mv;
    }

    private ModelAndView selectFilterScatterDataData(String applicationName, Range range, String filterText, int limit, boolean backwardDirection, String jsonpCallback, int version) {
        final LimitedScanResult<List<TransactionId>> limitedScanResult = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit, backwardDirection);

        final List<TransactionId> traceIdList = limitedScanResult.getScanData();
        logger.trace("submitted transactionId count={}", traceIdList.size());

        boolean requestComplete = traceIdList.size() < limit;

        // TODO just need sorted?  we need range check with tree-based structure.
        SortedSet<TransactionId> traceIdSet = new TreeSet<>(traceIdList);
        logger.debug("unified traceIdSet size={}", traceIdSet.size());

        Filter filter = filterBuilder.build(filterText);
        List<Dot> scatterData = scatter.selectScatterData(traceIdSet, applicationName, filter);
        if (logger.isDebugEnabled()) {
            logger.debug("getScatterData range scan(limited:{}, backwardDirection:{}) from ~ to:{} ~ {}, limited:{}, filterDataSize:{}",
                    limit, backwardDirection, DateUtils.longToDateStr(range.getFrom()), DateUtils.longToDateStr(range.getTo()), DateUtils.longToDateStr(limitedScanResult.getLimitedTime()), traceIdList.size());
        }

        Range resultRange;
        if (traceIdList.isEmpty()) {
            resultRange = new Range(-1, -1);
        } else {
            resultRange = new Range(limitedScanResult.getLimitedTime(), range.getTo());
        }
        return createModelAndView(resultRange, jsonpCallback, scatterData, requestComplete, version);
    }

    private ModelAndView selectScatterData(String applicationName, Range range, int limit, boolean backwardDirection, String jsonpCallback, int version) {
        final List<Dot> scatterData = scatter.selectScatterData(applicationName, range, limit, backwardDirection);
        Range resultRange;
        if (scatterData.isEmpty()) {
            resultRange = new Range(-1, -1);
        } else {
            resultRange = new Range(scatterData.get(scatterData.size() - 1).getAcceptedTime(), range.getTo());
        }

        boolean requestComplete = scatterData.size() < limit;

        return createModelAndView(resultRange, jsonpCallback, scatterData, requestComplete, version);
    }

    private ModelAndView createModelAndView(Range range, String jsonpCallback, List<Dot> scatterData, boolean requestComplete, int version) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("resultFrom", range.getFrom());
        mv.addObject("resultTo", range.getTo());
        mv.addObject("scatterIndex", ScatterIndex.MATA_DATA);
        final Map<String, List<Dot>> scatterAgentData = new HashMap<>();
        for (Dot dot : scatterData) {
            List<Dot> list = scatterAgentData.get(dot.getAgentId());
            if (list == null) {
                list = new ArrayList<>();
                scatterAgentData.put(dot.getAgentId(), list);
            }
            list.add(dot);
        }

        if (version == 4) {
            TimeWindow timeWindow = new TimeWindow(range);
            TreeMap<Long, List<Dot>> sortedMap = new TreeMap<>();
            for (Dot dot : scatterData) {
                long key = timeWindow.refineTimestamp(dot.getAcceptedTime());
                List<Dot> list = sortedMap.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    sortedMap.put(key, list);
                }
                list.add(dot);
            }

            // average
            // max
            // min
            List<Dot> averageList = new ArrayList<>();
            List<Dot> maxList = new ArrayList<>();
            List<Dot> minList = new ArrayList<>();
            for (Map.Entry<Long, List<Dot>> entry : sortedMap.entrySet()) {
                Dot max = null;
                Dot min = null;
                int totalTime = 0;
                for (Dot dot : entry.getValue()) {
                    if (max == null || dot.getElapsedTime() > max.getElapsedTime()) {
                        max = dot;
                    }

                    if (min == null || dot.getElapsedTime() < min.getElapsedTime()) {
                        min = dot;
                    }

                    totalTime += dot.getElapsedTime();
                }
                int averageTime = totalTime / entry.getValue().size();
                averageList.add(new Dot(new TransactionId("", 0, 0), entry.getKey(), averageTime, 0, ""));
                maxList.add(new Dot(new TransactionId(max.getTransactionIdAsString()), entry.getKey(), max.getElapsedTime(), max.getExceptionCode(), max.getAgentId()));
                minList.add(new Dot(new TransactionId(min.getTransactionIdAsString()), entry.getKey(), min.getElapsedTime(), min.getExceptionCode(), min.getAgentId()));
            }
            scatterAgentData.put("_#AverageAgent", averageList);
            scatterAgentData.put("_#MaxAgent", maxList);
            scatterAgentData.put("_#MinAgent", minList);
        }

        mv.addObject("complete", requestComplete);
        mv.addObject("scatter", scatterAgentData);

        if (jsonpCallback == null) {
            mv.setViewName("jsonView");
        } else {
            mv.setViewName("jsonpView");
        }
        return mv;
    }

    /**
     * scatter chart data query for "NOW" button
     *
     * @param applicationName
     * @param limit
     * @return
     */
    @RequestMapping(value = "/getLastScatterData", method = RequestMethod.GET)
    public ModelAndView getLastScatterData(
            @RequestParam("application") String applicationName,
            @RequestParam("period") long period,
            @RequestParam("limit") int limit,
            @RequestParam(value = "filter", required = false) String filterText,
            @RequestParam(value = "backwardDirection", required = false, defaultValue = "true") boolean backwardDirection,
            @RequestParam(value = "_callback", required = false) String jsonpCallback,
            @RequestParam(value = "v", required = false, defaultValue = "1") int version) {
        limit = LimitUtils.checkRange(limit);

        long to = TimeUtils.getDelayLastTime();
        long from = to - period;

        // TODO versioning is temporary. to sync template change and server dev
        return getScatterData(applicationName, from, to, limit, backwardDirection, filterText, jsonpCallback, version);
    }

    /**
     * selected points from scatter chart data query
     *
     * @param model
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/transactionmetadata", method = RequestMethod.POST)
    @ResponseBody
    public TransactionMetaDataViewModel transactionmetadata(Model model, HttpServletRequest request, HttpServletResponse response) {
        TransactionMetaDataViewModel viewModel = new TransactionMetaDataViewModel();
        TransactionMetadataQuery query = parseSelectTransaction(request);
        if (query.size() > 0) {
            List<SpanBo> metadata = scatter.selectTransactionMetadata(query);
            viewModel.setSpanBoList(metadata);
        }

        return viewModel;
    }

    private TransactionMetadataQuery parseSelectTransaction(HttpServletRequest request) {
        final TransactionMetadataQuery query = new TransactionMetadataQuery();
        int index = 0;
        while (true) {
            final String traceId = request.getParameter(PREFIX_TRANSACTION_ID + index);
            final String time = request.getParameter(PREFIX_TIME + index);
            final String responseTime = request.getParameter(PREFIX_RESPONSE_TIME + index);

            if (traceId == null || time == null || responseTime == null) {
                break;
            }

            query.addQueryCondition(traceId, Long.parseLong(time), Integer.parseInt(responseTime));
            index++;
        }
        logger.debug("query:{}", query);
        return query;
    }


    /**
     * @param applicationName
     * @param from
     * @param to
     * @param limit           max number of data return. if the requested data exceed this limit, we need additional calls to
     *                        fetch the rest of the data
     * @return
     */
    @RequestMapping(value = "/getScatterDataMadeOfDotGroup", method = RequestMethod.GET)
    public ModelAndView getScatterDataMadeOfDotGroup(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("xGroupUnit") int xGroupUnit,
            @RequestParam("yGroupUnit") int yGroupUnit,
            @RequestParam("limit") int limit,
            @RequestParam(value = "backwardDirection", required = false, defaultValue = "true") boolean backwardDirection,
            @RequestParam(value = "_callback", required = false) String jsonpCallback,
            @RequestParam(value = "v", required = false, defaultValue = "0") int version) {
        limit = LimitUtils.checkRange(limit);

        StopWatch watch = new StopWatch();
        watch.start("getScatterDataMadeOfDotGroup");

        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.createUncheckedRange(from, to);
        logger.debug("fetch scatter data made of dot group. RANGE={}, LIMIT={}, BACKWARD_DIRECTION:{}, X-Group-Unit:{}, Y-Group-Unit:{}", range, limit, backwardDirection, xGroupUnit, yGroupUnit);

        ModelAndView mv = selectScatterDataMadeOfDotGroup(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection);
        if (jsonpCallback == null) {
            mv.setViewName("jsonView");
        } else {
            mv.setViewName("jsonpView");
        }

        watch.stop();

        logger.info("Fetch scatterDataMadeOfDotGroup time : {}ms", watch.getLastTaskTimeMillis());

        return mv;
    }

    private ModelAndView selectScatterDataMadeOfDotGroup(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection) {
        final ScatterData scatterData = scatter.selectScatterDataMadeOfDotGroup(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection);

        ModelAndView mv = new ModelAndView();

        mv.addObject("currentServerTime", new ServerTime().getCurrentServerTime());
        mv.addObject("from", range.getFrom());
        mv.addObject("to", range.getTo());
        mv.addObject("resultFrom", scatterData.getOldestAcceptedTime());
        mv.addObject("resultTo", scatterData.getLatestAcceptedTime());

        boolean requestComplete = scatterData.getDotSize() < limit;
        mv.addObject("complete", requestComplete);
        mv.addObject("scatter", scatterData);

        return mv;
    }

}
