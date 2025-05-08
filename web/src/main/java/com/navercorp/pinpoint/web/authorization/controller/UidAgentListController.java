package com.navercorp.pinpoint.web.authorization.controller;


import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.service.AgentListService;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntryAndStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/uid/agentList")
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class UidAgentListController {

    private final AgentListService agentListService;
    private final ApplicationUidService applicationUidService;

    public UidAgentListController(AgentListService agentListService, ApplicationUidService applicationUidService) {
        this.agentListService = Objects.requireNonNull(agentListService, "agentListService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
    }


    @GetMapping()
    public List<AgentListEntryAndStatus> getAgentList(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                                      @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                                      @RequestParam(value = "orderBy", required = false, defaultValue = "NO_OP") String orderBy) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return Collections.emptyList();
        }

        return agentListService.getAgentList(serviceUidObject, applicationUid).stream()
                .sorted(OrderBy.of(orderBy))
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"from", "to"})
    public List<AgentListEntryAndStatus> getAgentList(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                                      @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                                      @RequestParam("from") @PositiveOrZero long from,
                                                      @RequestParam("to") @PositiveOrZero long to,
                                                      @RequestParam(value = "orderBy", required = false, defaultValue = "NO_OP") String orderBy) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        Range range = Range.between(from, to);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return Collections.emptyList();
        }
        return agentListService.getActiveAgentList(serviceUidObject, applicationUid, range).stream()
                .sorted(OrderBy.of(orderBy))
                .collect(Collectors.toList());
    }

    @DeleteMapping()
    public Response deleteAgents(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                 @RequestParam(value = "applicationName") @NotBlank String applicationName) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return SimpleResponse.ok();
        }
        agentListService.deleteAgents(serviceUidObject, applicationUid);
        return SimpleResponse.ok();
    }

    @DeleteMapping(params = {"agentId"})
    public Response deleteAgent(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                @RequestParam(value = "agentId") @NotBlank String agentId) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return SimpleResponse.ok();
        }
        agentListService.deleteAgent(serviceUidObject, applicationUid, agentId);
        return SimpleResponse.ok();
    }

    private enum OrderBy implements Comparator<AgentListEntryAndStatus> {
        NO_OP(Comparator.comparing(o -> 0)),
        NAME_ASC(Comparator.comparing(o -> o.getAgentListEntry().getAgentName())),
        NAME_DESC(NAME_ASC.reversed()),
        ID_ASC(Comparator.comparing(o -> o.getAgentListEntry().getAgentId())),
        ID_DESC(ID_ASC.reversed()),
        START_TIME_ASC(Comparator.comparingLong(o -> o.getAgentListEntry().getStartTimestamp())),
        START_TIME_DESC(START_TIME_ASC.reversed());

        private static final Map<String, OrderBy> MAP =
                Stream.of(values()).collect(Collectors.toMap(Enum::name, e -> e));

        public static OrderBy of(String value) {
            return MAP.getOrDefault(value.toUpperCase(), NO_OP);
        }

        private final Comparator<AgentListEntryAndStatus> comparator;

        OrderBy(Comparator<AgentListEntryAndStatus> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(AgentListEntryAndStatus o1, AgentListEntryAndStatus o2) {
            return comparator.compare(o1, o2);
        }
    }
}
