package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ServerInstanceDatasourceService {
    private final ServerGroupListDataSource datasource;

    public ServerInstanceDatasourceService(AgentInfoService datasource) {
        Objects.requireNonNull(datasource, "agentInfoService");
        this.datasource =  new AgentInfoServerGroupListDataSource(datasource);
    }

    public ServerGroupListDataSource getServerGroupListDataSource() {
        return datasource;
    }
}
