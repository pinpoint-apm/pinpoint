package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.server.uid.ServiceInfo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.uid.service.ServiceGroupService;
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
        return serviceGroupService.selectAllServiceNames();
    }

    @PostMapping(value = "/service")
    public Response insertServiceGroup(@RequestParam("serviceName") @NotBlank String serviceName,
                                       @RequestBody(required = false) Map<String, String> tags) {
        serviceGroupService.createService(serviceName);
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/service")
    public ResponseEntity<ServiceInfo> getServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceUid serviceUid = serviceGroupService.selectServiceUid(serviceName);
        if (serviceUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new ServiceInfo(serviceUid, serviceName, null));
    }

    @GetMapping(value = "/service/name")
    public ResponseEntity<String> getServiceName(@RequestParam("serviceUid") int serviceUid) {
        String serviceName = serviceGroupService.selectServiceName(ServiceUid.of(serviceUid));
        if (serviceName == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceName);
    }

    @GetMapping(value = "/service/uid")
    public ResponseEntity<Integer> getServiceUid(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceUid serviceUid = serviceGroupService.selectServiceUid(serviceName);
        if (serviceUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceUid.getUid());
    }

    @DeleteMapping(value = "/service")
    public Response deleteServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        serviceGroupService.deleteService(serviceName);
        return SimpleResponse.ok();
    }
}
