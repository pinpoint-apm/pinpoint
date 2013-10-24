package com.nhn.pinpoint.web.controller;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.service.FlowChartService;
import com.nhn.pinpoint.web.service.MonitorService;
import com.nhn.pinpoint.web.vo.AgentStatus;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author netspider
 */
@Controller
public class MainController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FlowChartService flow;

	@Autowired
	private MonitorService monitor;

	@RequestMapping(value = "/applications", method = RequestMethod.GET)
	public String flow(Model model, HttpServletResponse response) {
		List<Application> applications = flow.selectAllApplicationNames();
		model.addAttribute("applications", applications);

		logger.debug("Applications, {}", applications);

		return "applications";
	}

	@RequestMapping(value = "/agentStatus", method = RequestMethod.GET)
	public String agentStatus(Model model, HttpServletResponse response, @RequestParam("agentId") List<String> agentIdList) {
		SortedMap<String, AgentStatus> statusMap = new TreeMap<String, AgentStatus>();

		for (String agentId : agentIdList) {
			AgentInfoBo agentInfo = monitor.getAgentInfo(agentId);
			statusMap.put(agentId, new AgentStatus(agentInfo));
		}

		model.addAttribute("statusMap", statusMap);

		return "agentstatus";
	}

	@RequestMapping(value = "/serverTime", method = RequestMethod.GET)
	public String getServerTime(Model model, HttpServletResponse response) {
		model.addAttribute("currentServerTime", System.currentTimeMillis());
		return "serverTime";
	}
}