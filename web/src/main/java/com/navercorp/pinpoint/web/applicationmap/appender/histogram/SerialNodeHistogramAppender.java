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
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Collection;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class SerialNodeHistogramAppender implements NodeHistogramAppender {

    private final NodeHistogramFactory nodeHistogramFactory;

    public SerialNodeHistogramAppender(NodeHistogramFactory nodeHistogramFactory) {
        if (nodeHistogramFactory == null) {
            throw new NullPointerException("nodeHistogramFactory must not be null");
        }
        this.nodeHistogramFactory = nodeHistogramFactory;
    }

    @Override
    public void appendNodeHistogram(Range range, NodeList nodeList, LinkList linkList) {
        final Collection<Node> nodes = nodeList.getNodeList();
        for (Node node : nodes) {
            node.setNodeHistogram(createNodeHistogram(range, node, linkList));
        }
    }

    private NodeHistogram createNodeHistogram(Range range, Node node, LinkList linkList) {
        final Application application = node.getApplication();
        final ServiceType serviceType = application.getServiceType();
        if (serviceType.isWas()) {
            // for WAS nodes, set their own response time histogram
            return nodeHistogramFactory.createWasNodeHistogram(application, range);
        } else if (serviceType.isTerminal() || serviceType.isUnknown()) {
            return nodeHistogramFactory.createTerminalNodeHistogram(application, range, linkList);
        } else if (serviceType.isQueue()) {
            // Virtual queue node - queues with agent installed will be handled above as a WAS node
            return nodeHistogramFactory.createQueueNodeHistogram(application, range, linkList);
        } else if (serviceType.isUser()) {
            return nodeHistogramFactory.createUserNodeHistogram(application, range, linkList);
        } else {
            return nodeHistogramFactory.createEmptyNodeHistogram(application, range);
        }
    }
}
