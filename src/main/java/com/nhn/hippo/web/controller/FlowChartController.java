package com.nhn.hippo.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.calltree.RPCCallTree;
import com.nhn.hippo.web.service.FlowChartService;

/**
 * retrieve data for drawing call tree.
 * 
 * @author netspider
 * 
 */
@Controller
public class FlowChartController {

	@Autowired
	private FlowChartService flow;

	/**
	 * <pre>
	 * testurl = netscurl "http://localhost:7080/flow.hippo?host=TEST_AGENT_ID&from=1348565386677&to=1348565386677"
	 * </pre>
	 * 
	 * @param model
	 * @param hosts
	 * @param from
	 * @param to
	 * @return
	 */
	@RequestMapping(value = "/flow", method = RequestMethod.GET)
	public String arcus(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {
		String[] agentIds = flow.selectAgentIds(hosts);
		List<byte[]> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);
		RPCCallTree callTree = flow.selectRPCCallTree(traceIds);

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());
		model.addAttribute("value", "hello world");

		System.out.println(callTree.toString());

		return "flow";
	}
}