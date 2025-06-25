package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import com.navercorp.pinpoint.service.vo.ServiceInfo;
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

@RestController
@RequestMapping("/api")
public class ServiceInfoController {

    private final StaticServiceRegistry staticServiceRegistry;
    private final ServiceInfoService serviceInfoService;

    public ServiceInfoController(StaticServiceRegistry staticServiceRegistry, ServiceInfoService serviceInfoService) {
        this.staticServiceRegistry = staticServiceRegistry;
        this.serviceInfoService = serviceInfoService;
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

    @PostMapping(value = "/service")
    public ResponseEntity insertServiceGroup(@RequestParam("serviceName") @NotBlank String serviceName,
                                             @RequestBody(required = false) Map<String, String> configuration) {
        if (staticServiceRegistry.getServiceUid(serviceName) != null) {
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

    @GetMapping(value = "/service/name")
    public ResponseEntity<String> getServiceName(@RequestParam("serviceUid") int serviceUid) {
        String staticServiceName = staticServiceRegistry.getServiceName(serviceUid);
        if (staticServiceName != null) {
            return ResponseEntity.ok(staticServiceName);
        }
        String serviceName = serviceInfoService.getServiceName(serviceUid);
        if (serviceName != null) {
            return ResponseEntity.ok(serviceName);
        }

        return ResponseEntity.noContent().build();
    }

    @Deprecated
    @GetMapping(value = "/service/uid")
    public ResponseEntity<Integer> getServiceUid(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceUid serviceUid = serviceInfoService.getServiceUid(serviceName);
        if (serviceUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceUid.getUid());
    }

    @PutMapping(value = "/service")
    public ResponseEntity<String> updateServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName,
                                                    @RequestBody() Map<String, String> configuration) {
        if (staticServiceRegistry.getServiceUid(serviceName) != null) {
            return ResponseEntity.badRequest().body("Cannot update service: " + serviceName);
        }

        serviceInfoService.updateServiceConfig(serviceName, configuration);
        return ResponseEntity.ok("updated: " + serviceName + " configuration");
    }

    @PutMapping(value = "/service/name")
    public ResponseEntity<String> updateServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName,
                                                    @RequestBody(required = false) @NotBlank String newServiceName) {
        if (staticServiceRegistry.getServiceUid(serviceName) != null) {
            return ResponseEntity.badRequest().body("Cannot update service: " + serviceName);
        }

        serviceInfoService.updateServiceName(serviceName, newServiceName);
        return ResponseEntity.ok("updated: " + serviceName + " to new service name: " + newServiceName);
    }

    @DeleteMapping(value = "/service")
    public ResponseEntity<String> deleteServiceInfo(@RequestParam("serviceName") @NotBlank String serviceName) {
        if (staticServiceRegistry.getServiceUid(serviceName) != null) {
            return ResponseEntity.badRequest().body("Cannot delete service: " + serviceName);
        }

        serviceInfoService.deleteService(serviceName);
        return ResponseEntity.ok("deleted: " + serviceName);
    }
}
