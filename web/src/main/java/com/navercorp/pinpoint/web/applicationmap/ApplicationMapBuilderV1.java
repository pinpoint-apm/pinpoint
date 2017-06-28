/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.EmptyNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.EmptyServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkFactory.LinkType;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilderV1 implements ApplicationMapBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Range range;

    private LinkType linkType;
    private NodeHistogramFactory nodeHistogramFactory;
    private ServerInstanceListFactory serverInstanceListFactory;

    ApplicationMapBuilderV1(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
    }

    @Override
    public ApplicationMapBuilder linkType(LinkType linkType) {
        this.linkType = linkType;
        return this;
    }

    @Override
    public ApplicationMapBuilder includeNodeHistogram(NodeHistogramFactory nodeHistogramFactory) {
        this.nodeHistogramFactory = nodeHistogramFactory;
        return this;
    }

    @Override
    public ApplicationMapBuilder includeServerInfo(ServerInstanceListFactory serverInstanceListFactory) {
        this.serverInstanceListFactory = serverInstanceListFactory;
        return this;
    }

    @Override
    public ApplicationMap build(Application application) {
        logger.info("Building empty application map");

        NodeList nodeList = new NodeList();
        LinkList emptyLinkList = new LinkList();

        Node node = new Node(application);
        if (serverInstanceListFactory != null) {
            ServerInstanceList runningInstances = serverInstanceListFactory.createWasNodeInstanceList(node, range.getTo());
            if (runningInstances.getInstanceCount() > 0) {
                node.setServerInstanceList(runningInstances);
                nodeList.addNode(node);
            }
        }

        NodeHistogramFactory nodeHistogramFactory = this.nodeHistogramFactory;
        if (nodeHistogramFactory == null) {
            nodeHistogramFactory = new EmptyNodeHistogramFactory();
        }
        appendNodeResponseTime(nodeList, emptyLinkList, nodeHistogramFactory);

        return new DefaultApplicationMap(range, nodeList, emptyLinkList);
    }

    @Override
    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataDuplexMap must not be null");
        }
        logger.info("Building application map");

        LinkType linkType = this.linkType;
        if (linkType == null) {
            linkType = LinkType.DETAILED;
        }

        NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
        LinkList linkList = LinkListFactory.createLinkList(linkType, nodeList, linkDataDuplexMap, range);

        NodeHistogramFactory nodeHistogramFactory = this.nodeHistogramFactory;
        if (nodeHistogramFactory == null) {
            nodeHistogramFactory = new EmptyNodeHistogramFactory();
        }
        appendNodeResponseTime(nodeList, linkList, nodeHistogramFactory);

        ServerInstanceListFactory serverInstanceListFactory = this.serverInstanceListFactory;
        if (serverInstanceListFactory == null) {
            serverInstanceListFactory = new EmptyServerInstanceListFactory();
        }
        appendAgentInfo(nodeList, linkDataDuplexMap, serverInstanceListFactory);

        final ApplicationMap map = new DefaultApplicationMap(range, nodeList, linkList);
        return map;
    }

    public void appendNodeResponseTime(NodeList nodeList, LinkList linkList, NodeHistogramFactory nodeHistogramFactory) {
        final Collection<Node> nodes = nodeList.getNodeList();
        for (Node node : nodes) {
            final ServiceType nodeType = node.getServiceType();
            if (nodeType.isWas()) {
                // for WAS nodes, set their own response time histogram
                final Application wasNode = node.getApplication();
                final NodeHistogram nodeHistogram = nodeHistogramFactory.createWasNodeHistogram(wasNode, range);
                node.setNodeHistogram(nodeHistogram);

            } else if (nodeType.isTerminal() || nodeType.isUnknown()) {
                final NodeHistogram nodeHistogram = nodeHistogramFactory.createTerminalNodeHistogram(node.getApplication(), range, linkList);
                node.setNodeHistogram(nodeHistogram);
            } else if (nodeType.isQueue()) {
                // Virtual queue node - queues with agent installed will be handled above as a WAS node
                final NodeHistogram nodeHistogram = nodeHistogramFactory.createQueueNodeHistogram(node.getApplication(), range, linkList);
                node.setNodeHistogram(nodeHistogram);
            } else if (nodeType.isUser()) {
                // for User nodes, find its source link and create the histogram
                Application userNode = node.getApplication();
                final NodeHistogram nodeHistogram = nodeHistogramFactory.createUserNodeHistogram(userNode, range, linkList);
                node.setNodeHistogram(nodeHistogram);
            } else {
                // dummy data
                NodeHistogram dummy = nodeHistogramFactory.createEmptyNodeHistogram(node.getApplication(), range);
                node.setNodeHistogram(dummy);
            }
        }
    }

    private void appendAgentInfo(NodeList nodeList, LinkDataDuplexMap linkDataDuplexMap, ServerInstanceListFactory serverInstanceListFactory) {
        for (Node node : nodeList.getNodeList()) {
            appendServerInfo(node, linkDataDuplexMap, serverInstanceListFactory);
        }
    }

    private void appendServerInfo(Node node, LinkDataDuplexMap linkDataDuplexMap, ServerInstanceListFactory serverInstanceListFactory) {
        final ServiceType nodeServiceType = node.getServiceType();
        if (nodeServiceType.isUnknown()) {
            // we do not know the server info for unknown nodes
            return;
        }
        ServerInstanceList serverInstanceList;
        if (nodeServiceType.isWas()) {
            long timestamp = range.getTo();
            serverInstanceList = serverInstanceListFactory.createWasNodeInstanceList(node, timestamp);
            node.setServerInstanceList(serverInstanceList);
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