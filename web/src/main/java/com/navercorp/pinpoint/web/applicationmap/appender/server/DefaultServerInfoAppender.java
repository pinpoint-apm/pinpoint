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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.common.server.util.time.Range;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
public class DefaultServerInfoAppender implements ServerInfoAppender {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServerInstanceListFactory serverInstanceListFactory;

    private final Executor executor;

    public DefaultServerInfoAppender(ServerInstanceListFactory serverInstanceListFactory, Executor executor) {
        this.serverInstanceListFactory = Objects.requireNonNull(serverInstanceListFactory, "serverInstanceListFactory");
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
        final AtomicBoolean stopSign = new AtomicBoolean();
        final CompletableFuture[] futures = getServerInstanceListFutures(range, nodes, linkDataDuplexMap, stopSign);
        if (-1 == timeoutMillis) {
            // Returns the result value when complete
            CompletableFuture.allOf(futures).join();
        } else {
            try {
                CompletableFuture.allOf(futures).get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) { // InterruptedException, ExecutionException, TimeoutException
                stopSign.set(Boolean.TRUE);
                String cause = "an error occurred while adding server info";
                if (e instanceof TimeoutException) {
                    cause += " build timed out. timeout=" + timeoutMillis + "ms";
                }
                throw new RuntimeException(cause, e);
            }
        }
    }

    private CompletableFuture[] getServerInstanceListFutures(Range range, Collection<Node> nodes, LinkDataDuplexMap linkDataDuplexMap, AtomicBoolean stopSign) {
        List<CompletableFuture<Void>> serverInstanceListFutures = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getServiceType().isUnknown()) {
                // we do not know the server info for unknown nodes
                continue;
            }
            CompletableFuture<Void> serverInstanceListFuture = getServerInstanceListFuture(range, node, linkDataDuplexMap, stopSign);
            serverInstanceListFutures.add(serverInstanceListFuture);
        }
        return serverInstanceListFutures.toArray(new CompletableFuture[0]);
    }

    private CompletableFuture<Void> getServerInstanceListFuture(Range range, Node node, LinkDataDuplexMap linkDataDuplexMap, AtomicBoolean stopSign) {
        CompletableFuture<ServerInstanceList> serverInstanceListFuture;
        ServiceType nodeServiceType = node.getServiceType();
        if (nodeServiceType.isWas()) {
            serverInstanceListFuture = CompletableFuture.supplyAsync(new Supplier<ServerInstanceList>() {
                @Override
                public ServerInstanceList get() {
                    if (Boolean.TRUE == stopSign.get()) { // Stop
                        return serverInstanceListFactory.createEmptyNodeInstanceList();
                    }
                    return serverInstanceListFactory.createWasNodeInstanceList(node, range);
                }
            }, executor);
        } else if (nodeServiceType.isTerminal() || nodeServiceType.isAlias()) {
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
