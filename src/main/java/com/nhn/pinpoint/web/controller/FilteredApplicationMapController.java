package com.nhn.pinpoint.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.nhn.pinpoint.web.util.LimitUtils;
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
import com.nhn.pinpoint.web.service.FilteredApplicationMapService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * 
 * @author netspider
 */
@Controller
public class FilteredApplicationMapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FilteredApplicationMapService filteredApplicationMapService;

    @Autowired
    private FilterBuilder filterBuilder;

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
											@RequestParam(value = "limit", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
        final Filter filter = filterBuilder.build(filterText);

        final LimitedScanResult<List<TransactionId>> limitedScanResult = filteredApplicationMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);

		ApplicationMap map = filteredApplicationMapService.selectApplicationMap(limitedScanResult.getScanData(), from, to, filter);
		
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
		model.addAttribute("timeseriesResponses", map.getTimeSeriesStore());

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
        limit = LimitUtils.checkRange(limit);

		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getFilteredServerMapData(model, response, applicationName, serviceType, from, to, filterText, limit); 
	}

}