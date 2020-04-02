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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.web.applicationmap.appender.histogram.EmptyNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppender;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.EmptyServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppender;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Range range;

    private final NodeHistogramAppenderFactory nodeHistogramAppenderFactory;
    private final ServerInfoAppenderFactory serverInfoAppenderFactory;

    private NodeType nodeType;
    private LinkType linkType;
    private NodeHistogramFactory nodeHistogramFactory;
    private ServerInstanceListFactory serverInstanceListFactory;

    public ApplicationMapBuilder(Range range, NodeHistogramAppenderFactory nodeHistogramAppenderFactory, ServerInfoAppenderFactory serverInfoAppenderFactory) {
        this.range = Objects.requireNonNull(range, "range");
        this.nodeHistogramAppenderFactory = Objects.requireNonNull(nodeHistogramAppenderFactory, "nodeHistogramAppenderFactory");
        this.serverInfoAppenderFactory = Objects.requireNonNull(serverInfoAppenderFactory, "serverInfoAppenderFactory");
    }

    public ApplicationMapBuilder nodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public ApplicationMapBuilder linkType(LinkType linkType) {
        this.linkType = linkType;
        return this;
    }

    public ApplicationMapBuilder includeNodeHistogram(NodeHistogramFactory nodeHistogramFactory) {
        this.nodeHistogramFactory = nodeHistogramFactory;
        return this;
    }

    public ApplicationMapBuilder includeServerInfo(ServerInstanceListFactory serverInstanceListFactory) {
        this.serverInstanceListFactory = serverInstanceListFactory;
        return this;
    }

    public ApplicationMap build(Application application) {
        logger.info("Building empty application map");

        NodeList nodeList = new NodeList();
        LinkList emptyLinkList = new LinkList();

        NodeType nodeType = this.nodeType;
        if (nodeType == null) {
            nodeType = NodeType.DETAILED;
        }

        Node node = new Node(nodeType, application);
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
        NodeHistogramAppender nodeHistogramAppender = nodeHistogramAppenderFactory.create(nodeHistogramFactory);
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, emptyLinkList);

        return new DefaultApplicationMap(range, nodeList, emptyLinkList);
    }

    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataDuplexMap");
        }
        logger.info("Building application map");

        NodeType nodeType = this.nodeType;
        if (nodeType == null) {
            nodeType = NodeType.DETAILED;
        }

        LinkType linkType = this.linkType;
        if (linkType == null) {
            linkType = LinkType.DETAILED;
        }

        NodeList nodeList = NodeListFactory.createNodeList(nodeType, linkDataDuplexMap);
        LinkList linkList = LinkListFactory.createLinkList(linkType, nodeList, linkDataDuplexMap, range);

        NodeHistogramFactory nodeHistogramFactory = this.nodeHistogramFactory;
        if (nodeHistogramFactory == null) {
            nodeHistogramFactory = new EmptyNodeHistogramFactory();
        }
        NodeHistogramAppender nodeHistogramAppender = nodeHistogramAppenderFactory.create(nodeHistogramFactory);
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);

        ServerInstanceListFactory serverInstanceListFactory = this.serverInstanceListFactory;
        if (serverInstanceListFactory == null) {
            serverInstanceListFactory = new EmptyServerInstanceListFactory();
        }
        ServerInfoAppender serverInfoAppender = serverInfoAppenderFactory.create(serverInstanceListFactory);
        serverInfoAppender.appendServerInfo(range, nodeList, linkDataDuplexMap);

        return new DefaultApplicationMap(range, nodeList, linkList);
    }
}
