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
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.NodeList;
import com.navercorp.pinpoint.web.applicationmap.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class SerialServerInfoAppender implements ServerInfoAppender {

    private final ServerInstanceListDataSource serverInstanceListDataSource;

    public SerialServerInfoAppender(ServerInstanceListDataSource serverInstanceListDataSource) {
        if (serverInstanceListDataSource == null) {
            throw new NullPointerException("serverInstanceListDataSource must not be null");
        }
        this.serverInstanceListDataSource = serverInstanceListDataSource;
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
           if (nodeServiceType.isWas()) {
               ServerInstanceList serverInstanceList = serverInstanceListDataSource.createServerInstanceList(node, range.getTo());
               node.setServerInstanceList(serverInstanceList);
           } else if (nodeServiceType.isTerminal() || nodeServiceType.isQueue()) {
               // extract information about the terminal node
               ServerBuilder builder = new ServerBuilder();
               for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
                   Application toApplication = linkData.getToApplication();
                   if (node.getApplication().equals(toApplication)) {
                       builder.addCallHistogramList(linkData.getTargetList());
                   }
               }
               ServerInstanceList serverInstanceList = builder.build();
               node.setServerInstanceList(serverInstanceList);
           } else {
               // add empty information
               node.setServerInstanceList(new ServerInstanceList());
           }
       }
    }
}
