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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
public class ParallelServerInfoAppender implements ServerInfoAppender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServerInstanceListFactory serverInstanceListFactory;

    private final ExecutorService executorService;

    public ParallelServerInfoAppender(ServerInstanceListFactory serverInstanceListFactory, ExecutorService executorService) {
        if (serverInstanceListFactory == null) {
            throw new NullPointerException("serverInstanceListFactory must not be null");
        }
        if (executorService == null) {
            throw new NullPointerException("executorService must not be null");
        }
        this.serverInstanceListFactory = serverInstanceListFactory;
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
            serverInstanceListFuture = CompletableFuture.supplyAsync(new Supplier<ServerInstanceList>() {
                @Override
                public ServerInstanceList get() {
                    return serverInstanceListFactory.createWasNodeInstanceList(node, to);
                }
            }, executorService);
        } else if (nodeServiceType.isTerminal()) {
            // extract information about the terminal node
            serverInstanceListFuture = CompletableFuture.completedFuture(serverInstanceListFactory.createTerminalNodeInstanceList(node, linkDataDuplexMap));
        } else if (nodeServiceType.isQueue()) {
            serverInstanceListFuture = CompletableFuture.completedFuture(serverInstanceListFactory.createQueueNodeInstanceList(node, linkDataDuplexMap));
        } else if (nodeServiceType.isUser()) {
            serverInstanceListFuture = CompletableFuture.completedFuture(serverInstanceListFactory.createUserNodeInstanceList());
        } else {
            serverInstanceListFuture = CompletableFuture.completedFuture(serverInstanceListFactory.createEmptyNodeInstanceList());
        }
        return serverInstanceListFuture.thenAccept(node::setServerInstanceList);
    }
}
