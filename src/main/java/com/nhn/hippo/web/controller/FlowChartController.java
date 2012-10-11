package com.nhn.hippo.web.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.service.FlowChartService;
import com.nhn.hippo.web.vo.TraceId;

/**
 * retrieve data for drawing call tree.
 *
 * @author netspider
 */
@Controller
public class FlowChartController {

    @Autowired
    private FlowChartService flow;

    @RequestMapping(value = "/flow", method = RequestMethod.GET)
    public String flow(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {
        String[] agentIds = flow.selectAgentIds(hosts);
        Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);

        RPCCallTree callTree = flow.selectRPCCallTree(traceIds);

        model.addAttribute("nodes", callTree.getNodes());
        model.addAttribute("links", callTree.getLinks());

        System.out.println(callTree.toString());

        return "flow";
    }

    @RequestMapping(value = "/flowserver", method = RequestMethod.GET)
    public String flowserver(Model model, @RequestParam("host") String[] hosts, @RequestParam("from") long from, @RequestParam("to") long to) {
        String[] agentIds = flow.selectAgentIds(hosts);
        Set<TraceId> traceIds = flow.selectTraceIdsFromTraceIndex(agentIds, from, to);

        ServerCallTree callTree = flow.selectServerCallTree(traceIds);

        model.addAttribute("nodes", callTree.getNodes());
        model.addAttribute("links", callTree.getLinks());
        model.addAttribute("businessTransactions", callTree.getBusinessTransactions().iterator());

        System.out.println(callTree.toString());

        return "flowserver";
    }
}