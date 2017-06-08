/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.appender.server;

import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author HyunGil Jeong
 */
@Component
public class ServerInfoAppenderFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mode;

    @Autowired
    public ServerInfoAppenderFactory(@Value("#{pinpointWebProps['web.servermap.appender.mode'] ?: 'serial'}") String mode) {
        this.mode = mode;
    }

    public ServerInfoAppender createAppender(Set<AgentInfo> agentInfos) {
        ServerInstanceListDataSource serverInstanceListDataSource = new ServerInstanceListDataSource() {
            @Override
            public ServerInstanceList createServerInstanceList(Node node, long timestamp) {
                ServerBuilder serverBuilder = new ServerBuilder();
                serverBuilder.addAgentInfo(agentInfos);
                ServerInstanceList serverInstanceList = serverBuilder.build();
                return serverInstanceList;
            }
        };
        return from(serverInstanceListDataSource);
    }

    public ServerInfoAppender createAppender(AgentInfoService agentInfoService) {
        ServerInstanceListDataSource serverInstanceListDataSource = new ServerInstanceListAgentInfoServiceDataSource(agentInfoService);
        return from(serverInstanceListDataSource);
    }

    private ServerInfoAppender from(ServerInstanceListDataSource serverInstanceListDataSource) {
        logger.debug("ServerInfoAppender mode : {}", mode);
        return new SerialServerInfoAppender(serverInstanceListDataSource);
    }
}
