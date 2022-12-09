package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.view.tree.StaticTreeView;
import com.navercorp.pinpoint.web.view.tree.TreeView;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilterChain;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.DefaultAgentInfoFilter;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(value = "/agents")
public class AgentListController {
    private final AgentInfoService agentInfoService;

    private SortByAgentInfo.Rules DEFAULT_SORTBY = SortByAgentInfo.Rules.AGENT_ID_ASC;

    public AgentListController(AgentInfoService agentInfoService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @GetMapping(value = "/search-all")
    public TreeView<InstancesList<AgentAndStatus>> getAllAgentsList() {
        long timestamp = System.currentTimeMillis();
        AgentsMapByApplication allAgentsList = this.agentInfoService.getAllAgentsList(
                AgentInfoFilter::accept,
                Range.between(timestamp, timestamp)
        );
        return treeView(allAgentsList);
    }

    @GetMapping(value = "/search-all", params = {"from", "to"})
    public TreeView<InstancesList<AgentAndStatus>> getAllAgentsList(
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        AgentInfoFilter filter = new DefaultAgentInfoFilter(from);
        AgentsMapByApplication allAgentsList = this.agentInfoService.getAllAgentsList(
                filter,
                Range.between(from, to)
        );
        return treeView(allAgentsList);
    }

    private static TreeView<InstancesList<AgentAndStatus>> treeView(AgentsMapByApplication agentsListsList) {
        List<InstancesList<AgentAndStatus>> list = agentsListsList.getAgentsListsList();
        return new StaticTreeView<>(list);
    }


    @GetMapping(value = "/search-application", params = {"application"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsList(
            @RequestParam("application") String applicationName,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy) {
        SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORTBY);
        long timestamp = System.currentTimeMillis();
        AgentInfoFilter runningAgentFilter = new AgentInfoFilterChain(
                AgentInfoFilter::filterRunning
        );
        AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                runningAgentFilter,
                applicationName,
                Range.between(timestamp, timestamp),
                paramSortBy
        );
        return treeView(list);
    }

    @GetMapping(value = "/search-application", params = {"application", "from", "to"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsList(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy) {
        SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORTBY);
        AgentInfoFilter currentRunFilter = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(from)
        );
        AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                currentRunFilter,
                applicationName,
                Range.between(from, to),
                paramSortBy
        );
        return treeView(list);
    }

    private static TreeView<InstancesList<AgentStatusAndLink>> treeView(AgentsMapByHost agentsMapByHost) {
        List<InstancesList<AgentStatusAndLink>> list = agentsMapByHost.getAgentsListsList();
        return new StaticTreeView<>(list);
    }

}
