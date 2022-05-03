package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;

public interface AgentDownLoadService {
    AgentDownloadInfo getLatestStableAgentDownloadInfo();
}
