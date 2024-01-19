package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimplifiedNodeHistogramFactory implements NodeHistogramFactory {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WasNodeHistogramDataSource wasNodeHistogramDataSource;

    public SimplifiedNodeHistogramFactory(WasNodeHistogramDataSource wasNodeHistogramDataSource) {
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

        // create applicationHistogram
        final List<Link> toLinkList = linkList.findToLink(terminalApplication);

        final ServiceType terminalService = terminalApplication.getServiceType();
        final Histogram applicationHistogram = new Histogram(terminalService);
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeHistogram.setApplicationHistogram(applicationHistogram);

        // create Agent histogram map for StatisticsAgentState
        if (terminalService.isTerminal() || terminalService.isAlias()) {
            final Map<String, Histogram> agentHistogramMap = new HashMap<>();
            for (Link link : toLinkList) {
                LinkCallDataMap inLink = link.getInLink();
                AgentHistogramList targetList = inLink.getOutLinkList();
                for (AgentHistogram histogram : targetList.getAgentHistogramList()) {
                    Histogram find = agentHistogramMap.get(histogram.getId());
                    if (find == null) {
                        find = new Histogram(histogram.getServiceType());
                        agentHistogramMap.put(histogram.getId(), find);
                    }
                    find.add(histogram.getHistogram());
                }
                nodeHistogram.setAgentHistogramMap(agentHistogramMap);
            }
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
        nodeHistogram.setApplicationHistogram(sourceLink.getHistogram());

        return nodeHistogram;
    }

    @Override
    public NodeHistogram createQueueNodeHistogram(Application queueApplication, Range range, LinkList linkList) {
        final List<Link> toLinkList = linkList.findToLink(queueApplication);
        if (toLinkList.isEmpty()) {
            return new NodeHistogram(queueApplication, range);
        }

        final NodeHistogram nodeHistogram = new NodeHistogram(queueApplication, range);

        // create applicationHistogram
        final Histogram applicationHistogram = new Histogram(queueApplication.getServiceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeHistogram.setApplicationHistogram(applicationHistogram);

        return nodeHistogram;
    }

    @Override
    public NodeHistogram createEmptyNodeHistogram(Application application, Range range) {
        return new NodeHistogram(application, range);
    }
}
