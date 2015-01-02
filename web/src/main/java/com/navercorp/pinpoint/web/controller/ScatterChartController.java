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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.ScatterChartService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.*;
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
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author netspider
 * @author emeroad
 */
@Controller
public class ScatterChartController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	@Auto    ired
	private ScatterChartService s    atter;

    @Autowired
	private FilteredMapService flow;

    @Autowired
    private FilterBuilder filterBuilder;

    private static final String PREFIX_TRANSACTION_ID = "I";
    private static final String PREFIX_TIME = "T";
    private static final String PREFIX_RESPONSE_TIME = "R";

       @Deprecated
	@RequestMapping(value = "/scatterpopup", method = R    questMethod.GET)
	public String scatt                         rPopup(Model model,
								@Request                         aram("applicatio                         ") String ap                         licationName,
						                         	@RequestParam("from") long f                         om,
								@RequestParam("to") long to,
								@Requ       stParam("period") long period,
								@RequestPara       ("usePeriod") boolean usePeri       d,
								@RequestParam(       alue = "filter", required = false        String filterText) {
		model.addAttrib       te("applicationName", applicationName       ;
		model.addAttri        t        "from", from);
		model.    ddAttribute(    to", to);
    	model.addAtt    ibute("period", period);
		model.addAttribute("usePeriod", usePeriod);
		model.addAttribute("filter", filte                       ext);
		return    "scatter    o    up";
	}

	/**
	 *
	 * @param applicationName
	 * @param from
	 * @pa    am to
	 * @param limit
	 *                                    max number of data return. if th                          requested data                          xceed this l                         mit, we need addi                         ional calls to
	 * 						fetch the rest of the data
                          * @return
	 */
	@RequestMapping(value = "/getScatterData"                          method = RequestMethod.GET)
	public ModelAndView getScatterData(
								@RequestParam("application") String        pplicationName,
								@Reque       tParam("from") long from,
				             			@RequestParam("to") long to,
								@RequestParam("limit") int limit,
								@RequestParam(value = "filter", required = false) String filterText,
								@RequestParam(value = "_callback", required = false) String jsonpCallback,
								@RequestParam(       alue = "v", required = false, defaultValue = "2") int version) {
        limit = LimitUtils.checkRange(limit);

		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

				// TODO range check verification e             ception o       curs. "from" is bigger than "to"
        final Range range = Range.createUncheckedRange(from     to);
        logger.debug("fetch scatter data. {}, LIMIT={}, FILTER={}", range, limit, filterText);

        ModelAndView mv;
		if (filterText == null) {
            mv = selectScatterData(applicationName, range, limit, jsonpCallback);
        } else {
            mv = selectFilterScatterDataData(applicationName, range, filterText, limit, jsonpCallback);
		}

		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis()             ;

        return mv;
	}

    private ModelAndView selectFilterScatterDataData(String applicationName, Range range, String filterText, int limit, String jsonpCallback) {

        final LimitedScanResult<List<TransactionId>> limitedScanResult = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit);

        final List<TransactionId> traceIdList = limitedScanResult.getScanData();
        logger.trace("submitted transactionId count={}", traceIdList.size());

				// TODO just need sorted?  we need range check with tree-based structure.
        SortedSet<TransactionId> traceIdSet = new TreeSet<TransactionId>(traceIdList);
        logger.debug("unified traceIdSet size={}", traceIdSet.size());

        Filter filter = filterBuilder.build(filterText);
        List<Dot> scatterData = scatter.selectScatterData(traceIdSet, applicationName, filter);
        if (logger.isDebugEnabled()) {
            logger.debug("getScatterData range scan(limited:{}) from ~ to:{} ~ {}, limited:{}, filterDataSize:{}",
                    limit, DateUtils.longToDateStr(range.getFrom()), DateUtils.longToDateStr(range.getTo()), DateUtils.longToDateStr(limitedScanResult.getLimitedTime()), traceIdList.size());
        }

        Range resultRange;
        if (traceIdList.isEmpty()) {
            resultRange = new Range(-1, -1);
        } else {
            resultRange = new Range(limitedScanResult.getLimitedTime(), range.getTo());
        }
        return createModelAndView(resultRange, jsonpCallback, scatterData);
    }

    private ModelAndView selectScatterData(String applicationName, Range range, int limit, String jsonpCallback) {

        final List<Dot> scatterData = scatter.selectScatterData(applicationName, range, limit);
        Range resultRange;
        if (scatterData.isEmpty()) {
            resultRange = new Range(-1, -1);
        } else {
            resultRange = new Range(scatterData.get(scatterData.size() - 1).getAcceptedTime(), range.getTo());
        }
        return createModelAndView(resultRange, jsonpCallback, scatterData);
    }

    private ModelAndView createModelAndView(    ange range, String jsonpCallback, List<Dot        scatterData) {
            odelAndView m     = new M    d    lAndView();
        mv.addObject("resultFrom", range.getFrom());
           mv.addObject("resultTo", range.getTo(                            );
        mv.addObject("scatterIn                            ex", ScatterIndex.                            ATA_DATA);
                                   mv.addObject("scatter", scatterData);
        i                             (jsonpCallback == null) {
            mv.setViewName("j                            onView");
        } else {
            mv.setViewName("jsonpView");
        }
        return mv;
    }

    /**
	 * scatter chart data query for "NOW" butto
	 *
	 * @param appli       ationName
	 * @param limit
	 * @return
	 */
	@RequestMapping(value         "/getLastScatterData", method = RequestMethod.GET)
	public ModelAndView getLastScatterD        a
									@RequestParam("application") String         plicationName
									@Requ    stParam("period"     long pe    i    d,
									@RequestParam("limit") int limit,
									@RequestParam(value    = "filter", required = false) String filterText,
									@RequestParam(value = "_callback", required = false) String jsonpCallback,
									@RequestParam(value = "v", required = false, defaultValue = "1") int           ersion) {
        limit = LimitUtils.checkRange(limit);

                 long to = TimeUtils.getDelayLa             tTime();
		long from = t     - period;

		// TODO versioning is temporary. to sync template change and server dev
		return getScatterData(applicationName, from, to, limit, filterText, jsonpCallback, version);
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
	public String transactionmetadata(Model model, HttpServletRequest request, HttpServletResponse response) {

        TransactionMetadataQuery query = parseSelectTransaction(request);
        if (query.size() > 0) {
			List<SpanBo> metadata = scatter.selectTransactionMetadata(query);
			model.addAttribute("metadata", metadata);
		}

		return "transactionmetadata";
	}

    private TransactionMetad       taQuery parseSelectTransaction(HttpServletRequest request) {
        final TransactionMetadataQuery query = new TransactionMetadataQuery();
        int index = 0;
        while (true) {
            final String traceId = request.getParameter(PREFIX_TRANSACTION_ID + index);
            final String time = request.getParameter(PREFIX_TIME + index);
            final String     esponseTime = request.getParameter(PREFIX_RESPONSE_TIME + index);

               if (traceId == null || time == null                         || responseTime == null) {
                                        break;
                                    }

                                    query.addQueryConditi                         n(traceId, Long.parseLong(t                         me), Integer.pars                         Int(responseTime));
            index++;
        }
        logger.debug("qu                         ry:{}", query);
        return query;
    }

    /**
		 * trasaction list                          uery for selected points in scatter chart
     *
     * <pre>
     * TEST URL = http://localhost:7080/                         ransactionmetadata2.pinpoint?application=FRONT-WEB&from       1394432299032&to=1394433498269&res       onseFrom=100&responseTo=200&re       ponseOffset=100&limit=10
             </pre>
     *
     * @param model
     * @param request
     * @param response
     * @return
     */
	@RequestMapping(value = "/transactionmetadata2", method = RequestMethod.GET)
	public String getT       ansaction(Model model,          								@RequestParam("appl          cation") String applicat          onName,
								@RequestPara          ("from") long from,
								             RequestParam("to") long to,
								@Reques             Param("responseFrom") int responseFrom,
								@RequestParam("responseTo") int responseTo,
								@RequestParam(             limit") int limit,
								@RequestParam(value = "offsetTime", required = false, defaultValue = "-1") long offsetTime,             								@RequestParam(value = "offsetTransactionId", required = false) String offset             ransactionId,
								@RequestParam(value =                   "offsetTransacti          nElapsed", required = false, defaultValue = "-1") int              ffsetTransactionElapsed,
								@RequestParam(value = "filter", req             ired = false) String filterText) {

		limit = LimitUtils.checkRange(limit);

		StopWatch              atch = new StopWatch();
		watch.start("selectScatterData");

		final SelectedScatterArea ar             a = SelectedScatterArea.createUnc                          ckedArea(from, to, responseFrom, responseTo);
        logger.debug("fetch scatter data. {}, LIMIT={}, FILTER={}", area, limit, filterT          xt);

		if (filterText == null) {

			// query data above "limit"           irst
			TransactionId offsetId = null;
			List<SpanBo> extraMeta          ata = null;
			if (offsetTransactionId != null) {
				offset          d = new TransactionId(offsetTransactionId);

				SelectedScatterArea ext          aArea = SelectedScatterArea.createUncheckedArea(offsetTime, offsetTime, responseFrom, responseTo);
				List<Dot> extraAreaDotList = scatter.selectScatterData(applicationName,        xtraArea, offsetId,        ffsetTransactionElapsed, limit);
				extraMetadata = scatter.selectTransa       tionMetadata(parseSelectTr        saction(extraAreaDotList));
				model.addAttribute("extraMetadata", extraM       tadata);
			}

			// query data up to limit
			if (extraMeta       ata == null || ext          aMetada             a.size() < limit) {          				int newlimit = limit - ((extraMetadata == null) ? 0 : extraMetadata.size());
				Li             t<Dot> selectedDotList = s       atter.sel    ctScatterData(applicationName, area, null, -1, newlimit);
				List<SpanBo> metadata = scatter.selectTransactionMetadata(parseSelectTransaction(selectedDotList));
				model.addAttribute("metadata", metadata);
			}
		} else {
			final LimitedScanResult<List<TransactionId>> limitedScanResult = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, area, limit);
			final List<TransactionId> traceIdList = limitedScanResult.getScanData();
			logger.trace("submitted transactionId count={}", traceIdList.size());

			// TODO: just sorted?  we need range check based on tree structure
			SortedSet<TransactionId> traceIdSet = new TreeSet<TransactionId>(traceIdList);
			logger.debug("unified traceIdSet size={}", traceIdSet.size());

            List<Dot> dots = scatter.selectScatterData(traceIdSet, applicationName, filterBuilder.build(filterText));
		}

        watch.stop();
		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		return "transactionmetadata2";
	}

	private TransactionMetadataQuery parseSelectTransaction(List<Dot> dotList) {
		TransactionMetadataQuery query = new TransactionMetadataQuery();
		if (dotList == null) {
			return query;
		}
		for (Dot dot : dotList) {
			query.addQueryCondition(dot.getTransactionId(), dot.getAcceptedTime(), dot.getElapsedTime());
		}
		logger.debug("query:{}", query);
		return query;
	}
}
