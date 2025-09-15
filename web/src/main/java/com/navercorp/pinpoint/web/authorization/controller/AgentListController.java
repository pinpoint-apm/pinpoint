package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ApplicationAgentInfoMapServiceImpl;
import com.navercorp.pinpoint.web.service.ApplicationAgentListQueryRule;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(value = "/api/agents")
@Validated
public class AgentListController {
    private final AgentInfoService agentInfoService;
    private final ServiceTypeRegistryService registry;
    private final ApplicationFactory applicationFactory;
    private final ResponseTimeHistogramService responseTimeHistogramService;

    private final ApplicationAgentInfoMapServiceImpl applicationAgentInfoService;
    private final HyperLinkFactory hyperLinkFactory;
    private final SortByAgentInfo.Rules DEFAULT_SORT_BY = SortByAgentInfo.Rules.AGENT_ID_ASC;

    public AgentListController(
            AgentInfoService agentInfoService,
            ServiceTypeRegistryService registry,
            ApplicationFactory applicationFactory,
            ResponseTimeHistogramService responseTimeHistogramService,
            ApplicationAgentInfoMapServiceImpl applicationAgentInfoService,
            HyperLinkFactory hyperLinkFactory) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.applicationAgentInfoService = applicationAgentInfoService;
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
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
        final AgentStatusFilter filter = AgentStatusFilters.recentStatus(from);
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
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy,
            @RequestParam(value = "query", required = false) String query) {
        final SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORT_BY);
        final ApplicationAgentListQueryRule applicationAgentListQueryRule = ApplicationAgentListQueryRule.getByValue(query, ApplicationAgentListQueryRule.ALL);
        final long timestamp = System.currentTimeMillis();
        final Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);
        Range between = Range.between(timestamp, timestamp);
        TimeWindow timeWindow = new TimeWindow(between);
        final AgentsMapByHost list = this.applicationAgentInfoService.getAgentsListByApplicationName(
                application,
                timeWindow,
                paramSortBy,
                applicationAgentListQueryRule,
                AgentInfoFilters.acceptAll()
        );
        return treeView(list);
    }

    @GetMapping(value = "/search-application", params = {"application", "from", "to"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy,
            @RequestParam(value = "query", required = false) String query) {
        final SortByAgentInfo.Rules paramSortBy = sortBy.orElse(DEFAULT_SORT_BY);
        final ApplicationAgentListQueryRule applicationAgentListQueryRule = ApplicationAgentListQueryRule.getByValue(query, ApplicationAgentListQueryRule.ACTIVE_STATUS);
        final Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);
        Range between = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(between);

        final AgentsMapByHost list = this.applicationAgentInfoService.getAgentsListByApplicationName(
                application,
                timeWindow,
                paramSortBy,
                applicationAgentListQueryRule,
                AgentInfoFilters.acceptAll()
        );
        return treeView(list);
    }

    //use only for server map list
    @GetMapping(value = "/search-application", params = {"application", "from", "to", "applicationPairs"})
    public TreeView<InstancesList<AgentStatusAndLink>> getAgentsListWithVirtualNodes(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "sortBy") Optional<SortByAgentInfo.Rules> sortBy,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "applicationPairs", required = false) ApplicationPairs applicationPairs
    ) {
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        if (serviceType.isWas()) {
            final ApplicationAgentListQueryRule serverMapAgentListQueryRule = ApplicationAgentListQueryRule.getByValue(query, ApplicationAgentListQueryRule.ACTIVE_STATISTICS);
            return getAgentsList(
                    applicationName, serviceTypeCode, serviceTypeName, from, to, sortBy, String.valueOf(serverMapAgentListQueryRule)
            );
        }
        Range between = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(between);

        final Application application = applicationFactory.createApplication(applicationName, serviceType.getCode());

        final List<Application> fromApplications =
                pairsToList(applicationPairs.getFromApplications());
        final List<Application> toApplications =
                pairsToList(applicationPairs.getToApplications());
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption
                .Builder(application, timeWindow,
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
                agentInfo.setAgentId(instance.getName());
                agentInfo.setAgentName(instance.getAgentName(), instance.getName());
                agentInfo.setHostName(instance.getHostName());
                agentInfo.setIp(instance.getIp());
                agentInfo.setServiceType(instance.getServiceType());
                AgentStatus agentStatus = new AgentStatus(instance.getName(), instance.getStatus(), 0);

                AgentStatusAndLink agentStatusAndLik = new AgentStatusAndLink(
                        agentInfo,
                        agentStatus,
                        newHyperLink(hyperLinkFactory, instance)
                );
                agents.add(agentStatusAndLik);
            }
            InstancesList<AgentStatusAndLink> instancesList = new InstancesList<>(group.getHostName(), agents);

            listMap.add(instancesList);
        }
        return new AgentsMapByHost(new InstancesListMap<>(listMap));
    }

    private List<HyperLink> newHyperLink(HyperLinkFactory hyperLinkFactory, ServerInstance serverInstance) {
        if (serverInstance == null) {
            return List.of();
        }
        return hyperLinkFactory.build(LinkSources.from(serverInstance.getHostName(), serverInstance.getIp(), serverInstance.getServiceType()));
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

    private Application createApplication(String applicationName, Short serviceTypeCode, String serviceTypeName) {
        if (StringUtils.hasLength(applicationName)) {
            if (serviceTypeCode != null) {
                return applicationFactory.createApplication(applicationName, serviceTypeCode);
            } else if (serviceTypeName != null) {
                return applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
            }
        }
        // return application with without service type
        return new Application(applicationName, ServiceType.UNDEFINED);
    }

}
