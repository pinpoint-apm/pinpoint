package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.view.tree.StaticTreeView;
import com.navercorp.pinpoint.web.view.tree.TreeView;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilterChain;
import com.navercorp.pinpoint.web.vo.agent.DefaultAgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(value = "/agents")
@Validated
public class AgentListController {
    private final AgentInfoService agentInfoService;

    private final SortByAgentInfo.Rules DEFAULT_SORT_BY = SortByAgentInfo.Rules.AGENT_ID_ASC;

    public AgentListController(AgentInfoService agentInfoService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @GetMapping(value = "/search-all")
    public TreeView<InstancesList<AgentAndStatus>> getAllAgentsList() {
        final long timestamp = System.currentTimeMillis();
        final AgentsMapByApplication<AgentAndStatus> allAgentsList = this.agentInfoService.getAllAgentsList(
                AgentStatusFilter::accept,
                Range.between(timestamp, timestamp)
        );
        return treeView(allAgentsList);
    }

    @GetMapping(value = "/search-all", params = {"from", "to"})
    public TreeView<InstancesList<AgentAndStatus>> getAllAgentsList(
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final AgentStatusFilter filter = new DefaultAgentStatusFilter(from);
        final AgentsMapByApplication<AgentAndStatus> allAgentsList = this.agentInfoService.getAllAgentsList(
                filter,
                Range.between(from, to)
        );
        return treeView(allAgentsList);
    }

    private static <T> TreeView<InstancesList<T>> treeView(AgentsMapByApplication<T> agentsListsList) {
        final List<InstancesList<T>> list = agentsListsList.getAgentsListsList();
        return new StaticTreeView<>(list);
    }

    @GetMapping(value = "/search-application", params = {"application"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy) {
        final SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORT_BY);
        final long timestamp = System.currentTimeMillis();
        final AgentStatusFilter runningAgentFilter = new AgentStatusFilterChain(AgentStatusFilter::filterRunning);
        final AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                runningAgentFilter,
                applicationName,
                Range.between(timestamp, timestamp),
                paramSortBy
        );
        return treeView(list);
    }

    @GetMapping(value = "/search-application", params = {"application", "from", "to"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy) {
        final SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORT_BY);
        final AgentStatusFilter currentRunFilter = new AgentStatusFilterChain(
                new DefaultAgentStatusFilter(from)
        );
        final AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                currentRunFilter,
                applicationName,
                Range.between(from, to),
                paramSortBy
        );
        return treeView(list);
    }

    private static TreeView<InstancesList<AgentStatusAndLink>> treeView(AgentsMapByHost agentsMapByHost) {
        final List<InstancesList<AgentStatusAndLink>> list = agentsMapByHost.getAgentsListsList();
        return new StaticTreeView<>(list);
    }

    @GetMapping(value = "/statistics")
    public TreeView<InstancesList<DetailedAgentInfo>> getAllAgentStatistics() {
        final long timestamp = System.currentTimeMillis();
        final AgentsMapByApplication<DetailedAgentInfo> allAgentsList =
                this.agentInfoService.getAllAgentsStatisticsList(
                        AgentStatusFilter::accept,
                        Range.between(timestamp, timestamp)
                );
        return treeView(allAgentsList);
    }

    @GetMapping(value = "/statistics", params = {"from", "to"})
    public TreeView<InstancesList<DetailedAgentInfo>> getAllAgentStatistics(
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        final AgentsMapByApplication<DetailedAgentInfo> allAgentsList =
                this.agentInfoService.getAllAgentsStatisticsList(
                        AgentStatusFilter::accept,
                        Range.between(from, to)
                );
        return treeView(allAgentsList);
    }

}
