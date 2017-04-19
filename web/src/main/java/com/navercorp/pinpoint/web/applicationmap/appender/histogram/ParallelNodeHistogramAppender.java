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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.Link;
import com.navercorp.pinpoint.web.applicationmap.LinkList;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.NodeList;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
public class ParallelNodeHistogramAppender implements NodeHistogramAppender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NodeHistogramDataSource nodeHistogramDataSource;

    private final ExecutorService executorService;

    public ParallelNodeHistogramAppender(NodeHistogramDataSource nodeHistogramDataSource, ExecutorService executorService) {
        if (nodeHistogramDataSource == null) {
            throw new NullPointerException("nodeHistogramDataSource must not be null");
        }
        if (executorService == null) {
            throw new NullPointerException("executorService must not be null");
        }
        this.nodeHistogramDataSource = nodeHistogramDataSource;
        this.executorService = executorService;
    }

    @Override
    public void appendNodeHistogram(Range range, NodeList nodeList, LinkList linkList) {
        if (nodeList == null) {
            return;
        }
        final Collection<Node> nodes = nodeList.getNodeList();
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        CompletableFuture[] futures = getNodeHistogramFutures(range, nodes, linkList);
        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            logger.error("Error appending node histograms", e);
        }
    }

    private CompletableFuture[] getNodeHistogramFutures(Range range, Collection<Node> nodes, LinkList linkList) {
        List<CompletableFuture<Void>> nodeHistogramFutures = new ArrayList<>();
        for (Node node : nodes) {
            CompletableFuture<Void> nodeHistogramFuture = getNodeHistogramFuture(range, node, linkList);
            nodeHistogramFutures.add(nodeHistogramFuture);
        }
        return nodeHistogramFutures.toArray(new CompletableFuture[nodeHistogramFutures.size()]);
    }

    private CompletableFuture<Void> getNodeHistogramFuture(Range range, Node node, LinkList linkList) {
        CompletableFuture<NodeHistogram> nodeHistogramFuture;
        final ServiceType nodeType = node.getServiceType();
        if (nodeType.isWas()) {
            // for WAS nodes, set their own response time histogram
            final Application wasNode = node.getApplication();
            nodeHistogramFuture = CompletableFuture.supplyAsync(new Supplier<NodeHistogram>() {
                @Override
                public NodeHistogram get() {
                    return nodeHistogramDataSource.createNodeHistogram(wasNode, range);
                }
            }, executorService);
        } else if (nodeType.isTerminal() || nodeType.isUnknown()) {
            nodeHistogramFuture = CompletableFuture.completedFuture(createTerminalNodeHistogram(range, node, linkList));
        } else if (nodeType.isQueue()) {
            // Virtual queue node - queues with agent installed will be handled above as a WAS node
            nodeHistogramFuture = CompletableFuture.completedFuture(createTerminalNodeHistogram(range, node, linkList));
        } else if (nodeType.isUser()) {
            nodeHistogramFuture = CompletableFuture.completedFuture(createUserNodeHistogram(range, node, linkList));
        } else {
            nodeHistogramFuture = CompletableFuture.completedFuture(new NodeHistogram(node.getApplication(), range));
        }
        return nodeHistogramFuture.thenAccept(node::setNodeHistogram);
    }

    private NodeHistogram createTerminalNodeHistogram(Range range, Node node, LinkList linkList) {
        // for Terminal nodes, add all links pointing to iself and create the histogram
        final Application nodeApplication = node.getApplication();
        final NodeHistogram nodeHistogram = new NodeHistogram(nodeApplication, range);

        // create applicationHistogram
        final List<Link> toLinkList = linkList.findToLink(nodeApplication);
        final Histogram applicationHistogram = new Histogram(node.getServiceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeHistogram.setApplicationHistogram(applicationHistogram);

        // create applicationTimeHistogram
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
            linkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(nodeApplication, range);
        ApplicationTimeHistogram applicationTimeHistogram = builder.build(linkCallDataMap.getLinkDataList());
        nodeHistogram.setApplicationTimeHistogram(applicationTimeHistogram);

        // for Terminal nodes, create AgentLevel histogram
        if (nodeApplication.getServiceType().isTerminal() || nodeApplication.getServiceType().isQueue()) {
            final Map<String, Histogram> agentHistogramMap = new HashMap<>();

            for (Link link : toLinkList) {
                LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
                AgentHistogramList targetList = sourceLinkCallDataMap.getTargetList();
                for (AgentHistogram histogram : targetList.getAgentHistogramList()) {
                    Histogram find = agentHistogramMap.get(histogram.getId());
                    if (find == null) {
                        find = new Histogram(histogram.getServiceType());
                        agentHistogramMap.put(histogram.getId(), find);
                    }
                    find.add(histogram.getHistogram());
                }
                nodeHistogram.setAgentHistogramMap(agentHistogramMap);
            }
        }

        LinkCallDataMap mergeSource = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
            mergeSource.addLinkDataMap(sourceLinkCallDataMap);
        }

        AgentTimeHistogramBuilder agentTimeBuilder = new AgentTimeHistogramBuilder(nodeApplication, range);
        AgentTimeHistogram agentTimeHistogram = agentTimeBuilder.buildTarget(mergeSource);
        nodeHistogram.setAgentTimeHistogram(agentTimeHistogram);

        return nodeHistogram;
    }

    private NodeHistogram createUserNodeHistogram(Range range, Node node, LinkList linkList) {
        // for User nodes, find its source link and create the histogram
        Application userNode = node.getApplication();

        final NodeHistogram nodeHistogram = new NodeHistogram(userNode, range);
        final List<Link> fromLink = linkList.findFromLink(userNode);
        if (fromLink.size() > 1) {
            // used first(0) link.
            logger.warn("Invalid from UserNode:{}", linkList.getLinkList());
        } else if (fromLink.isEmpty()) {
            logger.warn("from UserNode not found:{}", userNode);
            return null;
        }
        final Link sourceLink = fromLink.get(0);
        nodeHistogram.setApplicationHistogram(sourceLink.getHistogram());

        ApplicationTimeHistogram histogramData = sourceLink.getTargetApplicationTimeSeriesHistogramData();
        nodeHistogram.setApplicationTimeHistogram(histogramData);
        return nodeHistogram;
    }

}
