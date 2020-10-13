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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.util.CollectionUtils;

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
 * @author jaehong.kim
 */
public class DefaultNodeHistogramAppender implements NodeHistogramAppender {

    private final NodeHistogramFactory nodeHistogramFactory;

    private final Executor executor;

    public DefaultNodeHistogramAppender(NodeHistogramFactory nodeHistogramFactory, Executor executor) {
        this.nodeHistogramFactory = Objects.requireNonNull(nodeHistogramFactory, "nodeHistogramFactory");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public void appendNodeHistogram(final Range range, final NodeList nodeList, final LinkList linkList, final long timeoutMillis) {
        if (nodeList == null) {
            return;
        }
        final Collection<Node> nodes = nodeList.getNodeList();
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        final AtomicBoolean stopSign = new AtomicBoolean();
        final CompletableFuture[] futures = getNodeHistogramFutures(range, nodes, linkList, stopSign);
        if (-1 == timeoutMillis) {
            // Returns the result value when complete
            CompletableFuture.allOf(futures).join();
        } else {
            try {
                CompletableFuture.allOf(futures).get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) { // InterruptedException, ExecutionException, TimeoutException
                stopSign.set(Boolean.TRUE);
                String cause = "an error occurred while adding node histogram";
                if (e instanceof TimeoutException) {
                    cause += " build timed out. timeout=" + timeoutMillis + "ms";
                }
                throw new RuntimeException(cause, e);
            }
        }
    }

    private CompletableFuture[] getNodeHistogramFutures(Range range, Collection<Node> nodes, LinkList linkList, AtomicBoolean stopSign) {
        List<CompletableFuture<Void>> nodeHistogramFutures = new ArrayList<>();
        for (Node node : nodes) {
            CompletableFuture<Void> nodeHistogramFuture = getNodeHistogramFuture(range, node, linkList, stopSign);
            nodeHistogramFutures.add(nodeHistogramFuture);
        }
        return nodeHistogramFutures.toArray(new CompletableFuture[0]);
    }

    private CompletableFuture<Void> getNodeHistogramFuture(Range range, Node node, LinkList linkList, AtomicBoolean stopSign) {
        CompletableFuture<NodeHistogram> nodeHistogramFuture;
        final Application application = node.getApplication();
        final ServiceType serviceType = application.getServiceType();

        if (serviceType.isWas()) {
            // for WAS nodes, set their own response time histogram
            final Application wasNode = node.getApplication();
            nodeHistogramFuture = CompletableFuture.supplyAsync(new Supplier<NodeHistogram>() {
                @Override
                public NodeHistogram get() {
                    if (Boolean.TRUE == stopSign.get()) { // Stop
                        return nodeHistogramFactory.createEmptyNodeHistogram(application, range);
                    }
                    return nodeHistogramFactory.createWasNodeHistogram(wasNode, range);
                }
            }, executor);
        } else if (serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isAlias()) {
            nodeHistogramFuture = CompletableFuture.completedFuture(nodeHistogramFactory.createTerminalNodeHistogram(application, range, linkList));
        } else if (serviceType.isQueue()) {
            // Virtual queue node - queues with agent installed will be handled above as a WAS node
            nodeHistogramFuture = CompletableFuture.completedFuture(nodeHistogramFactory.createQueueNodeHistogram(application, range, linkList));
        } else if (serviceType.isUser()) {
            nodeHistogramFuture = CompletableFuture.completedFuture(nodeHistogramFactory.createUserNodeHistogram(application, range, linkList));
        } else {
            nodeHistogramFuture = CompletableFuture.completedFuture(nodeHistogramFactory.createEmptyNodeHistogram(application, range));
        }
        return nodeHistogramFuture.thenAccept(node::setNodeHistogram);
    }
}
