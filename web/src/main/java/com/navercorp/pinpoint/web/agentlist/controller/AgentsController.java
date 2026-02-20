/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.agentlist.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.agentlist.AgentsFactory;
import com.navercorp.pinpoint.web.agentlist.service.AgentsService;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.service.ApplicationAgentListQueryRule;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import com.navercorp.pinpoint.web.vo.Service;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(value = "/api/agents")
@Validated
public class AgentsController {

    private final ServiceTypeRegistryService registry;
    private final ApplicationFactory applicationFactory;
    private final ResponseTimeHistogramService responseTimeHistogramService;

    private final AgentsService agentsService;
    private final HyperLinkFactory hyperLinkFactory;
    private final RangeValidator rangeValidator;

    public AgentsController(
            ServiceTypeRegistryService registry,
            ApplicationFactory applicationFactory,
            ResponseTimeHistogramService responseTimeHistogramService,
            AgentsService agentsService,
            HyperLinkFactory hyperLinkFactory,
            ConfigProperties configProperties) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.agentsService = Objects.requireNonNull(agentsService, "agentsService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        Objects.requireNonNull(configProperties, "configProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(configProperties.getInspectorPeriodMax()));
    }


    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/overview", params = {"application"})
    public List<AgentStatusAndLink> getAgentsList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam(value = "query", required = false) String query) {
        final ApplicationAgentListQueryRule applicationAgentListQueryRule = ApplicationAgentListQueryRule
                .getByValue(query, ApplicationAgentListQueryRule.ALL);
        final long timestamp = System.currentTimeMillis();
        final Application application = createApplication(Service.DEFAULT, applicationName, serviceTypeCode, serviceTypeName);
        Range between = Range.between(timestamp, timestamp);
        TimeWindow timeWindow = new TimeWindow(between);
        return agentsService.getAgentsByApplicationName(
                application,
                timeWindow,
                applicationAgentListQueryRule,
                AgentInfoFilters.acceptAll()
        );
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/overview", params = {"application", "from", "to"})
    public List<AgentStatusAndLink> getAgentsList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "query", required = false) String query) {
        final ApplicationAgentListQueryRule applicationAgentListQueryRule = ApplicationAgentListQueryRule.getByValue(query, ApplicationAgentListQueryRule.ACTIVE_STATUS);
        final Application application = createApplication(Service.DEFAULT, applicationName, serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);
        rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range);

        return agentsService.getAgentsByApplicationName(
                application,
                timeWindow,
                applicationAgentListQueryRule,
                AgentInfoFilters.acceptAll()
        );
    }


    //use only for server map list
    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/overview", params = {"application", "from", "to", "applicationPairs"})
    public List<AgentStatusAndLink> getAgentsListWithVirtualNodes(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "applicationPairs", required = false) ApplicationPairs applicationPairs
    ) {
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        if (serviceType.isWas()) {
            final ApplicationAgentListQueryRule serverMapAgentListQueryRule = ApplicationAgentListQueryRule.getByValue(query, ApplicationAgentListQueryRule.ACTIVE_STATISTICS);
            return getAgentsList(
                    applicationName, serviceTypeCode, serviceTypeName, from, to, String.valueOf(serverMapAgentListQueryRule)
            );
        }
        Range range = Range.between(from, to);
        rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range);

        final Application application = applicationFactory.createApplication(Service.DEFAULT, applicationName, serviceType.getCode());

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

        return AgentsFactory.extractVirtualNode(nodeHistogramSummary, hyperLinkFactory);
    }

    private List<Application> pairsToList(List<ApplicationPair> applicationPairs) {
        if (CollectionUtils.isEmpty(applicationPairs)) {
            return Collections.emptyList();
        }
        final List<Application> applications = new ArrayList<>(applicationPairs.size());
        for (ApplicationPair applicationPair : applicationPairs) {
            final String applicationName = applicationPair.getApplicationName();
            final int serviceTypeCode = applicationPair.getServiceTypeCode();
            final Application application = this.applicationFactory.createApplication(Service.DEFAULT, applicationName, serviceTypeCode);
            applications.add(application);
        }
        return applications;
    }

    private Application createApplication(Service service, String applicationName, Short serviceTypeCode, String serviceTypeName) {
        if (StringUtils.hasLength(applicationName)) {
            if (serviceTypeCode != null) {
                return applicationFactory.createApplication(service, applicationName, serviceTypeCode);
            } else if (serviceTypeName != null) {
                return applicationFactory.createApplicationByTypeName(service, applicationName, serviceTypeName);
            }
        }
        // return application without service type
        return new Application(applicationName, ServiceType.UNDEFINED);
    }

}
