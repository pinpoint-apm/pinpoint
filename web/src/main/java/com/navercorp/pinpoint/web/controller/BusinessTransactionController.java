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


import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.SpanResult;
import com.navercorp.pinpoint.web.service.SpanService;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.BusinessTransactions;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author emeroad
 */
@Controller
public class BusinessTransactionController {

    private Logger logger = LoggerFactory.getLogger(this.getClass())

	@Auto    ired
	private SpanService spanS    rvice;

    @Autowired
	private TransactionInfoService transactio    InfoServ    ce;

	@Autowired
	private FilteredMapService filteredMapService;

    @Autowired
    private FilterBuilder fil    erBuilder;

    /**
	 * executed URLs in applicationname query wit        n from ~ to t    meframe
	 *
	 * @param     odel
	 * @pa    am applica    ionName
     * @param from
	      @param to
	 * @return
	 */
    @Deprecated
	@RequestMapping(value = "/transactionList"     method = RequestMethod.GET)
    @ResponseBody
	pub                                  ic Model getBusinessTransactio                                  sData(Mode                                   model
											@RequestParam("application") Strin                                   applicationName,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
							       			@RequestParam(value = "filt       r", required = false) String filterText,
											@RequestParam(value = "li       it", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
		Range range = new Range(from, to       ;
		// TODO more refactoring needed: partial       y separated out server map lookup logic.
		LimitedScanResult<List<TransactionId>> traceIdList = filteredMapService.selectTraceIdsFromApplicationTraceIndex(a       plicationName, range, limit);

		Filter filter = filterBuilder.build(       ilterText);
		BusinessTransactions selectBusinessTransactions = transactionInfo       ervice.selectBusinessTransactions(traceIdList.getScanData(), applicationName, range        filter);

		model.addAttribute("lastFetchedTimestamp", traceIdList.getLimitedTime(       );
		model.addAttribute("rpcList", selectBusinessTr       nsactions.getBusinessTransaction());
		       odel.addAttribute("requestList", se       ectBusinessTransactions.getBusinessTransaction());
		model.addAttribu       e("scatterList", selectBusinessTransactions.getBusinessTransaction());
		mode       .addAttribute("applicationName", applicat       onName);
		model.addAttribute("from", new Date(from));
		model.addAttribute("       o", new D    te(to));
		model    addAttribute("urlCount", selectBusinessTransactions.getURLCount());
		model.addAttribute("t    talCount", selectBusinessTransactions.getTotalCallCount());
		model.addAttribute("fil                                  erText", filterText);
		model.                                  ddAttribute("f                                  lter", filter);
        // Deprecated jsp -> n                                  ed json dump
		return model;
	}

    @Deprecated
	@RequestMapping(value = "/lastTransactionList", method = RequestMethod.GET)
    @ResponseBody
	public Model       getLastBusinessTrans       ctionsData(Model model, HttpServletResponse response,
											@RequestParam("appl    cation")       String applicationName,
											@RequestParam("period") long period,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "limit", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
        long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getBusinessTransactionsData(model, applicationName, from, to, filterText, limit);
	}

    /**
		 * info lookup for a selected transaction
     *
     * @param traceIdParam
     * @param focusTimestamp
     * @return
     */
    @RequestMapping(value = "/transactionInfo", method = RequestMethod.GET)
    public ModelAndView transactionInfo(@RequestParam("traceId") String traceIdParam,
                                        @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") long focusTimestamp,
                                        @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion,
                                        HttpServletResponse response) {
        logger.debug("traceId:{}", traceIdParam);

        final TransactionId traceId = new TransactionId(traceIdParam);

        // select spans
        final SpanResult spanResult = this.spanService.selectSpan(traceId, focusTimestamp);
        List<SpanAlign> spanAligns = spanResult.getSpanAlignList();

        if (spanAligns.isEmpty()) {
            // TODO fix error page.
            final ModelAndView error = new ModelAndView();
            // redefine errorCode.???
            error.addObject("errorCode", 9);
            error.addObject("message", "Trace not found. traceId:" + traceId);
            error.setViewName("error");
            return error;
        }

        final ModelAndView mv = new ModelAndView();
        // debug
        mv.addObject("spanList", spanAligns);

        mv.addObject("traceId", traceId);

        // application map
        ApplicationMap map = filteredMapService.selectApplicationMap(traceId);
        mv.addObject("nodes", map.getNodes());
        mv.addObject("links", map.getLinks());

        // call stacks
        RecordSet recordSet = this.transactionInfoService.createRecordSet(spanAligns, focusTimestamp);
        mv.addObject("recordSet", recordSet);

        mv.addObject("applicationName", recordSet.getApplicationName());
        mv.addObject("callstack", recordSet.getRecordList());
        mv.addObject("timeline", recordSet.getRecordList());
        mv.addObject("callstackStart", recordSet.getStartTime());
        mv.addObject("callstackEnd", recordSet.getEndTime());
        mv.addObject("completeState", spanResult.getCompleteTypeString());


        if (viewVersion == 2) {
            mv.setViewName("transactionInfoJsonHash");
        } else {
            mv.setViewName("transactionInfoJson");
        }
        return mv;
    }
}
