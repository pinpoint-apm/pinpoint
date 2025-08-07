package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.uid.service.AgentIdService;
import com.navercorp.pinpoint.uid.service.ApplicationCleanupService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.service.AgentListService;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.ServiceUidCachedService;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/admin")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class UidAdminController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceUidCachedService serviceUidCachedService;
    private final ApplicationUidService applicationUidService;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final ApplicationCleanupService applicationUidCleanupService;

    private final AgentIdService agentIdService;
    private final AgentListService agentListService;

    public UidAdminController(ServiceUidCachedService serviceUidCachedService,
                              ApplicationUidService applicationUidService,
                              ServiceTypeRegistryService serviceTypeRegistryService,
                              ApplicationCleanupService applicationCleanupService,
                              AgentIdService agentIdService,
                              AgentListService agentListService) {
        this.serviceUidCachedService = Objects.requireNonNull(serviceUidCachedService, "serviceUidCachedService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.applicationUidCleanupService = Objects.requireNonNull(applicationCleanupService, "applicationCleanupService");
        this.agentIdService = Objects.requireNonNull(agentIdService, "agentNameService");
        this.agentListService = Objects.requireNonNull(agentListService, "agentListService");
    }

    @GetMapping(value = "/debug/agent")
    public List<String> getAllAgent(@RequestParam(value = "serviceName", required = false) String serviceName,
                                    @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                    @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                    @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceType.getCode());
        return agentIdService.getAgentId(serviceUid, applicationUid);
    }

    @DeleteMapping("/agent")
    public Response deleteAgent(@RequestParam(value = "serviceName", required = false) String serviceName,
                                @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
                                @RequestParam(value = "agentId") @NotBlank String agentId) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);

        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceType.getCode());
        if (applicationUid == null) {
            return SimpleResponse.ok();
        }
        agentIdService.deleteAgent(serviceUid, applicationUid, agentId);
        return SimpleResponse.ok();
    }

    @DeleteMapping(value = "/application")
    public Response deleteApplication(@RequestParam(value = "serviceName", required = false) String serviceName,
                                      @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                      @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                      @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceType.getCode());

        agentIdService.deleteAllAgent(serviceUid, applicationUid);
        applicationUidService.deleteApplication(serviceUid, applicationName, serviceType.getCode());
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/cleanup/inactiveAgent")
    public Response cleanupInactiveAgents(@RequestParam(value = "serviceName", required = false) String serviceName,
                                          @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                          @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                          @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
                                          @RequestParam(value = "durationDays", defaultValue = "30") @Min(30) int durationDays) {
        long to = System.currentTimeMillis();
        long from = to - TimeUnit.DAYS.toMillis(durationDays);
        Range range = Range.between(from, to);
        ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);

        int agentCleanupCount = agentListService.cleanupInactiveAgent(serviceName, applicationName, serviceType.getCode(), range);
        return SimpleResponse.ok("agentCleanupCount: " + agentCleanupCount);
    }

    @GetMapping(value = "/cleanup/inactiveApplication")
    public Response cleanupInActiveApplication(@RequestParam(value = "serviceName", required = false) String serviceName,
                                               @RequestParam(value = "durationDays", defaultValue = "30") @Min(30) int durationDays) {
        long to = System.currentTimeMillis();
        long from = to - TimeUnit.DAYS.toMillis(durationDays);
        Range range = Range.between(from, to);
        ServiceUid serviceUid = getServiceUid(serviceName);

        int cleanupCount = 0;
        List<ApplicationUidRow> applicationUidRowList = applicationUidService.getApplications(serviceUid);
        for (ApplicationUidRow row : applicationUidRowList) {
            List<AgentAndStatus> agentList = agentListService.getApplicationAgentList(serviceName, row.applicationName(), row.serviceTypeCode(), range);
            if (agentList.isEmpty()) {
                deleteApplication(row);
                cleanupCount++;
            }
        }
        return SimpleResponse.ok("applicationCleanupCount: " + cleanupCount);
    }

    private void deleteApplication(ApplicationUidRow row) {
        agentIdService.deleteAllAgent(row.serviceUid(), row.applicationUid());
        applicationUidService.deleteApplication(row.serviceUid(), row.applicationName(), row.serviceTypeCode());
        logger.info("Cleanup inactive application: {}", row);
    }

    // Cleanup inconsistent application UIDs caused by rollback failure
    @GetMapping(value = "/cleanup/inconsistentApplicationUid")
    public Response cleanupInconsistentApplicationUid(@RequestParam(value = "serviceName", required = false) String serviceName) {
        ServiceUid serviceUid;
        if (StringUtils.isEmpty(serviceName)) {
            serviceUid = null;
        } else {
            serviceUid = serviceUidCachedService.getServiceUid(serviceName);
        }

        int cleanupCount = applicationUidCleanupService.cleanupInconsistentApplicationUid(serviceUid);
        return SimpleResponse.ok("cleanupCount: " + cleanupCount);
    }

    private ServiceUid getServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        return serviceUidCachedService.getServiceUid(serviceName);
    }

    private ServiceType findServiceType(Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            return serviceTypeRegistryService.findServiceType(serviceTypeCode);
        } else if (serviceTypeName != null) {
            return serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
        }
        throw new IllegalArgumentException("Either serviceTypeCode or serviceTypeName must be provided.");
    }
}
