package com.navercorp.pinpoint.metric.web.controller;

import com.navercorp.pinpoint.metric.web.service.SystemMetricHostExclusionService;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostGroupInfo;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostInfo;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(value = "/exclusion/systemMetric")
public class SystemMetricExclusionController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SystemMetricHostExclusionService systemMetricHostExclusionService;
    private final TenantProvider tenantProvider;

    public SystemMetricExclusionController(SystemMetricHostExclusionService systemMetricHostExclusionService, TenantProvider tenantProvider) {
        this.systemMetricHostExclusionService = systemMetricHostExclusionService;
        this.tenantProvider = tenantProvider;
    }

    @GetMapping(value = "/hostGroup")
    public List<String> getHostGroupNameList() {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostExclusionService.getHostGroupNameList(tenantId);
    }

    @GetMapping(value = "/hostGroup", params = {"hostGroupName"})
    public SystemMetricHostGroupInfo getHostGroupExclusionInfo(@RequestParam("hostGroupName") String hostGroupName) {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostExclusionService.getHostGroupInfo(tenantId, hostGroupName);
    }

    @GetMapping(value = "/hostGroup/host")
    public List<SystemMetricHostInfo> getHostExclusionInfoList(@RequestParam("hostGroupName") String hostGroupName,
                                                               @RequestParam(value = "orderBy", defaultValue = "hostName") String orderBy) {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostExclusionService.getHostInfoList(tenantId, hostGroupName, orderBy);
    }

    @PostMapping(value = "/hostGroup")
    public String insertHostGroupExclusion(@RequestParam("hostGroupName") String hostGroupName) {
        logger.debug("add hostGroup exclusion - hostGroupName: [{}]", hostGroupName);
        try {
            systemMetricHostExclusionService.insertHostGroupExclusion(hostGroupName);
            return "OK";
        } catch (Exception e) {
            logger.error("error while adding hostGroup exclusion", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping(value = "/hostGroup")
    public String deleteHostGroupExclusion(@RequestParam("hostGroupName") String hostGroupName) {
        logger.debug("delete host group exclusion - hostGroupName: [{}]", hostGroupName);
        try {
            systemMetricHostExclusionService.deleteHostGroupExclusion(hostGroupName);
            return "OK";
        } catch (Exception e) {
            logger.error("error while deleting hostGroup exclusion", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping(value = "/hostGroup/host")
    public String insertHostExclusion(@RequestParam("hostGroupName") String hostGroupName,
                                      @RequestParam("hostName") String hostName) {
        logger.debug("add host exclusion - hostGroupName: [{}], hostName: [{}]", hostGroupName, hostName);
        try {
            systemMetricHostExclusionService.insertHostExclusion(hostGroupName, hostName);
            return "OK";
        } catch (Exception e) {
            logger.error("error while adding host exclusion", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping(value = "/hostGroup/host")
    public String deleteHostExclusion(@RequestParam("hostGroupName") String hostGroupName,
                                      @RequestParam("hostName") String hostName) {
        logger.debug("delete host exclusion - hostGroupName: [{}], hostName: [{}]", hostGroupName, hostName);
        try {
            systemMetricHostExclusionService.deleteHostExclusion(hostGroupName, hostName);
            return "OK";
        } catch (Exception e) {
            logger.error("error while deleting host exclusion", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @DeleteMapping(value = "/unusedGroups")
    public String deleteUnusedGroupExclusions() {
        logger.debug("delete exclusions from unused groups");
        String tenantId = tenantProvider.getTenantId();
        try {
            systemMetricHostExclusionService.deleteUnusedGroupExclusions(tenantId);
            return "OK";
        } catch (Exception e) {
            logger.error("error while deleting exclusions from unused groups", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
