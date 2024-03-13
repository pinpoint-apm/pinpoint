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
        NodeHistogram.Builder builder = NodeHistogram.newBuilder(terminalApplication, range);

        // create applicationHistogram
        final List<Link> toLinkList = linkList.findToLink(terminalApplication);

        final ServiceType terminalService = terminalApplication.serviceType();
        final Histogram applicationHistogram = new Histogram(terminalService);
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        builder.setApplicationHistogram(applicationHistogram);

        // create Agent histogram map for StatisticsAgentState
        if (terminalService.isTerminal() || terminalService.isAlias()) {
            final Map<String, Histogram> agentHistogramMap = getAgentHistogramMap(toLinkList);
            builder.setAgentHistogramMap(agentHistogramMap);
        }

        return builder.build();
    }

    private Map<String, Histogram> getAgentHistogramMap(List<Link> toLinkList) {
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
        }
        return agentHistogramMap;
    }

    @Override
    public NodeHistogram createUserNodeHistogram(Application userApplication, Range range, LinkList linkList) {
        // for User nodes, find its source link and create the histogram
        final List<Link> fromLink = linkList.findFromLink(userApplication);
        if (fromLink.isEmpty()) {
            logger.warn("from UserNode not found:{}", userApplication);
            return createEmptyNodeHistogram(userApplication, range);
        } else if (fromLink.size() > 1) {
            // log and use first(0) link.
            logger.warn("Invalid from UserNode:{}", linkList.getLinkList());
        }
        final Link sourceLink = fromLink.get(0);

        NodeHistogram.Builder builder = NodeHistogram.newBuilder(userApplication, range);
        builder.setApplicationHistogram(sourceLink.getHistogram());
        return builder.build();
    }

    @Override
    public NodeHistogram createQueueNodeHistogram(Application queueApplication, Range range, LinkList linkList) {
        final List<Link> toLinkList = linkList.findToLink(queueApplication);
        if (toLinkList.isEmpty()) {
            return NodeHistogram.empty(queueApplication, range);
        }

        // create applicationHistogram
        final Histogram applicationHistogram = new Histogram(queueApplication.serviceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }

        NodeHistogram.Builder builder = NodeHistogram.newBuilder(queueApplication, range);
        builder.setApplicationHistogram(applicationHistogram);
        return builder.build();
    }

    @Override
    public NodeHistogram createEmptyNodeHistogram(Application application, Range range) {
        return NodeHistogram.empty(application, range);
    }
}
