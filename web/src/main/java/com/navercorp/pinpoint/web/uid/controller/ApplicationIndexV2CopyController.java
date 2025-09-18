package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.web.uid.service.ApplicationIndexV2CopyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin/copy")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationIndexV2CopyController {

    private final ApplicationIndexV2CopyService applicationIndexV2CopyService;

    public ApplicationIndexV2CopyController(ApplicationIndexV2CopyService applicationIndexV2CopyService) {
        this.applicationIndexV2CopyService = Objects.requireNonNull(applicationIndexV2CopyService, "applicationUidCopyService");
    }


    @GetMapping(value = "/v2/application")
    public ResponseEntity<String> copyApplicationList() {
        applicationIndexV2CopyService.copyApplication();
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "/v2/agent")
    public ResponseEntity<String> copyAgentList() {
        applicationIndexV2CopyService.copyAgentId();
        return ResponseEntity.ok("OK");
    }
}
