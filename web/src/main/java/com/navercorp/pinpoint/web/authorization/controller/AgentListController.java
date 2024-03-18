package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.view.tree.StaticTreeView;
import com.navercorp.pinpoint.web.view.tree.TreeView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.InstancesListMap;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
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
    private final ServiceTypeRegistryService registry;
    private final ApplicationFactory applicationFactory;
    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final SortByAgentInfo.Rules DEFAULT_SORT_BY = SortByAgentInfo.Rules.AGENT_ID_ASC;

    public AgentListController(
            AgentInfoService agentInfoService,
            ServiceTypeRegistryService registry,
            ApplicationFactory applicationFactory,
            ResponseTimeHistogramService responseTimeHistogramService
    ) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
    }

    @GetMapping(value = "/search-all")
    public TreeView<InstancesList<AgentAndStatus>> getAllAgentsList() {
        final long timestamp = System.currentTimeMillis();
        final AgentsMapByApplication<AgentAndStatus> allAgentsList = this.agentInfoService.getAllAgentsList(
                AgentStatusFilters.acceptAll(),
                Range.between(timestamp, timestamp)
        );
        return treeView(allAgentsList);
    }

    @GetMapping(value = "/search-all", params = {"from", "to"})
    public TreeView<InstancesList<AgentAndStatus>> getAllAgentsList(
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final AgentStatusFilter filter = AgentStatusFilters.recentRunning(from);
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
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName") String serviceTypeName,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy) {
        final SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORT_BY);
        final long timestamp = System.currentTimeMillis();

        ServiceType serviceType = this.registry.findServiceTypeByName(serviceTypeName);
        if (serviceType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service type not found: " + serviceTypeName);
        }

        final AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                AgentStatusFilters.running(),
                AgentInfoFilters.exactServiceType(serviceTypeCode, serviceTypeName),
                applicationName,
                serviceType.getCode(),
                Range.between(timestamp, timestamp),
                paramSortBy
        );
        return treeView(list);
    }

    @GetMapping(value = "/search-application", params = {"application", "from", "to"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName") String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy
    ) {
        final SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORT_BY);

        ServiceType serviceType = this.registry.findServiceTypeByName(serviceTypeName);
        if (serviceType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service type not found: " + serviceTypeName);
        }

        final AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                AgentStatusFilters.recentRunning(from),
                AgentInfoFilters.exactServiceType(serviceTypeCode, serviceTypeName),
                applicationName,
                serviceType.getCode(),
                Range.between(from, to),
                paramSortBy
        );
        return treeView(list);
    }

    @GetMapping(value = "/search-application", params = {"application", "from", "to", "applicationPairs"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsListWithVirtualNodes(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy,
            @RequestParam(value = "applicationPairs", required = false) ApplicationPairs applicationPairs
    ) {
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        if (serviceType.isWas()) {
            return getAgentsList(
                    applicationName, serviceTypeCode, serviceTypeName, from, to, sortBy
            );
        }

        final Application application = applicationFactory.createApplication(applicationName, serviceType.getCode());

        final List<Application> fromApplications =
                pairsToList(applicationPairs.getFromApplications());
        final List<Application> toApplications =
                pairsToList(applicationPairs.getToApplications());
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption
                .Builder(application, Range.between(from, to),
                fromApplications, toApplications)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(
                option
        );

        final AgentsMapByHost list = extractVirtualNode(nodeHistogramSummary);
        return treeView(list);
    }

    private List<Application> pairsToList(List<ApplicationPair> applicationPairs) {
        if (CollectionUtils.isEmpty(applicationPairs)) {
            return Collections.emptyList();
        }
        final List<Application> applications = new ArrayList<>(applicationPairs.size());
        for (ApplicationPair applicationPair : applicationPairs) {
            final String applicationName = applicationPair.getApplicationName();
            final short serviceTypeCode = applicationPair.getServiceTypeCode();
            final Application application = this.applicationFactory.createApplication(applicationName, serviceTypeCode);
            applications.add(application);
        }
        return applications;
    }

    private AgentsMapByHost extractVirtualNode(NodeHistogramSummary nodeHistogramSummary) {
        List<InstancesList<AgentStatusAndLink>> listMap = new ArrayList<>();
        List<ServerGroup> groups = nodeHistogramSummary.getServerGroupList().getServerGroupList();
        for (ServerGroup group : groups) {
            List<AgentStatusAndLink> agents = new ArrayList<>();
            for (ServerInstance instance : group.getInstanceList()) {
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.setAgentId(AgentId.of(instance.getName()));
                agentInfo.setAgentName(instance.getAgentName());
                agentInfo.setServiceType(instance.getServiceType());

                AgentStatus agentStatus = new AgentStatus(instance.getName(), instance.getStatus(), 0);

                agents.add(
                        new AgentStatusAndLink(
                                agentInfo,
                                agentStatus,
                                group.getLinkList()
                        )
                );
            }
            InstancesList<AgentStatusAndLink> instancesList = new InstancesList<>(group.getHostName(), agents);

            listMap.add(instancesList);
        }
        return new AgentsMapByHost(new InstancesListMap<>(listMap));
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
                        AgentStatusFilters.acceptAll(),
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
                        AgentStatusFilters.acceptAll(),
                        Range.between(from, to)
                );
        return treeView(allAgentsList);
    }

}
