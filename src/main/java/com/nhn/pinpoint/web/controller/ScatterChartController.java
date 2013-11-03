package com.nhn.pinpoint.web.controller;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nhn.pinpoint.common.util.DateUtils;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.filter.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.web.service.FlowChartService;
import com.nhn.pinpoint.web.service.ScatterChartService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.TransactionMetadataQuery;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 * 
 * @author netspider
 */
@Controller
public class ScatterChartController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ScatterChartService scatter;
	
	@Autowired
	private FlowChartService flow;

    @Autowired
    private FilterBuilder filterBuilder;

//	@RequestMapping(value = "/selectedScatter", method = RequestMethod.GET)
//	public String selectedScatter(Model model, HttpServletResponse response) {
//		return "selectedScatter";
//	}
//
//	@RequestMapping(value = "/selectedScatterList", method = RequestMethod.GET)
//	public String selectedScatterList(Model model, HttpServletResponse response) {
//		return "selectedScatterList";
//	}

	@RequestMapping(value = "/scatterpopup", method = RequestMethod.GET)
	public String scatterPopup(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("period") long period, @RequestParam("usePeriod") boolean usePeriod, @RequestParam(value = "filter", required = false) String filterText) {
		model.addAttribute("applicationName", applicationName);
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("period", period);
		model.addAttribute("usePeriod", usePeriod);
		model.addAttribute("filter", filterText);
		return "scatterPopup";
	}

	/**
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 *            한번에 조회 할 데이터의 크기, 조회 결과가 이 크기를 넘어가면 limit개만 반환한다. 나머지는 다시 요청해서
	 *            조회해야 한다.
	 * @return
	 */
	@RequestMapping(value = "/getScatterData", method = RequestMethod.GET)
	public String getScatterData(Model model,
								HttpServletResponse response,
								@RequestParam("application") String applicationName, 
								@RequestParam("from") long from, 
								@RequestParam("to") long to,
								@RequestParam("limit") int limit, 
								@RequestParam(value = "filter", required = false) String filterText, 
								@RequestParam(value = "_callback", required = false) String jsonpCallback,
								@RequestParam(value = "v", required = false, defaultValue = "1") int version) {

		logger.debug("fetch scatter data FROM={}, TO={}, LIMIT={}, FILTER={}", from, to, limit, filterText);
		
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");
		
		List<Dot> scatterData;
		if (filterText == null) {
			// FIXME ResultWithMark로 변경해야할지도?
			scatterData = scatter.selectScatterData(applicationName, from, to, limit);
			
			if (scatterData.isEmpty()) {
				model.addAttribute("resultFrom", -1);
				model.addAttribute("resultTo", -1);
			} else {
				model.addAttribute("resultFrom", scatterData.get(scatterData.size() - 1).getTimestamp());
				model.addAttribute("resultTo", to);
			}
		} else {
			final LimitedScanResult<List<TransactionId>> limitedScanResult = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to, limit);
			List<TransactionId> traceIdList = limitedScanResult.getScanData();
			logger.debug("selected traceidList {}", traceIdList);
			
			SortedSet<TransactionId> traceIdSet = new TreeSet<TransactionId>(traceIdList);
			logger.debug("selectScatterData with {}", traceIdSet);

            Filter filter = filterBuilder.build(filterText);
            scatterData = scatter.selectScatterData(traceIdSet, applicationName, filter);

			if (traceIdList.isEmpty()) {
				model.addAttribute("resultFrom", -1);
				model.addAttribute("resultTo", -1);
			} else {
				model.addAttribute("resultFrom", limitedScanResult.getLimitedTime());
				model.addAttribute("resultTo", to);
                if (logger.isDebugEnabled()) {
                    logger.debug("getScatterData range scan(limited:{}) from ~ to:{} ~ {} limited:{} filterDataSize:{}",
                            limit, DateUtils.longToDateStr(from), DateUtils.longToDateStr(to), DateUtils.longToDateStr(limitedScanResult.getLimitedTime()), traceIdList.size());
                }
			}
		}

		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("scatter", scatterData);

		// TODO version은 임시로 사용됨. template변경과 서버개발을 동시에 하려고. 변경 후 삭제예정.
		if (jsonpCallback == null) {
			return "scatter_json" + ((version > 1) ? version : "");
		} else {
			model.addAttribute("callback", jsonpCallback);
			return "scatter_jsonp" + ((version > 1) ? version : "");
		}
	}

	/**
	 * NOW 버튼을 눌렀을 때 scatter 데이터 조회.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getLastScatterData", method = RequestMethod.GET)
	public String getLastScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period, @RequestParam("limit") int limit, @RequestParam(value = "filter", required = false) String filterText, @RequestParam(value = "_callback", required = false) String jsonpCallback, @RequestParam(value = "v", required = false, defaultValue = "1") int version) {
		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		// TODO version은 임시로 사용됨. template변경과 서버개발을 동시에 하려고..
		return getScatterData(model, response, applicationName, from, to, limit, filterText, jsonpCallback, version);
	}

//	/**
//	 * scatter 실시간 갱신에서는 to 시간을 지정하지 않는다. server time을 사용하고 조회된 시간 범위를 반환해준다.
//	 * UI에서는 반환된 조회 범위를 참조해서 다음 쿼리를 요청한다.
//	 * 
//	 * @param model
//	 * @param response
//	 * @param applicationName
//	 * @param from
//	 * @param limit
//	 * @return
//	 */
//	@RequestMapping(value = "/getRealtimeScatterData", method = RequestMethod.GET)
//	public String getRealtimeScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("limit") int limit) {
//		StopWatch watch = new StopWatch();
//		watch.start("selectScatterData");
//
//		long to = TimeUtils.getDelayLastTime();
//
//		// TODO need filter??
//		List<Dot> scatterData = scatter.selectScatterData(applicationName, from, to, limit);
//		watch.stop();
//
//		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());
//
//		model.addAttribute("scatter", scatterData);
//		model.addAttribute("queryFrom", from);
//
//		if (scatterData.size() >= limit) {
//			model.addAttribute("queryTo", scatterData.get(scatterData.size() - 1).getTimestamp());
//		} else {
//			model.addAttribute("queryTo", to);
//		}
//
//		model.addAttribute("limit", limit);
//
//		return "scatterRealtime";
//	}

	/**
	 * scatter에서 점 여러개를 선택했을 때 점에 대한 정보를 조회한다.
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/transactionmetadata", method = RequestMethod.POST)
	public String transactionmetadata(Model model, HttpServletRequest request, HttpServletResponse response) {
		String TRACEID = "I";
		String TIME = "T";
		String RESPONSE_TIME = "R";

		TransactionMetadataQuery query = new TransactionMetadataQuery();

		int index = 0;
		while (true) {
			String traceId = request.getParameter(TRACEID + index);
			String time = request.getParameter(TIME + index);
			String responseTime = request.getParameter(RESPONSE_TIME + index);

			if (traceId == null || time == null || responseTime == null) {
				break;
			}

			query.addQueryCondition(traceId, Long.parseLong(time), Integer.parseInt(responseTime));
			index++;
		}

		if (query.size() > 0) {
			List<SpanBo> metadata = scatter.selectTransactionMetadata(query);
			model.addAttribute("metadata", metadata);
		}

		return "transactionmetadata";
	}
}