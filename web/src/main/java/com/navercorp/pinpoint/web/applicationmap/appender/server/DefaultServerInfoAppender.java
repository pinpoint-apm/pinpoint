/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
public class DefaultServerInfoAppender implements ServerInfoAppender {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServerGroupListFactory serverGroupListFactory;

    private final Executor executor;

    public DefaultServerInfoAppender(ServerGroupListFactory serverGroupListFactory, Executor executor) {
        this.serverGroupListFactory = Objects.requireNonNull(serverGroupListFactory, "serverGroupListFactory");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public void appendServerInfo(final Range range, final NodeList source, final LinkDataDuplexMap linkDataDuplexMap, long timeoutMillis) {
        if (NodeList.isEmpty(source)) {
            return;
        }

        Collection<Node> nodes = source.getNodeList();

        final List<ServerGroupRequest> serverGroupRequest = getServerGroupListFutures(range, nodes, linkDataDuplexMap);

        timeoutMillis = defaultTimeoutMillis(timeoutMillis);
        join(serverGroupRequest, timeoutMillis);

        bind(serverGroupRequest);
    }

    private long defaultTimeoutMillis(long timeoutMillis) {
        if (timeoutMillis == -1) {
            return Long.MAX_VALUE;
        }
        return timeoutMillis;
    }

    private List<ServerGroupRequest> getServerGroupListFutures(Range range, Collection<Node> nodes, LinkDataDuplexMap linkDataDuplexMap) {
        List<ServerGroupRequest> serverGroupListFutures = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getServiceType().isUnknown()) {
                // we do not know the server info for unknown nodes
                continue;
            }
            CompletableFuture<ServerGroupList> serverGroupListFuture = getServerGroupListFuture(range, node, linkDataDuplexMap);
            serverGroupListFutures.add(new ServerGroupRequest(node, serverGroupListFuture));
        }
        return serverGroupListFutures;
    }

    private record ServerGroupRequest(Node node, CompletableFuture<ServerGroupList> future) {
    }

    private CompletableFuture<ServerGroupList> getServerGroupListFuture(Range range, Node node, LinkDataDuplexMap linkDataDuplexMap) {
        final Application application = node.getApplication();
        final ServiceType nodeServiceType = application.getServiceType();
        if (nodeServiceType.isWas()) {
            return CompletableFuture.supplyAsync(new Supplier<>() {
                @Override
                public ServerGroupList get() {
                    final long to = range.getTo();
                    return serverGroupListFactory.createWasNodeInstanceList(node, to);
                }
            }, executor);
        } else if (nodeServiceType.isTerminal() || nodeServiceType.isAlias()) {
            // extract information about the terminal node
            return CompletableFuture.completedFuture(serverGroupListFactory.createTerminalNodeInstanceList(node, linkDataDuplexMap));
        } else if (nodeServiceType.isQueue()) {
            return CompletableFuture.completedFuture(serverGroupListFactory.createQueueNodeInstanceList(node, linkDataDuplexMap));
        } else if (nodeServiceType.isUser()) {
            return CompletableFuture.completedFuture(serverGroupListFactory.createUserNodeInstanceList());
        }
        return CompletableFuture.completedFuture(serverGroupListFactory.createEmptyNodeInstanceList());
    }


    private void join(List<ServerGroupRequest> serverGroupRequests, long timeoutMillis) {
        @SuppressWarnings("rawtypes")
        final CompletableFuture[] futures = serverGroupRequests.stream()
                .map(ServerGroupRequest::future)
                .toArray(CompletableFuture[]::new);

        final CompletableFuture<Void> all = CompletableFuture.allOf(futures);
        try {
            all.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            all.cancel(false);
            String cause = "an error occurred while adding server info";
            if (e instanceof TimeoutException) {
                cause += " build timed out. timeout=" + timeoutMillis + "ms";
            }
            throw new RuntimeException(cause, e);
        }
    }

    private void bind(List<ServerGroupRequest> serverGroupRequest) {
        for (ServerGroupRequest pair : serverGroupRequest) {
            CompletableFuture<ServerGroupList> future = pair.future();
            try {
                ServerGroupList serverGroupList = future.getNow(ServerGroupList.empty());
                Node node = pair.node();
                node.setServerGroupList(serverGroupList);
            } catch (Throwable th) {
                logger.warn("Failed to get server info for node {}", pair.node());
                throw new RuntimeException("Unexpected error", th);
            }
        }
    }

}
