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

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.service.SpanService;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.RequestMetadataQuery;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.bo.SpanBo;

/**
 * retrieve data for drawing call tree.
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

	private void addResponseHeader(final HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*.*");
	}

	@Deprecated
	@RequestMapping(value = "/flowrpc", method = RequestMethod.GET)
	public String flowrpc(Model model, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		Set<TraceId> traceIds = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);

		RPCCallTree callTree = flow.selectRPCCallTree(traceIds);

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		logger.debug("callTree:{}", callTree);

		return "flow";
	}

	@RequestMapping(value = "/servermap", method = RequestMethod.GET)
	public String servermap(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
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

	@RequestMapping(value = "/businesstransactions", method = RequestMethod.GET)
	public String businesstransactions(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		// TOOD 구조개선을 위해 server map조회 로직 분리함, 임시로 분리한 상태이고 개선이 필요하다.

		Set<TraceId> traceIds = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);

		BusinessTransactions selectBusinessTransactions = flow.selectBusinessTransactions(traceIds, applicationName, from, to);

		model.addAttribute("businessTransactions", selectBusinessTransactions.getBusinessTransactionIterator());

		addResponseHeader(response);
		return "businesstransactions";
	}

	@RequestMapping(value = "/scatter", method = RequestMethod.GET)
	public String scatter(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

		List<Dot> scatterData = flow.selectScatterData(applicationName, from, to);
		watch.stop();

		logger.info("Fetch scatterData time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("scatter", scatterData);

		addResponseHeader(response);
		return "scatter";
	}
	
	@RequestMapping(value = "/scatter2", method = RequestMethod.GET)
	public String scatter2(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
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
	@RequestMapping(value = "/scatter2realtime", method = RequestMethod.GET)
	public String scatter2realtime(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("limit") int limit) {
		StopWatch watch = new StopWatch();
		watch.start("selectScatterData");

		long to = System.currentTimeMillis() - 3000L;
		
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

	// TODO UI에서 한꺼번에 많은 데이터를 조회하지 않도록 제한해야함.
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
	@RequestMapping(value = "/flow2", method = RequestMethod.GET)
	public String flowbyHost(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {
		String[] agentIds = flow.selectAgentIds(hosts);
		Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);

		RPCCallTree callTree = flow.selectRPCCallTree(traceIds);

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		logger.debug("callTree:{}", callTree);

		return "flow";
	}

	@Deprecated
	@RequestMapping(value = "/flowserver2", method = RequestMethod.GET)
	public String flowserverByHost(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {
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