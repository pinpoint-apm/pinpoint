package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.web.uid.service.ApplicationUidCopyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin/uid/copy")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidCopyController {

    private final ApplicationUidCopyService applicationUidCopyService;

    public ApplicationUidCopyController(ApplicationUidCopyService applicationUidCopyService) {
        this.applicationUidCopyService = Objects.requireNonNull(applicationUidCopyService, "applicationUidCopyService");
    }


    @GetMapping(value = "application")
    public ResponseEntity<String> copyApplicationList() {
        applicationUidCopyService.copyApplication();
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "agent")
    public ResponseEntity<String> copyAgentList() {
        applicationUidCopyService.copyAgentId();
        return ResponseEntity.ok("OK");
    }
}
