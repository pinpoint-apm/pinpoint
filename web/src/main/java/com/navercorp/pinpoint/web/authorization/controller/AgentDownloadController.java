package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentDownLoadService;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import com.navercorp.pinpoint.web.vo.AgentInstallationInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class AgentDownloadController {

    private final AgentDownLoadService agentDownLoadService;

    public AgentDownloadController(AgentDownLoadService agentDownLoadService) {
        this.agentDownLoadService = Objects.requireNonNull(agentDownLoadService, "agentDownLoadService");
    }

    @RequestMapping(value = "/getAgentInstallationInfo")
    public ResponseEntity<CodeResult> getAgentDownloadUrl() {
        AgentDownloadInfo latestStableAgentDownloadInfo = agentDownLoadService.getLatestStableAgentDownloadInfo();
        if (latestStableAgentDownloadInfo != null) {
            return CodeResult.ok(new AgentInstallationInfo(latestStableAgentDownloadInfo));
        }

        return CodeResult.serverError("can't find suitable download url");
    }

}
