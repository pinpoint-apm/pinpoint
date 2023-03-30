package com.navercorp.pinpoint.web.install.service;

import com.navercorp.pinpoint.web.install.model.AgentDownloadInfo;

public interface AgentDownLoadService {
    AgentDownloadInfo getLatestStableAgentDownloadInfo();
}
