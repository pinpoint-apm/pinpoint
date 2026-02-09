package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.Optional;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(value = "/api/agents")
@Validated
public class AgentListController implements AccessDeniedExceptionHandler {
    private final AgentInfoService agentInfoService;
    private final RangeValidator rangeValidator;

    public AgentListController(
            AgentInfoService agentInfoService,
            ConfigProperties configProperties) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        Objects.requireNonNull(configProperties, "configProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(configProperties.getInspectorPeriodMax()));
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_CALL_API_FOR_APP_AGENT_MANAGEMENT)")
    @GetMapping(value = "/statistics")
    public List<DetailedAgentAndStatus> getAllAgentStatistics() {
        final long timestamp = System.currentTimeMillis();
        return this.agentInfoService.getAllAgentsStatisticsList(
                        AgentStatusFilters.acceptAll(),
                        Range.between(timestamp, timestamp)
                );
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_CALL_API_FOR_APP_AGENT_MANAGEMENT)")
    @GetMapping(value = "/statistics", params = {"from", "to"})
    public List<DetailedAgentAndStatus> getAllAgentStatistics(
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range);
        return this.agentInfoService.getAllAgentsStatisticsList(
                        AgentStatusFilters.acceptAll(),
                        range
                );
    }

}
