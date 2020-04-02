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

import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultServerInstanceListFactory implements ServerInstanceListFactory {

    private final ServerInstanceListDataSource serverInstanceListDataSource;

    public DefaultServerInstanceListFactory(ServerInstanceListDataSource serverInstanceListDataSource) {
        this.serverInstanceListDataSource = Objects.requireNonNull(serverInstanceListDataSource, "serverInstanceListDataSource");
    }

    @Override
    public ServerInstanceList createWasNodeInstanceList(Node wasNode, long timestamp) {
        return serverInstanceListDataSource.createServerInstanceList(wasNode, timestamp);
    }

    @Override
    public ServerInstanceList createTerminalNodeInstanceList(Node terminalNode, LinkDataDuplexMap linkDataDuplexMap) {
        // extract information about the terminal node
        ServerBuilder builder = new ServerBuilder();
        for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
            Application toApplication = linkData.getToApplication();
            if (terminalNode.getApplication().equals(toApplication)) {
                builder.addCallHistogramList(linkData.getTargetList());
            }
        }
        return builder.build();
    }

    @Override
    public ServerInstanceList createQueueNodeInstanceList(Node queueNode, LinkDataDuplexMap linkDataDuplexMap) {
        return createEmptyNodeInstanceList();
    }

    @Override
    public ServerInstanceList createUserNodeInstanceList() {
        return createEmptyNodeInstanceList();
    }

    @Override
    public ServerInstanceList createEmptyNodeInstanceList() {
        return new ServerInstanceList();
    }
}
