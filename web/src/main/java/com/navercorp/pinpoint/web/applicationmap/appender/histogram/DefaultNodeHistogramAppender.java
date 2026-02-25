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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.concurrent.FutureUtils;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.util.CancellableHistogramFactory;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 * @author jaehong.kim
 */
public class DefaultNodeHistogramAppender implements NodeHistogramAppender {

    private final CancellableHistogramFactory nodeHistogramFactory;

    private final Executor executor;

    public DefaultNodeHistogramAppender(NodeHistogramFactory nodeHistogramFactory, Executor executor) {
        Objects.requireNonNull(nodeHistogramFactory, "nodeHistogramFactory");
        this.nodeHistogramFactory = new CancellableHistogramFactory(nodeHistogramFactory);
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public void appendNodeHistogram(final TimeWindow timeWindow, final NodeList nodeList, final LinkList linkList, final long timeoutMillis) {
        if (NodeList.isEmpty(nodeList)) {
            return;
        }
        final Collection<Node> nodes = nodeList.getNodeList();
        final Node[] nodeArray = nodes.toArray(new Node[0]);
        final CompletableFuture<List<NodeHistogram>> future = getNodeHistogramList(timeWindow, nodeArray, linkList);
        List<NodeHistogram> result = allOf(future, timeoutMillis, nodeHistogramFactory::cancel);
        bindHistogramToNode(nodeArray, result);
    }

    private void bindHistogramToNode(Node[] nodes, List<NodeHistogram> histogramList) {
        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            NodeHistogram nodeHistogram = histogramList.get(i);
            node.setNodeHistogram(nodeHistogram);
        }
    }

    private <T> T allOf(Future<T> future, long timeoutMillis, Runnable stopAction) {
        Objects.requireNonNull(future, "future");
        Objects.requireNonNull(stopAction, "stopAction");

        boolean success = false;
        try {
            T t = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            success = true;
            return t;
        } catch (ExecutionException e) {
            throw new RuntimeException("NodeHistogram create error", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("NodeHistogram interrupt error", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("NodeHistogram timeout error timeout:" + timeoutMillis, e);
        } finally {
            if (!success) {
                stopAction.run();
            }
        }
    }

    private CompletableFuture<List<NodeHistogram>> getNodeHistogramList(TimeWindow timeWindow, Node[] nodes, LinkList linkList) {
        @SuppressWarnings("unchecked")
        CompletableFuture<NodeHistogram>[] futures = new CompletableFuture[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            futures[i] = asyncNodeHistogram(timeWindow, node, linkList);
        }
        return FutureUtils.allOfAsync(futures);
    }

    private CompletableFuture<NodeHistogram> asyncNodeHistogram(TimeWindow timeWindow, Node node, LinkList linkList) {
        final Application application = node.getApplication();
        final ServiceType serviceType = application.getServiceType();
        final Range range = timeWindow.getWindowRange();
        if (serviceType.isWas()) {
            // for WAS nodes, set their own response time histogram
            final Application wasNode = node.getApplication();
            return CompletableFuture.supplyAsync(new Supplier<NodeHistogram>() {
                @Override
                public NodeHistogram get() {
                    return nodeHistogramFactory.createWasNodeHistogram(wasNode, timeWindow);
                }
            }, executor);
        } else if (serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isAlias()) {
            return CompletableFuture.completedFuture(nodeHistogramFactory.createTerminalNodeHistogram(application, range, linkList));
        } else if (serviceType.isQueue()) {
            // Virtual queue node - queues with agent installed will be handled above as a WAS node
            return CompletableFuture.completedFuture(nodeHistogramFactory.createQueueNodeHistogram(application, range, linkList));
        } else if (serviceType.isUser()) {
            return CompletableFuture.completedFuture(nodeHistogramFactory.createUserNodeHistogram(application, range, linkList));
        }

        return CompletableFuture.completedFuture(nodeHistogramFactory.createEmptyNodeHistogram(application, range));
    }

}
