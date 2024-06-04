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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    public void appendServerInfo(final Range range, final NodeList source, final LinkDataDuplexMap linkDataDuplexMap, final long timeoutMillis) {
        if (source == null) {
            return;
        }
        Collection<Node> nodes = source.getNodeList();
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }

        final CompletableFuture<ServerGroupList>[] futures = getServerGroupListFutures(range, nodes, linkDataDuplexMap);
        CompletableFuture.allOf(futures).cancel(true);
        if (-1 == timeoutMillis) {
            // Returns the result value when complete
            CompletableFuture.allOf(futures).join();
        } else {
            try {
                CompletableFuture.allOf(futures).get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                CompletableFuture.allOf(futures).cancel(true);
                String cause = "an error occurred while adding server info";
                if (e instanceof TimeoutException) {
                    cause += " build timed out. timeout=" + timeoutMillis + "ms";
                }
                throw new RuntimeException(cause, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<ServerGroupList>[] getServerGroupListFutures(Range range, Collection<Node> nodes, LinkDataDuplexMap linkDataDuplexMap) {
        List<CompletableFuture<ServerGroupList>> serverGroupListFutures = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getServiceType().isUnknown()) {
                // we do not know the server info for unknown nodes
                continue;
            }
            CompletableFuture<ServerGroupList> serverGroupListFuture = getServerGroupListFuture(range, node, linkDataDuplexMap);
            serverGroupListFutures.add(serverGroupListFuture);
        }

        return serverGroupListFutures.toArray(new CompletableFuture[0]);
    }

    private CompletableFuture<ServerGroupList> getServerGroupListFuture(Range range, Node node, LinkDataDuplexMap linkDataDuplexMap) {
        CompletableFuture<ServerGroupList> serverGroupListFuture = getServerGroupListFuture0(node, range, linkDataDuplexMap);
        serverGroupListFuture.whenComplete((serverGroupList, throwable) -> {
            if (throwable != null) {
                // error
                logger.warn("Failed to get server info for node {}", node, throwable);
                node.setServerGroupList(serverGroupListFactory.createEmptyNodeInstanceList());
            } else {
                logger.trace("ServerGroupList: {}", serverGroupList);
                node.setServerGroupList(serverGroupList);
            }
        });
        return serverGroupListFuture;
}

    private CompletableFuture<ServerGroupList> getServerGroupListFuture0(Node node, Range range, LinkDataDuplexMap linkDataDuplexMap) {
        final Application application = node.getApplication();
        final ServiceType nodeServiceType = application.getServiceType();
        if (nodeServiceType.isWas()) {
            return CompletableFuture.supplyAsync(new Supplier<>() {
                @Override
                public ServerGroupList get() {
                    final Instant to = range.getToInstant();
                    return serverGroupListFactory.createWasNodeInstanceList(node, to);
                }
            }, executor);
        } else if (nodeServiceType.isTerminal() || nodeServiceType.isAlias()) {
            // extract information about the terminal node
            return CompletableFuture.completedFuture(serverGroupListFactory.createTerminalNodeInstanceList(application, linkDataDuplexMap));
        } else if (nodeServiceType.isQueue()) {
            return CompletableFuture.completedFuture(serverGroupListFactory.createQueueNodeInstanceList(application, linkDataDuplexMap));
        } else if (nodeServiceType.isUser()) {
            return CompletableFuture.completedFuture(serverGroupListFactory.createUserNodeInstanceList());
        }
        return CompletableFuture.completedFuture(serverGroupListFactory.createEmptyNodeInstanceList());
    }
}
