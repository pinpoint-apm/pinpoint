package com.nhn.hippo.web.controller;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.service.SpanService;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.RequestMetadataQuery;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.bo.SpanBo;

/**
 * 각종 차트의 데이터를 조회한다.
 * 
 * @author netspider
 */
@Controller
public class FlowChartController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FlowChartService flow;

	@Autowired
	private SpanService spanService;

	// Ajax UI개발 테스트를 위해 추가함. crossdomain문제 해결용도.
	private void addResponseHeader(final HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*.*");
	}

	/**
	 * 항상 3초 전 데이터를 조회한다. 이것은 collector에서 지연되는 상황에 대한 처리.
	 * 
	 * @return
	 */
	private long getQueryEndTime() {
		return System.currentTimeMillis() - 3000L;
	}

	@RequestMapping(value = "/getServerMapData", method = RequestMethod.GET)
	public String getServerMapData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		// TODO 제거 하거나, interceptor로 할것.
		StopWatch watch = new StopWatch();
		watch.start("scanTraceindex");

		Set<TraceId> traceIds = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);

		watch.stop();
		logger.info("Fetch traceIds elapsed : {}ms, {} traces", watch.getLastTaskTimeMillis(), traceIds.size());
		watch.start("selectServerCallTree");

		ServerCallTree callTree = flow.selectServerCallTree(traceIds, applicationName, from, to);

		watch.stop();
		logger.info("Fetch calltree time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		logger.debug("callTree:{}", callTree);

		addResponseHeader(response);
		return "servermap";
	}

	@RequestMapping(value = "/getLastServerMapData", method = RequestMethod.GET)
	public String getLastServerMapData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period) {
		long to = getQueryEndTime();
		long from = to - period;
		return getServerMapData(model, response, applicationName, from, to);
	}

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
	@RequestMapping(value = "/getBusinessTransactionsData", method = RequestMethod.GET)
	public String getBusinessTransactionsData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		// TOOD 구조개선을 위해 server map조회 로직 분리함, 임시로 분리한 상태이고 개선이 필요하다.

		Set<TraceId> traceIds = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);

		BusinessTransactions selectBusinessTransactions = flow.selectBusinessTransactions(traceIds, applicationName, from, to);

		model.addAttribute("businessTransactions", selectBusinessTransactions.getBusinessTransactionIterator());

		addResponseHeader(response);
		return "businesstransactions";
	}

	@RequestMapping(value = "/getLastBusinessTransactionsData", method = RequestMethod.GET)
	public String getLastBusinessTransactionsData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period) {
		long to = getQueryEndTime();
		long from = to - period;
		return getBusinessTransactionsData(model, response, applicationName, from, to);
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
	public String getScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

		List<Dot> scatterData = flow.selectScatterData(applicationName, from, to, limit);
		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("scatter", scatterData);

		addResponseHeader(response);
		return "scatter";
	}

	/**
	 * NOW 버튼을 눌렀을 때 scatter 데이터 조회.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getLastScatterData", method = RequestMethod.GET)
	public String getLastScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period, @RequestParam("limit") int limit) {
		long to = getQueryEndTime();
		long from = to - period;
		return getScatterData(model, response, applicationName, from, to, limit);
	}

	/**
	 * scatter 실시간 갱신에서는 to 시간을 지정하지 않는다. server time을 사용하고 조회된 시간 범위를 반환해준다.
	 * UI에서는 반환된 조회 범위를 참조해서 다음 쿼리를 요청한다.
	 * 
	 * @param model
	 * @param response
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getRealtimeScatterData", method = RequestMethod.GET)
	public String getRealtimeScatterData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("limit") int limit) {
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

		long to = getQueryEndTime();

		List<Dot> scatterData = flow.selectScatterData(applicationName, from, to, limit);
		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("scatter", scatterData);
		model.addAttribute("queryFrom", from);

		if (scatterData.size() >= limit) {
			model.addAttribute("queryTo", scatterData.get(scatterData.size() - 1).getTimestamp());
		} else {
			model.addAttribute("queryTo", to);
		}

		model.addAttribute("limit", limit);

		addResponseHeader(response);
		return "scatterRealtime";
	}

	/**
	 * scatter에서 점 여러개를 선택했을 때 점에 대한 정보를 조회한다.
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/requestmetadata", method = RequestMethod.GET)
	public String requestmetadata(Model model, HttpServletRequest request, HttpServletResponse response) {
		String TRACEID = "tr";
		String TIME = "ti";
		String RESPONSE_TIME = "re";

		RequestMetadataQuery query = new RequestMetadataQuery();

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
			List<SpanBo> metadata = spanService.selectRequestMetadata(query);
			model.addAttribute("metadata", metadata);
		}

		addResponseHeader(response);
		return "requestmetadata";
	}

	@Deprecated
	@RequestMapping(value = "/flowserverByHost", method = RequestMethod.GET)
	public String flowServerByHost(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {
		String[] agentIds = flow.selectAgentIds(hosts);

		// TODO 제거 하거나, interceptor로 할것.
		StopWatch watch = new StopWatch();
		watch.start("scanTraceindex");

		Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);

		watch.stop();
		logger.info("time:{} {}", watch.getLastTaskTimeMillis(), traceIds.size());
		watch.start("selectServerCallTree");

		ServerCallTree callTree = flow.selectServerCallTree(traceIds);

		watch.stop();
		logger.info("time:{}", watch.getLastTaskTimeMillis());

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		logger.debug("callTree:{}", callTree);

		return "flowserver";
	}
}