package com.nhn.pinpoint.web.controller;

import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.filter.FilterBuilder;
import com.nhn.pinpoint.web.service.ApplicationMapService;
import com.nhn.pinpoint.web.service.FlowChartService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.ResultWithMark;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * 
 * @author netspider
 */
@Controller
public class ApplicationMapController {

	@Autowired
	private ApplicationMapService applicationMapService;

	@Autowired
	private FlowChartService flow;

	@RequestMapping(value = "/getServerMapData2", method = RequestMethod.GET)
	public String getServerMapData2(Model model,
									HttpServletResponse response,
									@RequestParam("application") String applicationName, 
									@RequestParam("serviceType") short serviceType, 
									@RequestParam("from") long from,
									@RequestParam("to") long to,
									@RequestParam(value = "hideIndirectAccess", defaultValue = "false") boolean hideIndirectAccess) {
		
		ApplicationMap map = applicationMapService.selectApplicationMap(applicationName, serviceType, from, to, hideIndirectAccess);

		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());

		return "applicationmap";
	}

	@RequestMapping(value = "/getLastServerMapData2", method = RequestMethod.GET)
	public String getLastServerMapData2(Model model,
										HttpServletResponse response,
										@RequestParam("application") String applicationName,
										@RequestParam("serviceType") short serviceType,
										@RequestParam("period") long period,
										@RequestParam(value = "hideIndirectAccess", defaultValue = "false") boolean hideIndirectAccess) {
		
		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getServerMapData2(model, response, applicationName, serviceType, from, to, hideIndirectAccess);
	}

	@RequestMapping(value = "/filtermap", method = RequestMethod.GET)
	public String filtermap(Model model,
							HttpServletResponse response,
							@RequestParam("application") String applicationName, 
							@RequestParam("serviceType") short serviceType,
							@RequestParam("from") long from,
							@RequestParam("to") long to, 
							@RequestParam(value = "filter", required = false) String filterText) {
		
		model.addAttribute("applicationName", applicationName);
		model.addAttribute("serviceType", serviceType);
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("fromDate", new Date(from));
		model.addAttribute("toDate", new Date(to));
		model.addAttribute("filterText", filterText);
		model.addAttribute("filter", FilterBuilder.build(filterText));

		return "applicationmap.filtered.view";
	}

//	@Deprecated
//	@RequestMapping(value = "/getFilteredServerMapData", method = RequestMethod.GET)
//	public String getFilteredServerMapData(Model model,
//											HttpServletResponse response,
//											@RequestParam("application") String applicationName, 
//											@RequestParam("serviceType") short serviceType,
//											@RequestParam("from") long from,
//											@RequestParam("to") long to,
//											@RequestParam(value = "filter", required = false) String filterText,
//											@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {
//		ResultWithMark<Set<TransactionId>, Long> traceIdSet = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);
//		Filter filter = FilterBuilder.build(filterText);
//		ServerCallTree map = flow.selectServerCallTree(traceIdSet.getValue(), filter);
//		
//		model.addAttribute("nodes", map.getNodes());
//		model.addAttribute("links", map.getLinks());
//		model.addAttribute("filter", filter);
//
//		return "applicationmap.filtered";
//	}
	
	@RequestMapping(value = "/getFilteredServerMapData2", method = RequestMethod.GET)
	public String getFilteredServerMapData2(Model model,
											HttpServletResponse response,
											@RequestParam("application") String applicationName, 
											@RequestParam("serviceType") short serviceType,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {
		
		ResultWithMark<Set<TransactionId>, Long> traceIdSet = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);
		Filter filter = FilterBuilder.build(filterText);
		
		ApplicationMap map = flow.selectApplicationMap(traceIdSet.getValue(), from, to, filter);
		
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("filter", filter);
		model.addAttribute("lastFetchedTimestamp", traceIdSet.getMark());
		
		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());
//		model.addAttribute("timeseriesResponses", map.getTimeseriesResponses());

		return "applicationmap.filtered2";
	}
	
	// 선택한 연결선을 통과하는 요청의 통계 정보 조회.
	// 필터 사용 안함.
	@RequestMapping(value = "/linkStatistics", method = RequestMethod.GET)
	public String getLinkStatistics(Model model,
									HttpServletResponse response, 
									@RequestParam("from") long from,
									@RequestParam("to") long to,
									@RequestParam("srcApplicationName") String srcApplicationName,
									@RequestParam("srcServiceType") short srcServiceType,
									@RequestParam("destApplicationName") String destApplicationName,
									@RequestParam("destServiceType") short destServiceType) {
		
		LinkStatistics linkStatistics = flow.linkStatistics(from, to, srcApplicationName, srcServiceType, destApplicationName, destServiceType);

		model.addAttribute("from", from);
		model.addAttribute("to", to);

		model.addAttribute("srcApplicationName", srcApplicationName);
		model.addAttribute("destApplicationName", destApplicationName);

		model.addAttribute("srcApplicationType", ServiceType.findServiceType(srcServiceType));
		model.addAttribute("destApplicationType", ServiceType.findServiceType(destServiceType));

		model.addAttribute("linkStatistics", linkStatistics);
		model.addAttribute("histogramSummary", linkStatistics.getHistogramSummary().entrySet().iterator());
		model.addAttribute("timeseriesSlotIndex", linkStatistics.getTimeseriesSlotIndex().entrySet().iterator());
		model.addAttribute("timeseriesValue", linkStatistics.getTimeseriesValue());

		// FIXME lastFetchedTimestamp는 filtered에서만 사용되는 값으로 여기에서는 필요 없음. 일단 임시방편으로 -1로 세팅.
		model.addAttribute("lastFetchedTimestamp", -1);
		
		return "linkStatisticsDetail";
	}
	
	// 선택한 연결선을 통과하는 요청의 통계 정보 조회.
	// 필터 사용.
	@RequestMapping(value = "/filteredLinkStatistics", method = RequestMethod.GET)
	public String getFilteredLinkStatistics(Model model,
									HttpServletResponse response, 
									@RequestParam("application") String applicationName,
									@RequestParam("serviceType") short serviceType,
									@RequestParam("from") long from,
									@RequestParam("to") long to,
									@RequestParam("srcApplicationName") String srcApplicationName,
									@RequestParam("srcServiceType") short srcServiceType,
									@RequestParam("destApplicationName") String destApplicationName,
									@RequestParam("destServiceType") short destServiceType,
									@RequestParam(value = "filter", required = false) String filterText,
									@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {
		
		ResultWithMark<Set<TransactionId>, Long> traceIdSet = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);
		Filter filter = FilterBuilder.build(filterText);
		LinkStatistics linkStatistics = flow.linkStatisticsDetail(from, to, traceIdSet.getValue(), srcApplicationName, srcServiceType, destApplicationName, destServiceType, filter);
		
		model.addAttribute("lastFetchedTimestamp", traceIdSet.getMark());
		model.addAttribute("linkStatistics", linkStatistics);
		
		return "linkStatisticsDetail";
	}
	
//	@Deprecated
//	@RequestMapping(value = "/applicationStatistics", method = RequestMethod.GET)
//	public String getLinkStatistics(Model model,
//			HttpServletResponse response, 
//			@RequestParam("from") long from,
//			@RequestParam("to") long to,
//			@RequestParam("applicationName") String applicationName,
//			@RequestParam("serviceType") short serviceType) {
//		
//		ApplicationStatistics stat = applicationMapService.selectApplicationStatistics(applicationName, serviceType, from, to);
//		
//		model.addAttribute("from", from);
//		model.addAttribute("to", to);
//		model.addAttribute("applicationStatistics", stat);
//
//		return "applicationStatisticsDetail";
//	}
}