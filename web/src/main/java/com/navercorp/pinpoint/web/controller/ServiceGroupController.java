package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.config.UserConfigProperties;
import com.navercorp.pinpoint.web.service.ServiceGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class ServiceGroupController {

    private final ServiceGroupService serviceGroupService;

    private final UserConfigProperties userConfigProperties;
    private final UserService userService;

    public ServiceGroupController(ServiceGroupService serviceGroupService, UserConfigProperties userConfigProperties, UserService userService) {
        this.serviceGroupService = Objects.requireNonNull(serviceGroupService, "serviceGroupService");
        this.userConfigProperties = Objects.requireNonNull(userConfigProperties, "userConfigProperties");
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    @GetMapping(value = "/serviceNames")
    public List<String> getAllServiceNames() {
        List<String> serviceNames = serviceGroupService.selectAllServiceNames();
        return serviceNames;
    }

    @PostMapping(value = "/service")
    public Response insertServiceGroup(@RequestParam("serviceName") @NotBlank String serviceName,
                                       @RequestBody(required = false) Map<String, String> tags) {
        if (tags == null) {
            tags = Collections.emptyMap();
        }
        Map<String, String> newTags = addUserId(tags);
        serviceGroupService.createServiceGroup(serviceName, newTags);
        return SimpleResponse.ok();
    }

    private Map<String, String> addUserId(Map<String, String> tags) {
        if (userConfigProperties.isOpenSource()) {
            return tags;
        } else {
            String userId = userService.getUserIdFromSecurity();
            if (StringUtils.isEmpty(userId)) {
                throw new IllegalStateException("no user id found");
            }
            Map<String, String> treeMap = new TreeMap<>(tags);
            treeMap.putIfAbsent("Pinpoint-createdBy", userId);
            return treeMap;
        }
    }

    @GetMapping(value = "/service")
    public ResponseEntity<ServiceInfo> getServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        UUID uuid = serviceGroupService.selectServiceUid(serviceName);
        if (uuid == null) {
            return ResponseEntity.noContent().build();
        }
        Map<String, String> tags = serviceGroupService.selectServiceTags(serviceName);
        return ResponseEntity.ok(new ServiceInfo(uuid, serviceName, tags));
    }

    @DeleteMapping(value = "/service")
    public Response deleteServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        serviceGroupService.deleteServiceGroup(serviceName);
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/service/name")
    public ResponseEntity<String> getServiceName(@RequestParam("serviceUid") @NotBlank String serviceUidString) {
        UUID serviceUid = UUID.fromString(serviceUidString);
        String serviceName = serviceGroupService.selectServiceName(serviceUid);
        if (serviceName == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceName);
    }

    @GetMapping(value = "/service/uid")
    public ResponseEntity<UUID> getServiceUid(@RequestParam("serviceName") @NotBlank String serviceName) {
        UUID uuid = serviceGroupService.selectServiceUid(serviceName);
        if (uuid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(uuid);
    }

    @GetMapping(value = "/service/tag")
    public ResponseEntity<Map<String, String>> getServiceTag(@RequestParam("serviceName") @NotBlank String serviceName) {
        Map<String, String> serviceInfo = serviceGroupService.selectServiceTags(serviceName);
        if (serviceInfo == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceInfo);
    }

    @PostMapping(value = "/service/tag")
    public Response insertServiceTag(@RequestParam("serviceName") @NotBlank String serviceName,
                                     @RequestParam("key") @NotBlank String key,
                                     @RequestParam("value") @NotNull String value) {
        serviceGroupService.insertServiceTag(serviceName, key, value);
        return SimpleResponse.ok();
    }

    @DeleteMapping(value = "/service/tag")
    public Response deleteServiceTag(@RequestParam("serviceName") @NotBlank String serviceName,
                                     @RequestParam("key") @NotBlank String key) {
        serviceGroupService.deleteServiceTag(serviceName, key);
        return SimpleResponse.ok();
    }
}
