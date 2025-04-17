package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ServerInstanceDatasourceService {
    private final AgentInfoService agentInfoService;

    public ServerInstanceDatasourceService(AgentInfoService agentInfoService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    public ServerGroupListDataSource getServerGroupListDataSource() {
        return new AgentInfoServerGroupListDataSource(agentInfoService);
    }
}
