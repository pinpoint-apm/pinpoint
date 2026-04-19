package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.web.uid.service.ApplicationIndexV2CopyService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin/copy")
@ConditionalOnProperty(name = "pinpoint.web.application.index.v2.enabled", havingValue = "true")
@Validated
public class ApplicationIndexV2CopyController {

    private final ApplicationIndexV2CopyService applicationIndexV2CopyService;

    public ApplicationIndexV2CopyController(ApplicationIndexV2CopyService applicationIndexV2CopyService) {
        this.applicationIndexV2CopyService = Objects.requireNonNull(applicationIndexV2CopyService, "applicationUidCopyService");
    }

    @PostMapping(value = "/applications")
    public ResponseEntity<String> copyApplicationList() {
        applicationIndexV2CopyService.copyApplication();
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value = "/agents/all")
    public ResponseEntity<String> copyAllAgents(
            @RequestParam(value = "maxIterations", required = false, defaultValue = "200000") int maxIterations,
            @RequestParam(value = "batchSize", required = false, defaultValue = "1000") int batchSize
    ) {
        applicationIndexV2CopyService.copyAgentId(0, maxIterations, batchSize);
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value = "/agents")
    public ResponseEntity<String> copyRecentAgents(
            @RequestParam(value = "durationDays", required = false, defaultValue = "30") int durationDays,
            @RequestParam(value = "maxIterations", required = false, defaultValue = "200000") int maxIterations,
            @RequestParam(value = "batchSize", required = false, defaultValue = "1000") int batchSize
    ) {
        applicationIndexV2CopyService.copyAgentId(durationDays, maxIterations, batchSize);
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value = "/agents", params = {"applicationName"})
    public ResponseEntity<String> copyAgentsByApplicationName(
            @RequestParam(value = "applicationName") @NotBlank String applicationName
    ) {
        applicationIndexV2CopyService.copyAgentId(applicationName);
        return ResponseEntity.ok("OK");
    }

    // for OTLP(1220), node(1400), python(1700), go(1800), envoy(1550)
    @PostMapping(value = "/agents", params = {"serviceTypeCode"})
    public ResponseEntity<String> copyAgentsByServiceTypeCode(
            @RequestParam(value = "serviceTypeCode") int serviceTypeCode
    ) {
        applicationIndexV2CopyService.copyAgentId(serviceTypeCode);
        return ResponseEntity.ok("OK");
    }
}
