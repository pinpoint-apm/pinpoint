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

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HyunGil Jeong
 */
public class LinkListFactory {

    private static final Logger logger = LoggerFactory.getLogger(LinkListFactory.class);

    public static LinkList createLinkList(LinkType linkType, NodeList nodeList, LinkDataDuplexMap linkDataDuplexMap, Range range) {
        // don't change
        LinkList linkList = new LinkList();
        createSourceLink(linkType, nodeList, linkList, linkDataDuplexMap.getSourceLinkDataMap(), range);
        logger.debug("link size:{}", linkList.size());
        createTargetLink(linkType, nodeList, linkList, linkDataDuplexMap.getTargetLinkDataMap(), range);
        logger.debug("link size:{}", linkList.size());

        for (Link link : linkList.getLinkList()) {
            appendLinkHistogram(link, linkDataDuplexMap);
        }
        return linkList;
    }

    private static void createSourceLink(LinkType linkType, NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap, Range range) {
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
                    final Link link = addLink(linkType, linkList, fromNode, toNode, CreateType.Source, range);
                    if (link != null) {
                        logger.debug("createRpcSourceLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkType, linkList, fromNode, toNode, CreateType.Source, range);
                if (link != null) {
                    logger.debug("createSourceLink:{}", link);
                }
            }
        }
    }

    private static void createTargetLink(LinkType linkType, NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap, Range range) {
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
                    final Link link = addLink(linkType, linkList, fromNode, toNode, CreateType.Target, range);
                    if (link != null) {
                        logger.debug("createRpcTargetLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkType, linkList, fromNode, toNode, CreateType.Target, range);
                if (link != null) {
                    logger.debug("createTargetLink:{}", link);
                }
            }
        }
    }

    private static Link addLink(LinkType linkType, LinkList linkList, Node fromNode, Node toNode, CreateType createType, Range range) {
        final Link link = new Link(linkType, createType, fromNode, toNode, range);
        if (linkList.addLink(link)) {
            return link;
        } else {
            return null;
        }
    }

    private static void appendLinkHistogram(Link link, LinkDataDuplexMap linkDataDuplexMap) {
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
}
