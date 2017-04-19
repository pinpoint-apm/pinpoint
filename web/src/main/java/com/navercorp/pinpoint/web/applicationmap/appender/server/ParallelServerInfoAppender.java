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
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppender;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public class ParallelServerInfoAppender implements ServerInfoAppender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServerInstanceListDataSource serverInstanceListDataSource;

    private final ExecutorService executorService;

    public ParallelServerInfoAppender(ServerInstanceListDataSource serverInstanceListDataSource, ExecutorService executorService) {
        if (serverInstanceListDataSource == null) {
            throw new NullPointerException("serverInstanceListDataSource must not be null");
        }
        if (executorService == null) {
            throw new NullPointerException("executorService must not be null");
        }
        this.serverInstanceListDataSource = serverInstanceListDataSource;
        this.executorService = executorService;
    }

    @Override
    public void appendServerInfo(Range range, NodeList source, LinkDataDuplexMap linkDataDuplexMap) {
        if (source == null) {
            return;
        }
        Collection<Node> nodes = source.getNodeList();
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        CompletableFuture[] futures = getServerInstanceListFutures(range, nodes, linkDataDuplexMap);
        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            logger.error("Error appending node histograms", e);
        }
    }

    private CompletableFuture[] getServerInstanceListFutures(Range range, Collection<Node> nodes, LinkDataDuplexMap linkDataDuplexMap) {
        List<CompletableFuture<Void>> serverInstanceListFutures = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getServiceType().isUnknown()) {
                // we do not know the server info for unknown nodes
                continue;
            }
            CompletableFuture<Void> serverInstanceListFuture = getServerInstanceListFuture(range, node, linkDataDuplexMap);
            serverInstanceListFutures.add(serverInstanceListFuture);
        }
        return serverInstanceListFutures.toArray(new CompletableFuture[serverInstanceListFutures.size()]);
    }

    private CompletableFuture<Void> getServerInstanceListFuture(Range range, Node node, LinkDataDuplexMap linkDataDuplexMap) {
        CompletableFuture<ServerInstanceList> serverInstanceListFuture;
        ServiceType nodeServiceType = node.getServiceType();
        if (nodeServiceType.isWas()) {
            final long to = range.getTo();
            serverInstanceListFuture = CompletableFuture.supplyAsync(() -> serverInstanceListDataSource.createServerInstanceList(node, to), executorService);
        } else if (nodeServiceType.isTerminal() || nodeServiceType.isQueue()) {
            // extract information about the terminal node
            serverInstanceListFuture = CompletableFuture.completedFuture(createTerminalNodeInstanceList(node, linkDataDuplexMap));
        } else {
            serverInstanceListFuture = CompletableFuture.completedFuture(new ServerInstanceList());
        }
        return serverInstanceListFuture.thenAccept(node::setServerInstanceList);
    }

    private ServerInstanceList createTerminalNodeInstanceList(Node node, LinkDataDuplexMap linkDataDuplexMap) {
        ServerBuilder builder = new ServerBuilder();
        for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
            Application toApplication = linkData.getToApplication();
            if (node.getApplication().equals(toApplication)) {
                builder.addCallHistogramList(linkData.getTargetList());
            }
        }
        return builder.build();
    }
}
