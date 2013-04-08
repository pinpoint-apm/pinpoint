package com.nhn.hippo.web.controller;

import java.util.Set;

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

import com.nhn.hippo.web.applicationmap.ApplicationMap;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.service.ApplicationMapService;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.vo.TraceId;

/**
 * 
 * @author netspider
 */
@Controller
public class ApplicationMapController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FlowChartService flow;

	@Autowired
	private ApplicationMapService applicationMapService;

	@RequestMapping(value = "/getServerMapData2", method = RequestMethod.GET)
	public String getServerMapData2(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("serviceType") short serviceType, @RequestParam("from") long from, @RequestParam("to") long to) {
		ApplicationMap map = applicationMapService.selectApplicationMap(applicationName, serviceType, from, to);

		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());

		addResponseHeader(response);
		return "applicationmap";
	}

	@RequestMapping(value = "/getLastServerMapData2", method = RequestMethod.GET)
	public String getLastServerMapData2(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("serviceType") short serviceType, @RequestParam("period") long period) {
		long to = getQueryEndTime();
		long from = to - period;
		return getServerMapData2(model, response, applicationName, serviceType, from, to);
	}

	@RequestMapping(value = "/getServerMapData", method = RequestMethod.GET)
	public String getServerMapData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("from") long from, @RequestParam("to") long to) {
		// TODO 제거 하거나, interceptor로 할것.
		StopWatch watch = new StopWatch();
		watch.start("scanTraceindex");

		Set<TraceId> traceIdList = flow.selectTraceIdsFromApplicationTraceIndex(applicationName, from, to);

		watch.stop();
		logger.info("Fetch traceIdList elapsed : {}ms, {} traces", watch.getLastTaskTimeMillis(), traceIdList.size());
		watch.start("selectServerCallTree");

		ServerCallTree callTree = flow.selectServerCallTree(traceIdList, applicationName, from, to);

		watch.stop();
		logger.info("Fetch calltree time : {}ms", watch.getLastTaskTimeMillis());

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		addResponseHeader(response);

		return "servermap";
	}

	@RequestMapping(value = "/getLastServerMapData", method = RequestMethod.GET)
	public String getLastServerMapData(Model model, HttpServletResponse response, @RequestParam("application") String applicationName, @RequestParam("period") long period) {
		long to = getQueryEndTime();
		long from = to - period;
		return getServerMapData(model, response, applicationName, from, to);
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