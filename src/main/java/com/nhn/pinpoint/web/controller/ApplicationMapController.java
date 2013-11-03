package com.nhn.pinpoint.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.DateUtils;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.filter.FilterBuilder;
import com.nhn.pinpoint.web.service.ApplicationMapService;
import com.nhn.pinpoint.web.service.FlowChartService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * 
 * @author netspider
 */
@Controller
public class ApplicationMapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
	private ApplicationMapService applicationMapService;

	@Autowired
	private FlowChartService flow;

    @Autowired
    private FilterBuilder filterBuilder;

	/**
	 * FROM ~ TO기간의 서버 맵 데이터 조회
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @return
	 */
	@RequestMapping(value = "/getServerMapData", method = RequestMethod.GET)
	public String getServerMapData(Model model,
									HttpServletResponse response,
									@RequestParam("application") String applicationName, 
									@RequestParam("serviceType") short serviceType, 
									@RequestParam("from") long from,
									@RequestParam("to") long to) {
		
		ApplicationMap map = applicationMapService.selectApplicationMap(applicationName, serviceType, from, to);

		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());

		return "applicationmap";
	}

	/**
	 * Period before 부터 현재시간까지의 서버맵 조회.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param serviceType
	 * @param period
	 * @return
	 */
	@RequestMapping(value = "/getLastServerMapData", method = RequestMethod.GET)
	public String getLastServerMapData(Model model,
										HttpServletResponse response,
										@RequestParam("application") String applicationName,
										@RequestParam("serviceType") short serviceType,
										@RequestParam("period") long period) {
		
		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getServerMapData(model, response, applicationName, serviceType, from, to);
	}

	/**
	 * 필터가 적용된 서버맵의 FROM ~ TO기간의 데이터 조회
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @param filterText
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getFilteredServerMapData", method = RequestMethod.GET)
	public String getFilteredServerMapData(Model model,
											HttpServletResponse response,
											@RequestParam("application") String applicationName, 
											@RequestParam("serviceType") short serviceType,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {
		
		LimitedScanResult<List<TransactionId>> limitedScanResult = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);
		Filter filter = filterBuilder.build(filterText);
		
		ApplicationMap map = flow.selectApplicationMap(limitedScanResult.getScanData(), /*from, to,*/ filter);
		
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("filter", filter);
		model.addAttribute("lastFetchedTimestamp", limitedScanResult.getLimitedTime());
        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData range scan(limit:{}) from~to:{} ~ {} lastFetchedTimestamp:{}", limit, DateUtils.longToDateStr(from), DateUtils.longToDateStr(to), DateUtils.longToDateStr(limitedScanResult.getLimitedTime()));
        }

		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());
		
		// FIXME linkstatistics detail에 보여주는 timeseries값을 서버맵에서 제공할 예정.
		// model.addAttribute("timeseriesResponses", map.getTimeseriesResponses());

		return "applicationmap.filtered";
	}
	
	/**
	 * 필터가 적용된 서버맵의 Period before 부터 현재시간까지의 데이터 조회.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param serviceType
	 * @param filterText
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getLastFilteredServerMapData", method = RequestMethod.GET)
	public String getLastFilteredServerMapData(Model model,
			HttpServletResponse response,
			@RequestParam("application") String applicationName, 
			@RequestParam("serviceType") short serviceType,
			@RequestParam("period") long period,
			@RequestParam(value = "filter", required = false) String filterText,
			@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {

		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getFilteredServerMapData(model, response, applicationName, serviceType, from, to, filterText, limit); 
	}
	
	/**
	 * 필터가 사용되지 않은 서버맵의 연결선을 통과하는 요청의 통계정보 조회
	 * 
	 * @param model
	 * @param response
	 * @param from
	 * @param to
	 * @param srcApplicationName
	 * @param srcServiceType
	 * @param destApplicationName
	 * @param destServiceType
	 * @return
	 */
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
	
	/**
	 * 필터가 사용된 서버맵의 연결선을 통과하는 요청의 통계정보 조회
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @param srcApplicationName
	 * @param srcServiceType
	 * @param destApplicationName
	 * @param destServiceType
	 * @param filterText
	 * @param limit
	 * @return
	 */
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
		
		LimitedScanResult<List<TransactionId>> traceIdSet = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);
		Filter filter = filterBuilder.build(filterText);
		LinkStatistics linkStatistics = flow.linkStatisticsDetail(from, to, traceIdSet.getScanData(), srcApplicationName, srcServiceType, destApplicationName, destServiceType, filter);
		
		model.addAttribute("lastFetchedTimestamp", traceIdSet.getLimitedTime());
		model.addAttribute("linkStatistics", linkStatistics);
		
		return "linkStatisticsDetail";
	}
}