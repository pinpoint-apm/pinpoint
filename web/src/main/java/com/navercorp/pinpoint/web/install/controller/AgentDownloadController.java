package com.navercorp.pinpoint.web.install.controller;

import com.navercorp.pinpoint.web.install.model.AgentDownloadInfo;
import com.navercorp.pinpoint.web.install.model.AgentInstallationInfo;
import com.navercorp.pinpoint.web.install.service.AgentDownLoadService;
import com.navercorp.pinpoint.web.response.CodeResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
public class AgentDownloadController {

    private final AgentDownLoadService agentDownLoadService;

    public AgentDownloadController(AgentDownLoadService agentDownLoadService) {
        this.agentDownLoadService = Objects.requireNonNull(agentDownLoadService, "agentDownLoadService");
    }

    @RequestMapping(value = "/getAgentInstallationInfo")
    public CodeResult<AgentInstallationInfo> getAgentDownloadUrl() {
        AgentDownloadInfo latestStableAgentDownloadInfo = agentDownLoadService.getLatestStableAgentDownloadInfo();
        if (latestStableAgentDownloadInfo != null) {
            return CodeResult.ok(new AgentInstallationInfo(latestStableAgentDownloadInfo));
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "can't find suitable download url");
    }

}
