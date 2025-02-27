package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ApplicationIdentifier;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import com.navercorp.pinpoint.web.service.ApplicationUidService;
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

import static com.navercorp.pinpoint.common.server.vo.ServiceUid.DEFAULT_SERVICE_UID;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class ApplicationUidController {

    private final String DEFAULT_SERVICE_UID_CODE = "0";
    private final ApplicationUidService applicationUidService;

    public ApplicationUidController(ApplicationUidService applicationUidService) {
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationInfoService");
    }

    @GetMapping(value = "/applicationNames")
    public List<String> getApplications(@RequestParam(value = "serviceUid", required = false, defaultValue = DEFAULT_SERVICE_UID_CODE) int serviceUid) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        return applicationUidService.getApplicationNames(serviceUidObject);
    }

    @GetMapping(value = "/application/uids")
    public List<ApplicationIdentifier> getApplicationIds(@RequestParam(value = "applicationName") @NotBlank String applicationName) {
        return applicationUidService.getApplicationIds(applicationName);
    }

    @GetMapping(value = "/application/uid")
    public ResponseEntity<Long> getApplicationUid(@RequestParam(value = "serviceUid", required = false, defaultValue = DEFAULT_SERVICE_UID_CODE) int serviceUid,
                                           @RequestParam(value = "applicationName") @NotBlank String applicationName) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUidObject, applicationName);
        if (applicationUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applicationUid.getUid());
    }

    @GetMapping(value = "/application/name")
    public ResponseEntity<String> getApplicationName(@RequestParam(value = "serviceUid", required = false, defaultValue = DEFAULT_SERVICE_UID_CODE) int serviceUid,
                                                     @RequestParam(value = "applicationId") long applicationId) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        ApplicationUid applicationUidObject = ApplicationUid.of(applicationId);
        String applicationName = applicationUidService.getApplicationName(serviceUidObject, applicationUidObject);
        if (applicationName == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applicationName);
    }

    @DeleteMapping(value = "/application")
    public Response deleteApplication(@RequestParam(value = "serviceUid", required = false, defaultValue = DEFAULT_SERVICE_UID_CODE) int serviceUid,
                                      @RequestParam(value = "applicationName") @NotBlank String applicationName) {
        ServiceUid serviceUidObject = ServiceUid.of(serviceUid);
        applicationUidService.deleteApplication(serviceUidObject, applicationName);
        return SimpleResponse.ok();
    }
}
