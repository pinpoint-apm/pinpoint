package com.nhn.pinpoint.web.controller;

import java.io.PrintWriter;
import java.util.List;
import java.util.SortedMap;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.web.service.AgentInfoService;
import com.nhn.pinpoint.web.service.AgentStatService;
import com.nhn.pinpoint.web.vo.linechart.AgentStatLineChart;

@Controller
public class AgentStatController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AgentStatService agentStatService;
	
	@Autowired
	private AgentInfoService agentInfoService;
	
	@Autowired
	@Qualifier("jsonObjectMapper")
	private ObjectMapper jsonObjectMapper; // it's thread-safe
	
	@RequestMapping(value = "/getAgentStat", method = RequestMethod.GET)
	public void getAgentStat(Model model,
							HttpServletResponse response,
							@RequestParam("agentId") String agentId,
							@RequestParam("from") long from,
							@RequestParam("to") long to,
							@RequestParam(value = "_callback", required = false) String jsonpCallback) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start("getAgentStat");
		
		List<TAgentStat> agentStatList = agentStatService.selectAgentStatList(agentId, from, to);
		
		watch.stop();
		if (logger.isInfoEnabled()) {
			logger.info("getAgentStat(agentId={}, from={}, to={}) : {}ms", agentId, from, to, watch.getLastTaskTimeMillis());
		}

		AgentStatLineChart chart = new AgentStatLineChart();
		for (TAgentStat each : agentStatList) {
			chart.addData(each);
		}
		
		// JSON or JSONP response
		response.setContentType("text/javascript; charset=UTF-8");
		PrintWriter out = response.getWriter();

		if (jsonpCallback != null) {
			out.write(jsonpCallback + "(" + jsonObjectMapper.writeValueAsString(chart) + ")");
		} else {
			out.write(jsonObjectMapper.writeValueAsString(chart));
		}
		
		out.close();
	}

	@RequestMapping(value = "/getAgentList", method = RequestMethod.GET)
	public String getApplicationAgentList(Model model, HttpServletResponse response,
											@RequestParam("application") String applicationName,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
											@RequestParam(value = "_callback", required = false) String jsonpCallback) {
		
		SortedMap<String, List<AgentInfoBo>> applicationAgentList = agentInfoService.getApplicationAgentList(applicationName, from, to);
		model.addAttribute("applicationAgentList", applicationAgentList);
		return "agentList";
	}
}
