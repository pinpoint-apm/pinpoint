package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ServerInstanceDatasourceService {
    private final AgentCollectionService agentCollectionService;
    private final HyperLinkFactory hyperLinkFactory;

    public ServerInstanceDatasourceService(AgentCollectionService agentCollectionService, HyperLinkFactory hyperLinkFactory) {
        this.agentCollectionService = Objects.requireNonNull(agentCollectionService, "agentCollectionService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    public ServerGroupListDataSource getServerGroupListDataSource() {
        return new AgentInfoServerGroupListDataSource(agentCollectionService, hyperLinkFactory);
    }
}
