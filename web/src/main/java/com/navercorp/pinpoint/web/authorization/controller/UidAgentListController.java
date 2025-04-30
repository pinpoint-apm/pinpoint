package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.service.AgentListService;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
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
@RequestMapping("/api/agentList")
public class UidAgentListController {

    private final AgentListService agentListService;
    private final ApplicationUidService applicationUidService;

    public UidAgentListController(AgentListService agentListService, ApplicationUidService applicationUidService) {
        this.agentListService = Objects.requireNonNull(agentListService, "agentListService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
    }

    @GetMapping(value = "")
    public List<AgentListEntry> getAgentList(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                             @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                             @RequestParam(value = "orderBy", required = false, defaultValue = "NO_OP") String orderBy) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return Collections.emptyList();
        }

        return agentListService.getApplicationAgentList(serviceUidObject, applicationUid).stream()
                .sorted(OrderBy.of(orderBy))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "", params = {"from", "to"})
    public List<AgentListEntry> getAgentList(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
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

        return agentListService.getApplicationAgentList(serviceUidObject, applicationUid, range).stream()
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
}
