package com.nhn.pinpoint.web.controller;


import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.nhn.pinpoint.common.util.LimitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.filter.FilterBuilder;
import com.nhn.pinpoint.web.service.FilteredApplicationMapService;
import com.nhn.pinpoint.web.service.TransactionInfoService;
import com.nhn.pinpoint.web.service.SpanResult;
import com.nhn.pinpoint.web.service.SpanService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.BusinessTransactions;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.callstacks.RecordSet;

/**
 * @author emeroad
 */
@Controller
public class BusinessTransactionController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SpanService spanService;

	@Autowired
	private TransactionInfoService transactionInfoService;

	@Autowired
	private FilteredApplicationMapService filteredApplicationMapService;

    @Autowired
    private FilterBuilder filterBuilder;

    /**
	 * applicationname에서 from ~ to 시간대에 수행된 URL을 조회한다.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param to
	 * @return
	 */
	@RequestMapping(value = "/transactionList", method = RequestMethod.GET)
	public String getBusinessTransactionsData(Model model, HttpServletResponse response,
											@RequestParam("application") String applicationName,
											@RequestParam("from") long from, 
											@RequestParam("to") long to,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "limit", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
		
		// TOOD 구조개선을 위해 server map조회 로직 분리함, 임시로 분리한 상태이고 개선이 필요하다.
		LimitedScanResult<List<TransactionId>> traceIdList = filteredApplicationMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);

		Filter filter = filterBuilder.build(filterText);
		BusinessTransactions selectBusinessTransactions = transactionInfoService.selectBusinessTransactions(traceIdList.getScanData(), applicationName, from, to, filter);

		model.addAttribute("lastFetchedTimestamp", traceIdList.getLimitedTime());
		model.addAttribute("rpcList", selectBusinessTransactions.getBusinessTransactionIterator());
		model.addAttribute("requestList", selectBusinessTransactions.getBusinessTransactionIterator());
		model.addAttribute("scatterList", selectBusinessTransactions.getBusinessTransactionIterator());
		model.addAttribute("applicationName", applicationName);
		model.addAttribute("from", new Date(from));
		model.addAttribute("to", new Date(to));
		model.addAttribute("urlCount", selectBusinessTransactions.getURLCount());
		model.addAttribute("totalCount", selectBusinessTransactions.getTotalCallCount());
		model.addAttribute("filterText", filterText);
		model.addAttribute("filter", filter);
		
		return "transactionList";
	}

	@RequestMapping(value = "/lastTransactionList", method = RequestMethod.GET)
	public String getLastBusinessTransactionsData(Model model, HttpServletResponse response,
											@RequestParam("application") String applicationName, 
											@RequestParam("period") long period,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "limit", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
        long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getBusinessTransactionsData(model, response, applicationName, from, to, filterText, limit);
	}

    /**
     * 선택한 하나의 Transaction 정보 조회.
     *
     * @param traceIdParam
     * @param focusTimestamp
     * @return
     */
    @RequestMapping(value = "/transactionInfo", method = RequestMethod.GET)
    public ModelAndView transactionInfo(@RequestParam("traceId") String traceIdParam, @RequestParam("focusTimestamp") long focusTimestamp,
                                        // FIXME jsonResult는 UI 개발 편의를 위해 임시로 추가된 변수 임. 나중에 제거.
                                        // 기존 html view에서 json을 넘어가는 중임.
                                        @RequestParam(value = "jsonResult", required = false, defaultValue = "false") boolean jsonResult,
                                        @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion) {
        logger.debug("traceId:{}", traceIdParam);

        final TransactionId traceId = new TransactionId(traceIdParam);

        ModelAndView mv = new ModelAndView("transactionInfo");

        try {
            // select spans
            final SpanResult spanResult = this.spanService.selectSpan(traceId, focusTimestamp);
            List<SpanAlign> spanAligns = spanResult.getSpanAlign();

            if (spanAligns.isEmpty()) {
                mv.addObject("errorCode", 9);
                mv.setViewName("error");
                return mv;
            }

            // debug
            mv.addObject("spanList", spanAligns);

            mv.addObject("traceId", traceId);

			// application map
			ApplicationMap map = filteredApplicationMapService.selectApplicationMap(traceId);
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
        } catch (Exception e) {
            logger.warn("BusinessTransactionController Error Cause" + e.getMessage(), e);
            // TODO 아무래도 다시 던져야 될듯한데. Exception처리 정책을 생각해봐야 한다.
            // throw e;
        }

        // FIXME jsonResult는 UI 개발 편의를 위해 임시로 추가된 변수 임. 나중에 제거.
        if (jsonResult) {
            if (viewVersion == 2) {
                mv.setViewName("transactionInfoJsonHash");
            } else {
                mv.setViewName("transactionInfoJson");
            }
        }
        return mv;
    }
}
