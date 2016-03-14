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
import com.navercorp.pinpoint.web.view.ServerTime;
import com.navercorp.pinpoint.web.view.TransactionMetaDataViewModel;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.TransactionMetadataQuery;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;
import java.util.SortedSet;
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
    @RequestMapping(value = "/getScatterData", method = RequestMethod.GET)
    public ModelAndView getScatterData(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("xGroupUnit") int xGroupUnit,
            @RequestParam("yGroupUnit") int yGroupUnit,
            @RequestParam("limit") int limit,
            @RequestParam(value = "backwardDirection", required = false, defaultValue = "true") boolean backwardDirection,
            @RequestParam(value = "filter", required = false) String filterText,
            @RequestParam(value = "_callback", required = false) String jsonpCallback,
            @RequestParam(value = "v", required = false, defaultValue = "1") int version) {
        limit = LimitUtils.checkRange(limit);

        StopWatch watch = new StopWatch();
        watch.start("getScatterData");

        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.createUncheckedRange(from, to);
        logger.debug("fetch scatter data. RANGE={}, X-Group-Unit:{}, Y-Group-Unit:{}, LIMIT={}, BACKWARD_DIRECTION:{}, FILTER:{}", range, xGroupUnit, yGroupUnit, limit, backwardDirection, filterText);

        ModelAndView mv = null;
        if (StringUtils.isEmpty(filterText)) {
            mv = selectScatterData(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection, version);
        } else {
            mv = selectFilterScatterData(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection, filterText, version);
        }

        if (jsonpCallback == null) {
            mv.setViewName("jsonView");
        } else {
            mv.setViewName("jsonpView");
        }

        watch.stop();

        logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

        return mv;
    }

    private ModelAndView selectScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection, int version) {
        ModelAndView mv = null;
        if (version == 1) {
            final ScatterData scatterData = scatter.selectScatterData(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection);
            boolean requestComplete = scatterData.getDotSize() < limit;

            mv = createScatterDataV1(scatterData, requestComplete);
        } else {
            mv = new ModelAndView();
        }

        mv.addObject("currentServerTime", new ServerTime().getCurrentServerTime());
        mv.addObject("from", range.getFrom());
        mv.addObject("to", range.getTo());
        return mv;
    }

    private ModelAndView selectFilterScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection, String filterText, int version) {
        final LimitedScanResult<List<TransactionId>> limitedScanResult = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit, backwardDirection);

        final List<TransactionId> traceIdList = limitedScanResult.getScanData();
        logger.trace("submitted transactionId count={}", traceIdList.size());

        boolean requestComplete = traceIdList.size() < limit;

        // TODO just need sorted?  we need range check with tree-based structure.
        SortedSet<TransactionId> traceIdSet = new TreeSet<>(traceIdList);
        logger.debug("unified traceIdSet size={}", traceIdSet.size());

        Filter filter = filterBuilder.build(filterText);

        ModelAndView mv;
        if (version == 1) {
            ScatterData scatterData = scatter.selectScatterData(traceIdSet, applicationName, range, xGroupUnit, yGroupUnit, filter);
            if (logger.isDebugEnabled()) {
                logger.debug("getScatterData range scan(limited:{}, backwardDirection:{}) from ~ to:{} ~ {}, limited:{}, filterDataSize:{}",
                        limit, backwardDirection, DateUtils.longToDateStr(range.getFrom()), DateUtils.longToDateStr(range.getTo()), DateUtils.longToDateStr(limitedScanResult.getLimitedTime()), traceIdList.size());
            }

            mv = createScatterDataV1(scatterData, requestComplete);
        } else {
            mv = new ModelAndView();
        }

        mv.addObject("currentServerTime", new ServerTime().getCurrentServerTime());
        mv.addObject("from", range.getFrom());
        mv.addObject("to", range.getTo());
        return mv;
    }

    private ModelAndView createScatterDataV1(ScatterData scatterData, boolean complete) {
        ModelAndView mv = new ModelAndView();

        mv.addObject("resultFrom", scatterData.getOldestAcceptedTime());
        mv.addObject("resultTo", scatterData.getLatestAcceptedTime());

        mv.addObject("complete", complete);
        mv.addObject("scatter", scatterData);

        return mv;
    }

}
