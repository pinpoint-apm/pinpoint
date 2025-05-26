package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.ServiceUidCachedService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidController {

    private final ServiceUidCachedService serviceUidCachedService;
    private final ApplicationUidService applicationUidService;

    public ApplicationUidController(ServiceUidCachedService serviceUidCachedService, ApplicationUidService applicationUidService) {
        this.serviceUidCachedService = Objects.requireNonNull(serviceUidCachedService, "serviceUidCachedService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "cachedApplicationUidService");
    }

    @GetMapping(value = "/applicationNames")
    public List<String> getApplications(@RequestParam(value = "serviceName", required = false, defaultValue = ServiceUid.DEFAULT_SERVICE_UID_NAME) String serviceName) {
        ServiceUid serviceUid = serviceUidCachedService.getServiceUid(serviceName);
        return applicationUidService.getApplicationNames(serviceUid);
    }

    @GetMapping(value = "/application/uid")
    public ResponseEntity<Long> getApplicationUid(@RequestParam(value = "serviceName", required = false, defaultValue = ServiceUid.DEFAULT_SERVICE_UID_NAME) String serviceName,
                                                  @RequestParam(value = "applicationName") @NotBlank String applicationName) {
        ServiceUid serviceUid = serviceUidCachedService.getServiceUid(serviceName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName);
        if (applicationUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applicationUid.getUid());
    }

    @GetMapping(value = "/application/name")
    public ResponseEntity<String> getApplicationName(@RequestParam(value = "serviceName", required = false, defaultValue = ServiceUid.DEFAULT_SERVICE_UID_NAME) String serviceName,
                                                     @RequestParam(value = "applicationUid") long applicationUid) {
        ServiceUid serviceUidObject = serviceUidCachedService.getServiceUid(serviceName);
        ApplicationUid applicationUidObject = ApplicationUid.of(applicationUid);
        String applicationName = applicationUidService.getApplicationName(serviceUidObject, applicationUidObject);
        if (applicationName == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applicationName);
    }

    @DeleteMapping(value = "/application")
    public Response deleteApplication(@RequestParam(value = "serviceName", required = false, defaultValue = ServiceUid.DEFAULT_SERVICE_UID_NAME) String serviceName,
                                      @RequestParam(value = "applicationName") @NotBlank String applicationName) {
        ServiceUid serviceUid = serviceUidCachedService.getServiceUid(serviceName);
        applicationUidService.deleteApplication(serviceUid, applicationName);
        return SimpleResponse.ok();
    }

}
