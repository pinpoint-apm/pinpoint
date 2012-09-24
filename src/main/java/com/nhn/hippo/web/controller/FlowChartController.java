package com.nhn.hippo.web.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.service.FlowChartService;

@Controller
public class FlowChartController {

	@Autowired
	private FlowChartService flow;

	/**
	 * <pre>
	 * testurl = http://localhost:7080/flow.hippo?host=TEST_AGENT_ID&from=1348453800000&to=1348453900000
	 * testurl = http://localhost:7080/flow.hippo?host=TEST_AGENT_ID&from=0&to=1348493900000
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
		/**
		 * get agentId list from 'Servers'
		 */
		String[] selectAgentIds = flow.selectAgentIds(hosts);

		System.out.println("selectedAgentIds=" + Arrays.toString(selectAgentIds));

		/**
		 * get traceId list from 'TraceIndex'
		 */
		List<byte[]> iterator = flow.selectTraceIdsFromTraceIndex(hosts, from, to);

		/**
		 * get all traces from 'Trace'
		 */
		flow.selectTraces(iterator);

		return "flow";
	}
}