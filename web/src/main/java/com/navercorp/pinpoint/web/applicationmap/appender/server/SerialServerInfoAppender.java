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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class SerialServerInfoAppender implements ServerInfoAppender {

    private final ServerInstanceListFactory serverInstanceListFactory;

    public SerialServerInfoAppender(ServerInstanceListFactory serverInstanceListFactory) {
        if (serverInstanceListFactory == null) {
            throw new NullPointerException("serverInstanceListFactory must not be null");
        }
        this.serverInstanceListFactory = serverInstanceListFactory;
    }

    @Override
    public void appendServerInfo(Range range, NodeList source, LinkDataDuplexMap linkDataDuplexMap) {
        if (source == null) {
            return;
        }
       for (Node node : source.getNodeList()) {
           ServiceType nodeServiceType = node.getServiceType();
           if (nodeServiceType.isUnknown()) {
               continue;
           }
           ServerInstanceList serverInstanceList;
           if (nodeServiceType.isWas()) {
               serverInstanceList = serverInstanceListFactory.createWasNodeInstanceList(node, range.getTo());
           } else if (nodeServiceType.isTerminal()) {
               serverInstanceList = serverInstanceListFactory.createTerminalNodeInstanceList(node, linkDataDuplexMap);
           } else if (nodeServiceType.isQueue()) {
               serverInstanceList = serverInstanceListFactory.createQueueNodeInstanceList(node, linkDataDuplexMap);
           } else if (nodeServiceType.isUser()) {
               serverInstanceList = serverInstanceListFactory.createUserNodeInstanceList();
           } else {
               serverInstanceList = serverInstanceListFactory.createEmptyNodeInstanceList();
           }
           node.setServerInstanceList(serverInstanceList);
       }
    }
}
