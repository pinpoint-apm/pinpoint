package com.nhn.hippo.web.controller;

import java.util.Iterator;
import java.util.Map;

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

	@RequestMapping(value = "/flow", method = RequestMethod.GET)
	public String arcus(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {

		String[] selectAgentIds = flow.selectAgentIds(hosts);
		
		Iterator<Map<String, Object>> iterator = flow.selectTraces(hosts, from, to);

		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}

		return "flow";
	}
}