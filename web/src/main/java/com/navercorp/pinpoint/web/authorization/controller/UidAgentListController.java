package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.service.AgentListService;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/agentList")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class UidAgentListController {

    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AgentListService agentListService;

    public UidAgentListController(ServiceTypeRegistryService serviceTypeRegistryService, AgentListService agentListService) {
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentListService = Objects.requireNonNull(agentListService, "agentListService");

    }

    @GetMapping(value = "")
    public List<AgentListEntry> getAgentList(@RequestParam(value = "serviceName", required = false) String serviceName,
                                             @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                             @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                             @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
                                             @RequestParam(value = "orderBy", required = false, defaultValue = "NO_OP") String orderBy) {
        ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);

        List<AgentListEntry> applicationAgentList;
        if (serviceType.equals(ServiceType.UNDEFINED)) {
            applicationAgentList = agentListService.getApplicationAgentList(serviceName, applicationName);
        } else {
            applicationAgentList = agentListService.getApplicationAgentList(serviceName, applicationName, serviceType.getCode());
        }
        return applicationAgentList.stream()
                .sorted(OrderBy.of(orderBy))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "", params = {"from", "to"})
    public List<AgentListEntry> getAgentList(@RequestParam(value = "serviceName", required = false) String serviceName,
                                             @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                             @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                             @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
                                             @RequestParam("from") @PositiveOrZero long from,
                                             @RequestParam("to") @PositiveOrZero long to,
                                             @RequestParam(value = "orderBy", required = false, defaultValue = "NO_OP") String orderBy) {
        ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);

        List<AgentListEntry> applicationAgentList;
        if (serviceType.equals(ServiceType.UNDEFINED)) {
            applicationAgentList = agentListService.getApplicationAgentList(serviceName, applicationName, range);
        } else {
            applicationAgentList = agentListService.getApplicationAgentList(serviceName, applicationName, serviceType.getCode(), range);
        }
        return applicationAgentList.stream()
                .sorted(OrderBy.of(orderBy))
                .collect(Collectors.toList());
    }

    private enum OrderBy implements Comparator<AgentListEntry> {
        NO_OP(Comparator.comparing(o -> 0)),
        NAME_ASC(Comparator.comparing(o -> o.getName())),
        NAME_DESC(NAME_ASC.reversed()),
        ID_ASC(Comparator.comparing(o -> o.getId())),
        ID_DESC(ID_ASC.reversed()),
        START_TIME_ASC(Comparator.comparingLong(o -> o.getStartTimestamp())),
        START_TIME_DESC(START_TIME_ASC.reversed());

        private static final Map<String, OrderBy> MAP =
                Stream.of(values()).collect(Collectors.toMap(Enum::name, e -> e));

        public static OrderBy of(String value) {
            return MAP.getOrDefault(value.toUpperCase(), NO_OP);
        }

        private final Comparator<AgentListEntry> comparator;

        OrderBy(Comparator<AgentListEntry> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(AgentListEntry o1, AgentListEntry o2) {
            return comparator.compare(o1, o2);
        }
    }

    private ServiceType findServiceType(Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            return serviceTypeRegistryService.findServiceType(serviceTypeCode);
        } else if (serviceTypeName != null) {
            return serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
        }
        return ServiceType.UNDEFINED;
    }
}
