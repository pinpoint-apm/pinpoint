package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.EmptyNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppender;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class MapTimeDataBuilder {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Range range;

    private final NodeHistogramAppenderFactory nodeHistogramAppenderFactory;

    private NodeType nodeType;
    private LinkType linkType;
    private NodeHistogramFactory nodeHistogramFactory;

    public MapTimeDataBuilder(Range range, NodeHistogramAppenderFactory nodeHistogramAppenderFactory) {
        this.range = Objects.requireNonNull(range, "range");
        this.nodeHistogramAppenderFactory = Objects.requireNonNull(nodeHistogramAppenderFactory, "nodeHistogramAppenderFactory");
    }

    public MapTimeDataBuilder nodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public MapTimeDataBuilder linkType(LinkType linkType) {
        this.linkType = linkType;
        return this;
    }

    public MapTimeDataBuilder includeNodeHistogram(NodeHistogramFactory nodeHistogramFactory) {
        this.nodeHistogramFactory = nodeHistogramFactory;
        return this;
    }

    public ApplicationMapTimeData build(long timeoutMillis) {
        logger.info("Building empty Time series view");

        NodeList emptyNodeList = new NodeList();
        LinkList emptyLinkList = new LinkList();

        NodeHistogramFactory nodeHistogramFactory = this.nodeHistogramFactory;
        if (nodeHistogramFactory == null) {
            nodeHistogramFactory = new EmptyNodeHistogramFactory();
        }
        NodeHistogramAppender nodeHistogramAppender = nodeHistogramAppenderFactory.create(nodeHistogramFactory);
        nodeHistogramAppender.appendNodeHistogram(range, emptyNodeList, emptyLinkList, timeoutMillis);

        return new ApplicationMapTimeData(emptyNodeList, emptyLinkList);
    }

    public ApplicationMapTimeData build(LinkDataDuplexMap linkDataDuplexMap, long timeoutMillis) {
        Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");

        logger.info("Building application map time data");

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
        final TimeoutWatcher timeoutWatcher = new TimeoutWatcher(timeoutMillis);
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList, timeoutWatcher.remainingTimeMillis());

        return new ApplicationMapTimeData(nodeList, linkList);
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
