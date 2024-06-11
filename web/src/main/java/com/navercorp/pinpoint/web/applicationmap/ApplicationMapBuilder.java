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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.EmptyNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppender;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.EmptyServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppender;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilder {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Range range;

    private final NodeHistogramAppenderFactory nodeHistogramAppenderFactory;
    private final ServerInfoAppenderFactory serverInfoAppenderFactory;

    private NodeHistogramFactory nodeHistogramFactory;
    private ServerGroupListFactory serverGroupListFactory;

    public ApplicationMapBuilder(Range range, NodeHistogramAppenderFactory nodeHistogramAppenderFactory,
                                 ServerInfoAppenderFactory serverInfoAppenderFactory) {
        this.range = Objects.requireNonNull(range, "range");
        this.nodeHistogramAppenderFactory = Objects.requireNonNull(nodeHistogramAppenderFactory, "nodeHistogramAppenderFactory");
        this.serverInfoAppenderFactory = Objects.requireNonNull(serverInfoAppenderFactory, "serverInfoAppenderFactory");
    }


    public ApplicationMapBuilder includeNodeHistogram(NodeHistogramFactory nodeHistogramFactory) {
        this.nodeHistogramFactory = nodeHistogramFactory;
        return this;
    }

    public ApplicationMapBuilder includeServerInfo(ServerGroupListFactory serverGroupListFactory) {
        this.serverGroupListFactory = serverGroupListFactory;
        return this;
    }

    public ApplicationMap build(Application application, long timeoutMillis) {
        logger.info("Building empty application map");

        NodeList nodeList = new NodeList();
        LinkList emptyLinkList = new LinkList();

        Node node = new Node(application);
        if (serverGroupListFactory != null) {
            ServerGroupList runningInstances = serverGroupListFactory.createWasNodeInstanceList(node, range.getToInstant());
            if (runningInstances.getInstanceCount() > 0) {
                node.setServerGroupList(runningInstances);
                nodeList.addNode(node);
            }
        }

        NodeHistogramFactory nodeHistogramFactory = this.nodeHistogramFactory;
        if (nodeHistogramFactory == null) {
            nodeHistogramFactory = new EmptyNodeHistogramFactory();
        }
        NodeHistogramAppender nodeHistogramAppender = nodeHistogramAppenderFactory.create(nodeHistogramFactory);
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, emptyLinkList, timeoutMillis);

        return DefaultApplicationMap.build(nodeList, emptyLinkList, range);
    }

    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap, long timeoutMillis) {
        Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");

        logger.info("Building application map");

        NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
        LinkList linkList = LinkListFactory.createLinkList(nodeList, linkDataDuplexMap, range);

        NodeHistogramFactory nodeHistogramFactory = this.nodeHistogramFactory;
        if (nodeHistogramFactory == null) {
            nodeHistogramFactory = new EmptyNodeHistogramFactory();
        }

        NodeHistogramAppender nodeHistogramAppender = nodeHistogramAppenderFactory.create(nodeHistogramFactory);
        final TimeoutWatcher timeoutWatcher = new TimeoutWatcher(timeoutMillis);
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList, timeoutWatcher.remainingTimeMillis());

        ServerGroupListFactory serverGroupListFactory = this.serverGroupListFactory;
        if (serverGroupListFactory == null) {
            serverGroupListFactory = new EmptyServerGroupListFactory();
        }
        ServerInfoAppender serverInfoAppender = serverInfoAppenderFactory.create(serverGroupListFactory);
        serverInfoAppender.appendServerInfo(range, nodeList, linkDataDuplexMap, timeoutWatcher.remainingTimeMillis());

        return DefaultApplicationMap.build(nodeList, linkList, range);
    }

    private static class TimeoutWatcher {
        private static final int INFINITY_TIME = -1;
        private final long timeoutMillis;
        private final long startTimeMillis;

        public TimeoutWatcher(long timeoutMillis) {
            if (timeoutMillis <= 0) {
                this.timeoutMillis = INFINITY_TIME;
            } else {
                this.timeoutMillis = timeoutMillis;
            }
            this.startTimeMillis = System.currentTimeMillis();
        }

        public long remainingTimeMillis() {
            if (timeoutMillis == INFINITY_TIME) {
                return INFINITY_TIME;
            }

            long elapsedTimeMillis = System.currentTimeMillis() - this.startTimeMillis;
            if (this.timeoutMillis <= elapsedTimeMillis) {
                return 0;
            }
            return this.timeoutMillis - elapsedTimeMillis;
        }
    }
}
