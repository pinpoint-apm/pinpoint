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

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author HyunGil Jeong
 * @author jaehong.kim
 */
public class NodeListFactory {

    private static final Logger logger = LogManager.getLogger(NodeListFactory.class);

    public static NodeList createNodeList(LinkDataDuplexMap linkDataDuplexMap) {
        NodeList nodeList = new NodeList();
        createNode(nodeList, linkDataDuplexMap.getSourceLinkDataMap());
        logger.debug("node size:{}", nodeList.size());
        createNode(nodeList, linkDataDuplexMap.getTargetLinkDataMap());
        logger.debug("node size:{}", nodeList.size());

        logger.debug("allNode:{}", nodeList.getNodeList());
        return nodeList;
    }

    private static void createNode(NodeList nodeList, LinkDataMap linkDataMap) {
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplication = linkData.getFromApplication();
            final Application toApplication = linkData.getToApplication();

            // FROM is either a CLIENT or a node
            // cannot be RPC. Already converted to unknown.
            if (isFromNode(fromApplication, toApplication)) {
                final boolean success = addNode(nodeList, fromApplication);
                if (success) {
                    logger.debug("createSourceNode:{}", fromApplication);
                }
            } else {
                logger.warn("found rpc fromNode linkData:{}", linkData);
            }

            // FROM -> TO : TO is either a CLIENT or a node
            // create node when it's alias even if RPC
            if (isToNode(fromApplication, toApplication)) {
                final boolean success = addNode(nodeList, toApplication);
                if (success) {
                    logger.debug("createTargetNode:{}", toApplication);
                }
            } else {
                logger.warn("found rpc toNode:{}", linkData);
            }
        }
    }

    private static boolean isFromNode(final Application fromApplication, final Application toApplication) {
        if (fromApplication.serviceType().isRpcClient()) {
            return false;
        }
        return true;
    }

    private static boolean isToNode(final Application fromApplication, final Application toApplication) {
        if (!toApplication.serviceType().isRpcClient()) {
            return true;
        }
        if (toApplication.serviceType().isAlias()) {
            return true;
        }
        return false;
    }

    private static boolean addNode(NodeList nodeList, Application application) {
        if (nodeList.containsNode(application)) {
            return false;
        }
        Node fromNode = new Node(application);
        return nodeList.addNode(fromNode);
    }
}
