package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.web.service.ServiceGroupService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class ServiceGroupController {

    private final ServiceGroupService serviceGroupService;

    public ServiceGroupController(ServiceGroupService serviceGroupService) {
        this.serviceGroupService = Objects.requireNonNull(serviceGroupService, "serviceGroupService");
    }

    @GetMapping(value = "/serviceNames")
    public List<String> getAllServiceNames() {
        List<String> serviceNames = serviceGroupService.selectAllServiceNames();
        return serviceNames.stream()
                .sorted()
                .toList();
    }

    @PostMapping(value = "/service")
    public Response insertServiceGroup(@RequestParam("serviceName") @NotBlank String serviceName,
                                       @RequestBody(required = false) Map<String, String> tags) {
        serviceGroupService.createService(serviceName);
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/service")
    public ResponseEntity<ServiceInfo> getServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        UUID uuid = serviceGroupService.selectServiceUid(serviceName);
        if (uuid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new ServiceInfo(uuid, serviceName, null));
    }

    @DeleteMapping(value = "/service")
    public Response deleteServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        serviceGroupService.deleteService(serviceName);
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
}
