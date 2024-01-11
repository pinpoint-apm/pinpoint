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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author HyunGil Jeong
 */
public class LinkListFactory {

    private static final Logger logger = LogManager.getLogger(LinkListFactory.class);

    public static LinkList createLinkList(NodeList nodeList, LinkDataDuplexMap linkDataDuplexMap, Range range) {
        // don't change
        LinkList linkList = new LinkList();
        createSourceLink(nodeList, linkList, linkDataDuplexMap.getSourceLinkDataMap(), range);
        int before = linkList.size();
        logger.debug("total link (in search) size:{}", before);
        createTargetLink(nodeList, linkList, linkDataDuplexMap.getTargetLinkDataMap(), range);
        logger.debug("total link (out search) size:{}->{}", before, linkList.size());

        for (Link link : linkList.getLinkList()) {
            appendLinkHistogram(link, linkDataDuplexMap);
        }
        return linkList;
    }

    private static void createSourceLink(NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap, Range range) {
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = nodeList.findNode(fromApplicationId);

            final Application toApplicationId = linkData.getToApplication();
            Node toNode = nodeList.findNode(toApplicationId);

            // rpc client missing
            if (toNode == null) {
                logger.warn("toNode rpc client not found:{}", toApplicationId);
                continue;
            }

            // for RPC clients: skip if there is a dest application, convert to "unknown cloud" if not
            // shouldn't really be necessary as rpc client toNodes are converted to unknown nodes beforehand.
            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeList.containsNode(toNode.getApplication()) || toNode.getServiceType().isAlias()) {
                    final Link link = addLink(linkList, fromNode, toNode, LinkDirection.IN_LINK, range);
                    if (link != null) {
                        logger.debug("createRpcSourceLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkList, fromNode, toNode, LinkDirection.IN_LINK, range);
                if (link != null) {
                    logger.debug("createSourceLink:{}", link);
                }
            }
        }
    }

    private static void createTargetLink(NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap, Range range) {
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = nodeList.findNode(fromApplicationId);

            final Application toApplicationId = linkData.getToApplication();
            Node toNode = nodeList.findNode(toApplicationId);

            // rpc client missing
            if (fromNode == null) {
                logger.warn("fromNode rpc client not found:{}", toApplicationId);
                continue;
            }

            // for RPC clients: skip if there is a dest application, convert to "unknown cloud" if not
            if (toNode.getServiceType().isRpcClient()) {
                // check if "to" node exists
                if (!nodeList.containsNode(toNode.getApplication())) {
                    final Link link = addLink(linkList, fromNode, toNode, LinkDirection.OUT_LINK, range);
                    if (link != null) {
                        logger.debug("createRpcTargetLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkList, fromNode, toNode, LinkDirection.OUT_LINK, range);
                if (link != null) {
                    logger.debug("createTargetLink:{}", link);
                }
            }
        }
    }

    private static Link addLink(LinkList linkList, Node fromNode, Node toNode, LinkDirection direction, Range range) {
        final Link link = new Link(direction, fromNode, toNode, range);
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
            link.addInLink(sourceLinkData.getLinkCallDataMap());
        }
        LinkData targetLinkData = linkDataDuplexMap.getTargetLinkData(key);
        if (targetLinkData != null) {
            link.addOutLink(targetLinkData.getLinkCallDataMap());
        }
    }
}
