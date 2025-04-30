package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.service.AgentListService;
import com.navercorp.pinpoint.web.uid.service.AgentNameService;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/admin")
public class UidAdminController {
    private final ApplicationUidService applicationUidService;
    private final AgentNameService agentNameService;

    private final AgentListService agentListService;

    public UidAdminController(ApplicationUidService applicationUidService, AgentNameService agentNameService, AgentListService agentListService) {
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
        this.agentNameService = Objects.requireNonNull(agentNameService, "agentNameService");
        this.agentListService = Objects.requireNonNull(agentListService, "agentListService");
    }

    // debug
    @GetMapping(value = "/debug/agent", params = "agentId")
    public List<AgentIdentifier> getAgent(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                          @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                          @RequestParam(value = "agentId") @NotBlank String agentId) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        return agentNameService.getAgentIdentifier(serviceUidObject, applicationUid, agentId);
    }

    @GetMapping(value = "/debug/agent")
    public List<AgentIdentifier> getAllAgent(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                             @RequestParam(value = "applicationName") @NotBlank String applicationName) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        return agentNameService.getAgentIdentifier(serviceUidObject, applicationUid);
    }

    @DeleteMapping("/agent")
    public Response deleteAgent(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                @RequestParam(value = "agentId") @NotBlank String agentId) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return SimpleResponse.ok();
        }
        agentNameService.deleteAgent(serviceUidObject, applicationUid, agentId);
        return SimpleResponse.ok();
    }

    @DeleteMapping(value = "/application")
    public Response deleteApplication(@RequestParam(value = "serviceUid") int serviceUid,
                                      @RequestParam(value = "applicationName") String applicationName) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);

        agentNameService.deleteAllAgents(serviceUidObject, applicationUid);
        applicationUidService.deleteApplication(serviceUidObject, applicationName);
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/cleanup/inactiveAgent")
    public Response cleanupInactiveAgents(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                          @RequestParam(value = "applicationName") String applicationName,
                                          @RequestParam(value = "durationDays", defaultValue = "30") @Min(30) int durationDays) {
        long to = System.currentTimeMillis();
        long from = to - TimeUnit.DAYS.toMillis(durationDays);
        Range range = Range.between(from, to);
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        int agentCleanupCount = agentListService.cleanupInactiveAgent(serviceUidObject, applicationUid, range);
        return SimpleResponse.ok("agentCleanupCount: " + agentCleanupCount);
    }

    @GetMapping(value = "/cleanup/inactiveApplication")
    public Response cleanupInActiveApplication(@RequestParam(value = "serviceUid", required = false, defaultValue = "0") int serviceUid,
                                               @RequestParam(value = "durationDays", defaultValue = "30") @Min(30) int durationDays) {
        long to = System.currentTimeMillis();
        long from = to - TimeUnit.DAYS.toMillis(durationDays);
        Range range = Range.between(from, to);
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);

        List<String> applicationNameList = applicationUidService.getApplicationNames(serviceUidObject);
        for (String applicationName : applicationNameList) {
            ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
            agentListService.cleanupInactiveAgent(serviceUidObject, applicationUid, range);
        }
        int applicationCleanupCount = applicationUidService.cleanupEmptyApplication(serviceUidObject, from);
        return SimpleResponse.ok("applicationCleanupCount: " + applicationCleanupCount);
    }

    // Cleanup inconsistent application UIDs caused by rollback failure
    @GetMapping(value = "/cleanup/inconsistentApplicationUid")
    public Response cleanupInconsistentApplicationUid(@RequestParam(value = "serviceUid") Optional<Integer> serviceUid) {
        ServiceUid serviceUidObject = serviceUid.map(ServiceUid::of).orElse(null);
        int cleanupCount = applicationUidService.cleanupInconsistentApplicationName(serviceUidObject);
        return SimpleResponse.ok("cleanupCount: " + cleanupCount);
    }

}
