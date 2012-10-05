package com.nhn.hippo.web.controller;

import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.calltree.RPCCallTree;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.vo.TraceId;

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
		System.out.println("");
		System.out.println("--------------------------------------");
		System.out.println("agentIds=" + Arrays.toString(agentIds));
		
		Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);
		for(TraceId tid : traceIds) {
			System.out.println("traceIds=" + tid);
		}
		System.out.println("--------------------------------------");
		System.out.println("");
		
		RPCCallTree callTree = flow.selectRPCCallTree(traceIds);

		model.addAttribute("nodes", callTree.getNodes());
		model.addAttribute("links", callTree.getLinks());

		System.out.println("");
		System.out.println("--------------------------------------");
		System.out.println(callTree.toString());

		return "flow";
	}
}