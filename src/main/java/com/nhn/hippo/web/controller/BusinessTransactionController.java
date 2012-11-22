package com.nhn.hippo.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.service.SpanService;
import com.nhn.hippo.web.vo.TraceId;

/**
 *
 */
@Controller
public class BusinessTransactionController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SpanService spanService;

	@Autowired
	private FlowChartService flow;

	@RequestMapping(value = "/selectTransaction", method = RequestMethod.GET)
	public ModelAndView flow(@RequestParam(value = "traceId") String traceId) {
		logger.debug("traceId:{}", traceId);
		List<SpanAlign> spanAligns = spanService.selectSpan(traceId);

		ModelAndView mv = new ModelAndView("selectTransaction");
		mv.addObject("spanList", spanAligns);
		mv.addObject("traceId", traceId);

		Set<TraceId> traceIds = new HashSet<TraceId>(1);
		traceIds.add(new TraceId(UUID.fromString(traceId)));
		ServerCallTree callTree = flow.selectServerCallTree(traceIds);
		mv.addObject("nodes", callTree.getNodes());
		mv.addObject("links", callTree.getLinks());

		RPCCallTree rpcTree = flow.selectRPCCallTree(traceIds);
		mv.addObject("rpcnodes", rpcTree.getNodes());
		mv.addObject("rpclinks", rpcTree.getLinks());
		
		return mv;
	}
}
