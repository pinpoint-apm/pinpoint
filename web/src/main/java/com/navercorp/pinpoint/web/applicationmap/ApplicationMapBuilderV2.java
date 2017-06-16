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
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilderV2 implements ApplicationMapBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Range range;

    private final NodeHistogramAppenderFactory nodeHistogramAppenderFactory;
    private final ServerInfoAppenderFactory serverInfoAppenderFactory;

    private NodeHistogramFactory nodeHistogramFactory;
    private ServerInstanceListFactory serverInstanceListFactory;

    ApplicationMapBuilderV2(Range range, NodeHistogramAppenderFactory nodeHistogramAppenderFactory, ServerInfoAppenderFactory serverInfoAppenderFactory) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (nodeHistogramAppenderFactory == null) {
            throw new NullPointerException("nodeHistogramAppenderFactory must not be null");
        }
        if (serverInfoAppenderFactory == null) {
            throw new NullPointerException("serverInfoAppenderFactory must not be null");
        }
        this.range = range;
        this.nodeHistogramAppenderFactory = nodeHistogramAppenderFactory;
        this.serverInfoAppenderFactory = serverInfoAppenderFactory;
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
        return new DefaultApplicationMap(range, nodeList, emptyLinkList);
    }

    @Override
    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap) {
        logger.info("Building application map");

        NodeList nodeList = buildNode(linkDataDuplexMap);
        LinkList linkList = buildLink(nodeList, linkDataDuplexMap);

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

    private NodeList buildNode(LinkDataDuplexMap linkDataDuplexMap) {
        NodeList nodeList = new NodeList();
        createNode(nodeList, linkDataDuplexMap.getSourceLinkDataMap());
        logger.debug("node size:{}", nodeList.size());
        createNode(nodeList, linkDataDuplexMap.getTargetLinkDataMap());
        logger.debug("node size:{}", nodeList.size());

        logger.debug("allNode:{}", nodeList.getNodeList());
        return nodeList;
    }

    private void createNode(NodeList nodeList, LinkDataMap linkDataMap) {
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplication = linkData.getFromApplication();
            // FROM is either a CLIENT or a node
            // cannot be RPC. Already converted to unknown.
            if (!fromApplication.getServiceType().isRpcClient()) {
                final boolean success = addNode(nodeList, fromApplication);
                if (success) {
                    logger.debug("createSourceNode:{}", fromApplication);
                }
            } else {
                logger.warn("found rpc fromNode linkData:{}", linkData);
            }

            final Application toApplication = linkData.getToApplication();
            // FROM -> TO : TO is either a CLIENT or a node
            if (!toApplication.getServiceType().isRpcClient()) {
                final boolean success = addNode(nodeList, toApplication);
                if (success) {
                    logger.debug("createTargetNode:{}", toApplication);
                }
            } else {
                logger.warn("found rpc toNode:{}", linkData);
            }
        }
    }

    private boolean addNode(NodeList nodeList, Application application) {
        if (nodeList.containsNode(application)) {
            return false;
        }
        Node fromNode = new Node(application);
        return nodeList.addNode(fromNode);
    }

    private LinkList buildLink(NodeList nodeList, LinkDataDuplexMap linkDataDuplexMap) {
        // don't change
        LinkList linkList = new LinkList();
        createSourceLink(nodeList, linkList, linkDataDuplexMap.getSourceLinkDataMap());
        logger.debug("link size:{}", linkList.size());
        createTargetLink(nodeList, linkList, linkDataDuplexMap.getTargetLinkDataMap());
        logger.debug("link size:{}", linkList.size());

        for (Link link : linkList.getLinkList()) {
            appendLinkHistogram(link, linkDataDuplexMap);
        }
        return linkList;
    }

    private void appendLinkHistogram(Link link, LinkDataDuplexMap linkDataDuplexMap) {
        logger.debug("appendLinkHistogram link:{}", link);

        LinkKey key = link.getLinkKey();
        LinkData sourceLinkData = linkDataDuplexMap.getSourceLinkData(key);
        if (sourceLinkData != null) {
            link.addSource(sourceLinkData.getLinkCallDataMap());
        }
        LinkData targetLinkData = linkDataDuplexMap.getTargetLinkData(key);
        if (targetLinkData != null) {
            link.addTarget(targetLinkData.getLinkCallDataMap());
        }
    }

    private void createSourceLink(NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap) {
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = nodeList.findNode(fromApplicationId);

            final Application toApplicationId = linkData.getToApplication();
            Node toNode = nodeList.findNode(toApplicationId);

            // rpc client missing
            if (toNode == null) {
                logger.warn("toNode rcp client not found:{}", toApplicationId);
                continue;
            }

            // for RPC clients: skip if there is a dest application, convert to "unknown cloud" if not
            // shouldn't really be necessary as rpc client toNodes are converted to unknown nodes beforehand.
            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeList.containsNode(toNode.getApplication())) {
                    final Link link = addLink(linkList, fromNode, toNode, CreateType.Source);
                    if (link != null) {
                        logger.debug("createRpcSourceLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkList, fromNode, toNode, CreateType.Source);
                if (link != null) {
                    logger.debug("createSourceLink:{}", link);
                }
            }
        }
    }

    private Link addLink(LinkList linkList, Node fromNode, Node toNode, CreateType createType) {
        final Link link = new Link(createType, fromNode, toNode, range);
        if (linkList.addLink(link)) {
            return link;
        } else {
            return null;
        }
    }

    private void createTargetLink(NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap) {
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = nodeList.findNode(fromApplicationId);

            final Application toApplicationId = linkData.getToApplication();
            Node toNode = nodeList.findNode(toApplicationId);

            // rpc client missing
            if (fromNode == null) {
                logger.warn("fromNode rcp client not found:{}", toApplicationId);
                continue;
            }

            // for RPC clients: skip if there is a dest application, convert to "unknown cloud" if not
            if (toNode.getServiceType().isRpcClient()) {
                // check if "to" node exists
                if (!nodeList.containsNode(toNode.getApplication())) {
                    final Link link = addLink(linkList, fromNode, toNode, CreateType.Target);
                    if (link != null) {
                        logger.debug("createRpcTargetLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkList, fromNode, toNode, CreateType.Target);
                if (link != null) {
                    logger.debug("createTargetLink:{}", link);
                }
            }
        }
    }
}
