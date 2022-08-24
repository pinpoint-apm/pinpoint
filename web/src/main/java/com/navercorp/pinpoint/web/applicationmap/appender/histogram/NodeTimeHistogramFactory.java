package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class NodeTimeHistogramFactory implements NodeHistogramFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WasNodeHistogramDataSource wasNodeHistogramDataSource;

    public NodeTimeHistogramFactory(WasNodeHistogramDataSource wasNodeHistogramDataSource) {
        this.wasNodeHistogramDataSource = Objects.requireNonNull(wasNodeHistogramDataSource, "wasNodeHistogramDataSource");
    }

    @Override
    public NodeHistogram createWasNodeHistogram(Application wasApplication, Range range) {
        return wasNodeHistogramDataSource.createNodeHistogram(wasApplication, range);
    }

    @Override
    public NodeHistogram createTerminalNodeHistogram(Application terminalApplication, Range range, LinkList linkList) {
        // for Terminal nodes, add all links pointing to the application and create the histogram
        final NodeHistogram nodeHistogram = new NodeHistogram(terminalApplication, range);

        final List<Link> toLinkList = linkList.findToLink(terminalApplication);

        // create applicationTimeHistogram
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
            linkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(terminalApplication, range);
        ApplicationTimeHistogram applicationTimeHistogram = builder.build(linkCallDataMap.getLinkDataList());
        nodeHistogram.setApplicationTimeHistogram(applicationTimeHistogram);

        // for Terminal nodes, create AgentLevel time histogram
        if (terminalApplication.getServiceType().isTerminal() || terminalApplication.getServiceType().isAlias()) {
            LinkCallDataMap mergeSource = new LinkCallDataMap();
            for (Link link : toLinkList) {
                LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
                mergeSource.addLinkDataMap(sourceLinkCallDataMap);
            }

            AgentTimeHistogramBuilder agentTimeBuilder = new AgentTimeHistogramBuilder(terminalApplication, range);
            AgentTimeHistogram agentTimeHistogram = agentTimeBuilder.buildTarget(mergeSource);
            nodeHistogram.setAgentTimeHistogram(agentTimeHistogram);
        }

        return nodeHistogram;
    }

    @Override
    public NodeHistogram createUserNodeHistogram(Application userApplication, Range range, LinkList linkList) {
        // for User nodes, find its source link and create the histogram
        final NodeHistogram nodeHistogram = new NodeHistogram(userApplication, range);
        final List<Link> fromLink = linkList.findFromLink(userApplication);
        if (fromLink.isEmpty()) {
            logger.warn("from UserNode not found:{}", userApplication);
            return createEmptyNodeHistogram(userApplication, range);
        } else if (fromLink.size() > 1) {
            // log and use first(0) link.
            logger.warn("Invalid from UserNode:{}", linkList.getLinkList());
        }
        final Link sourceLink = fromLink.get(0);

        ApplicationTimeHistogram histogramData = sourceLink.getTargetApplicationTimeSeriesHistogramData();
        nodeHistogram.setApplicationTimeHistogram(histogramData);
        return nodeHistogram;
    }

    @Override
    public NodeHistogram createQueueNodeHistogram(Application queueApplication, Range range, LinkList linkList) {
        final List<Link> toLinkList = linkList.findToLink(queueApplication);
        if (toLinkList.isEmpty()) {
            return new NodeHistogram(queueApplication, range);
        }

        final NodeHistogram nodeHistogram = new NodeHistogram(queueApplication, range);

        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap linkCallDataMapToUse = link.getSourceLinkCallDataMap();
            // queues, unlike terminal nodes, could have targetLinkCallDataMap instead of sourceLinkCallDataMap
            // depending on whether the link has been generated with the queue node as the caller, or the callee.
            if (linkCallDataMapToUse.getLinkDataList().isEmpty()) {
                linkCallDataMapToUse = link.getTargetLinkCallDataMap();
            }
            linkCallDataMap.addLinkDataMap(linkCallDataMapToUse);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(queueApplication, range);
        ApplicationTimeHistogram applicationTimeHistogram = builder.build(linkCallDataMap.getLinkDataList());
        nodeHistogram.setApplicationTimeHistogram(applicationTimeHistogram);

        return nodeHistogram;
    }

    @Override
    public NodeHistogram createEmptyNodeHistogram(Application application, Range range) {
        return new NodeHistogram(application, range);
    }

}
