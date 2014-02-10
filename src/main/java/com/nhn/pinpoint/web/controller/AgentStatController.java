package com.nhn.pinpoint.web.controller;

import java.util.List;
import java.util.SortedMap;

import javax.servlet.http.HttpServletResponse;

import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.web.service.AgentInfoService;
import com.nhn.pinpoint.web.service.AgentStatService;
import com.nhn.pinpoint.web.vo.linechart.agentstat.AgentStatChartGroup;

@Controller
public class AgentStatController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AgentStatService agentStatService;
	
	@Autowired
	private AgentInfoService agentInfoService;
	
	@RequestMapping(value = "/getAgentStat", method = RequestMethod.GET)
	public @ResponseBody AgentStatChartGroup getAgentStat(Model model,
							HttpServletResponse response,
							@RequestParam("agentId") String agentId,
							@RequestParam("from") long from,
							@RequestParam("to") long to,
							@RequestParam(value = "sampleRate", required = false) Integer sampleRate,
							@RequestParam(value = "_", required = false) String jsonpCallback) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start("agentStatService.selectAgentStatList");
        Range range = new Range(from, to);
		List<TAgentStat> agentStatList = agentStatService.selectAgentStatList(agentId, range);
		watch.stop();
		
		if (logger.isInfoEnabled()) {
			logger.info("getAgentStat(agentId={}, from={}, to={}) : {}ms", agentId, from, to, watch.getLastTaskTimeMillis());
		}

		// FIXME dummy
		int nPoints = (int) (to - from) / 5000;
		if (sampleRate == null) {
			sampleRate = nPoints < 300 ? 1 : nPoints / 300;
		}
		
		AgentStatChartGroup chart = new AgentStatChartGroup();
		for (TAgentStat each : agentStatList) {
			chart.addData(each, sampleRate);
		}

		/* Deprecated
		response.setContentType("text/javascript; charset=UTF-8");
		PrintWriter out = response.getWriter();

		if (jsonpCallback != null) {
			out.write(jsonpCallback + "(" + jsonObjectMapper.writeValueAsString(chart) + ")");
		} else {
			out.write(jsonObjectMapper.writeValueAsString(chart));
		}
		
		out.close();
		*/
		return chart;
	}

	@RequestMapping(value = "/getAgentList", method = RequestMethod.GET)
	public @ResponseBody SortedMap<String, List<AgentInfoBo>> getApplicationAgentList(Model model, HttpServletResponse response,
											@RequestParam("application") String applicationName,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
											@RequestParam(value = "_", required = false) String jsonpCallback) {
        Range range = new Range(from, to);
		SortedMap<String, List<AgentInfoBo>> applicationAgentList = agentInfoService.getApplicationAgentList(applicationName, range);
		return applicationAgentList;
	}
}
