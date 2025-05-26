package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import com.navercorp.pinpoint.service.vo.ServiceInfo;
import com.navercorp.pinpoint.web.uid.service.ServiceUidCachedService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ServiceInfoController {

    private final StaticServiceRegistry staticServiceRegistry;
    private final ServiceInfoService serviceInfoService;
    private final ServiceUidCachedService serviceUidCachedService;

    public ServiceInfoController(StaticServiceRegistry staticServiceRegistry, ServiceInfoService serviceInfoService, ServiceUidCachedService serviceUidCachedService) {
        this.staticServiceRegistry = Objects.requireNonNull(staticServiceRegistry, "staticServiceRegistry");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceInfoService");
        this.serviceUidCachedService = Objects.requireNonNull(serviceUidCachedService, "serviceUidCacheService");
    }

    @GetMapping(value = "/staticServiceNames")
    public List<String> getStaticServiceNames() {
        return staticServiceRegistry.getServiceNames();
    }

    @GetMapping(value = "/serviceNames")
    public List<String> getAllServiceNames() {
        return serviceInfoService.getServiceNames().stream()
                .sorted()
                .toList();
    }

    @GetMapping(value = "/service/name")
    public ResponseEntity<String> getServiceName(@RequestParam("serviceUid") int serviceUid) {
        String serviceName = serviceUidCachedService.getServiceName(ServiceUid.of(serviceUid));
        if (serviceName != null) {
            return ResponseEntity.ok(serviceName);
        }

        return ResponseEntity.noContent().build();
    }

    @Deprecated
    @GetMapping(value = "/service/uid")
    public ResponseEntity<Integer> getServiceUid(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceUid serviceUid = serviceUidCachedService.getServiceUid(serviceName);
        if (serviceUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceUid.getUid());
    }

    @PostMapping(value = "/service")
    public ResponseEntity<String> insertServiceGroup(@RequestParam("serviceName") @NotBlank String serviceName,
                                                     @RequestBody(required = false) Map<String, String> configuration) {
        if (staticServiceRegistry.contains(serviceName)) {
            return ResponseEntity.badRequest().body("Cannot use reserved names: " + staticServiceRegistry.getServiceNames());
        }
        serviceInfoService.insertService(serviceName, configuration);
        return ResponseEntity.ok(serviceName);
    }

    @GetMapping(value = "/service")
    public ResponseEntity<ServiceInfo> getServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceInfo serviceInfo = serviceInfoService.getServiceInfo(serviceName);
        if (serviceInfo == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceInfo);
    }

    @PutMapping(value = "/service")
    public ResponseEntity<String> updateServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName,
                                                    @RequestBody() Map<String, String> configuration) {
        if (staticServiceRegistry.contains(serviceName)) {
            return ResponseEntity.badRequest().body("Cannot update service: " + serviceName);
        }

        serviceInfoService.updateServiceConfig(serviceName, configuration);
        return ResponseEntity.ok("updated: " + serviceName + " configuration");
    }

    @PutMapping(value = "/service/name")
    public ResponseEntity<String> updateServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName,
                                                    @RequestBody(required = false) @NotBlank String newServiceName) {
        if (staticServiceRegistry.contains(serviceName)) {
            return ResponseEntity.badRequest().body("Cannot update service: " + serviceName);
        }
        serviceInfoService.updateServiceName(serviceName, newServiceName);

        ServiceUid cachedServiceUid = serviceUidCachedService.getServiceUid(serviceName);
        serviceUidCachedService.serviceUidCacheEvict(serviceName);
        serviceUidCachedService.serviceNameCacheEvict(cachedServiceUid);
        return ResponseEntity.ok("updated: " + serviceName + " to new service name: " + newServiceName);
    }

    @DeleteMapping(value = "/service")
    public ResponseEntity<String> deleteServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        if (staticServiceRegistry.contains(serviceName)) {
            return ResponseEntity.badRequest().body("Cannot delete service: " + serviceName);
        }

        serviceInfoService.deleteService(serviceName);
        serviceUidCachedService.serviceUidCacheEvict(serviceName);
        return ResponseEntity.ok("deleted: " + serviceName);
    }
}
