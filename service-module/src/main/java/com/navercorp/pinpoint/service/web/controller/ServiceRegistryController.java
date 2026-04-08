package com.navercorp.pinpoint.service.web.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.service.component.ReservedServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.service.web.controller.vo.ServiceNameRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v2/services")
public class ServiceRegistryController {

    private final ServiceRegistryService serviceRegistryService;
    private final ReservedServiceRegistry reservedServiceRegistry;

    public ServiceRegistryController(ServiceRegistryService serviceRegistryService,
                                     ReservedServiceRegistry reservedServiceRegistry) {
        this.serviceRegistryService = Objects.requireNonNull(serviceRegistryService, "serviceRegistryService");
        this.reservedServiceRegistry = Objects.requireNonNull(reservedServiceRegistry, "reservedServiceRegistry");
    }

    @PostMapping
    public Response insertService(@RequestBody @Valid ServiceNameRequest serviceNameRequest) {
        if (reservedServiceRegistry.contains(serviceNameRequest.getServiceName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot use reserved service name: " + serviceNameRequest.getServiceName());
        }
        // TODO: (minwoo) 실제 db에 중복값이 있는지 체크도 필요함.
        serviceRegistryService.insertService(serviceNameRequest.getServiceName());

        return SimpleResponse.ok();
    }

    @GetMapping
    public List<String> getServiceNames() {
        return serviceRegistryService.getServiceNames();
    }

    // TODO: (minwoo) 이게 진짜 필요한지 추후 검토 필요함.
    // TODO: (minwoo) serviceEntity는 dao 개념이 더 맞아서 controller에서 return 타입으로 써도될지 고민필요하고 애초에 이런 메소드가 필요없다면 고민 필요없음.
    @GetMapping("/service")
    public ResponseEntity<ServiceEntity> getService(@RequestParam("serviceName") @NotBlank String serviceName) {
        ServiceEntity service = serviceRegistryService.getService(serviceName);
        if (service == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(service);
    }

    @DeleteMapping("/service")
    public Response deleteService(@RequestParam("serviceName") @NotBlank String serviceName) {
        if (reservedServiceRegistry.contains(serviceName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete reserved service: " + serviceName);
        }
        serviceRegistryService.deleteService(serviceName);
        return SimpleResponse.ok();
    }
}
