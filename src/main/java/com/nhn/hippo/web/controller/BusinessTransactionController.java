package com.nhn.hippo.web.controller;


import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.filter.Filter;
import com.nhn.hippo.web.filter.FilterBuilder;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.service.RecordSetService;
import com.nhn.hippo.web.service.SpanService;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.callstacks.RecordSet;

/**
 *
 */
@Controller
public class BusinessTransactionController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SpanService spanService;

	@Autowired
	private RecordSetService recordSetService;

	@Autowired
	private FlowChartService flow;

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
	public String getBusinessTransactionsData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam(value = "filter", required = false) String filterText) {
		// TOOD 구조개선을 위해 server map조회 로직 분리함, 임시로 분리한 상태이고 개선이 필요하다.

		Set<TraceId> traceIdList = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);

		Filter filter = FilterBuilder.build(filterText);
		BusinessTransactions selectBusinessTransactions = flow.selectBusinessTransactions(traceIdList, applicationName, from, to, filter);

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
		
		addResponseHeader(response);
		return "transactionList";
	}

	@RequestMapping(value = "/lastTransactionList", method = RequestMethod.GET)
	public String getLastBusinessTransactionsData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period, @RequestParam(value = "filter", required = false) String filterText) {
		long to = getQueryEndTime();
		long from = to - period;
		return getBusinessTransactionsData(model, response, applicationName, from, to, filterText);
	}

	/**
	 * 선택한 하나의 Transaction 정보 조회.
	 *  
	 * @param traceIdParam
	 * @param focusTimestamp
	 * @return
	 */
	@RequestMapping(value = "/transactionInfo", method = RequestMethod.GET)
	public ModelAndView transactionInfo(@RequestParam("traceId") String traceIdParam, @RequestParam("focusTimestamp") long focusTimestamp) {
		logger.debug("traceId:{}", traceIdParam);

		final TraceId traceId = new TraceId(traceIdParam);

		ModelAndView mv = new ModelAndView("transactionInfo");

		try {
			// select spans
			List<SpanAlign> spanAligns = this.spanService.selectSpan(traceId);

			if (spanAligns.isEmpty()) {
				mv.addObject("errorCode", 9);
				mv.setViewName("error");
				return mv;
			}

			// debug
			mv.addObject("spanList", spanAligns);

			mv.addObject("traceId", traceId);

			// call tree
			ServerCallTree callTree = this.flow.selectServerCallTree(traceId);
			mv.addObject("nodes", callTree.getNodes());
			mv.addObject("links", callTree.getLinks());

			// call stacks
			RecordSet recordSet = this.recordSetService.createRecordSet(spanAligns, focusTimestamp);
			mv.addObject("recordSet", recordSet);

			mv.addObject("applicationName", recordSet.getApplicationName());
			mv.addObject("callstack", recordSet.getRecordList());
			mv.addObject("timeline", recordSet.getRecordList());
			mv.addObject("callstackStart", recordSet.getStartTime());
			mv.addObject("callstackEnd", recordSet.getEndTime());
		} catch (Exception e) {
			logger.warn("BusinessTransactionController Error Cause" + e.getMessage(), e);
			// TODO 아무래도 다시 던져야 될듯한데. Exception처리 정책을 생각해봐야 한다.
			// throw e;
		}

		return mv;
	}
}
