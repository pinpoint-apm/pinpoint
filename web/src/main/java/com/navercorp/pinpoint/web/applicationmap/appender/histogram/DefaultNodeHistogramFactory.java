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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
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

/**
 * @author HyunGil Jeong
 */
public class DefaultNodeHistogramFactory implements NodeHistogramFactory {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WasNodeHistogramDataSource wasNodeHistogramDataSource;

    public DefaultNodeHistogramFactory(WasNodeHistogramDataSource wasNodeHistogramDataSource) {
        this.wasNodeHistogramDataSource = Objects.requireNonNull(wasNodeHistogramDataSource, "wasNodeHistogramDataSource");
    }

    @Override
    public NodeHistogram createWasNodeHistogram(Application wasApplication, Range range) {
        return wasNodeHistogramDataSource.createNodeHistogram(wasApplication, range);
    }

    @Override
    public NodeHistogram createTerminalNodeHistogram(Application terminalApplication, Range range, LinkList linkList) {
        // for Terminal nodes, add all links pointing to the application and create the histogram
        final NodeHistogram.Builder nodeBuilder = NodeHistogram.newBuilder(terminalApplication, range);

        // create applicationHistogram
        final List<Link> toLinkList = linkList.findToLink(terminalApplication);
        final Histogram applicationHistogram = new Histogram(terminalApplication.serviceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeBuilder.setApplicationHistogram(applicationHistogram);

        // create applicationTimeHistogram
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap inLink = link.getInLink();
            linkCallDataMap.addLinkDataMap(inLink);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(terminalApplication, range);
        ApplicationTimeHistogram applicationTimeHistogram = builder.build(linkCallDataMap.getLinkDataList());
        nodeBuilder.setApplicationTimeHistogram(applicationTimeHistogram);

        // for Terminal nodes, create AgentLevel histogram
        if (terminalApplication.serviceType().isTerminal() || terminalApplication.serviceType().isAlias()) {
            LinkCallDataMap mergeSource = new LinkCallDataMap();
            final Map<String, Histogram> agentHistogramMap = new HashMap<>();
            for (Link link : toLinkList) {
                LinkCallDataMap inLinkCallDataMap = link.getInLink();
                mergeSource.addLinkDataMap(inLinkCallDataMap);
                AgentHistogramList outLinkList = inLinkCallDataMap.getOutLinkList();
                for (AgentHistogram histogram : outLinkList.getAgentHistogramList()) {
                    Histogram find = agentHistogramMap.get(histogram.getId());
                    if (find == null) {
                        find = new Histogram(histogram.getServiceType());
                        agentHistogramMap.put(histogram.getId(), find);
                    }
                    find.add(histogram.getHistogram());
                }
                nodeBuilder.setAgentHistogramMap(agentHistogramMap);
            }

            AgentTimeHistogramBuilder agentTimeBuilder = new AgentTimeHistogramBuilder(terminalApplication, range);
            AgentTimeHistogram agentTimeHistogram = agentTimeBuilder.buildTarget(mergeSource);
            nodeBuilder.setAgentTimeHistogram(agentTimeHistogram);
        }

        return nodeBuilder.build();
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

        final NodeHistogram.Builder builder = NodeHistogram.newBuilder(userApplication, range);
        builder.setApplicationHistogram(sourceLink.getHistogram());

        ApplicationTimeHistogram histogramData = sourceLink.getTargetApplicationTimeSeriesHistogramData();
        builder.setApplicationTimeHistogram(histogramData);
        return builder.build();
    }

    @Override
    public NodeHistogram createQueueNodeHistogram(Application queueApplication, Range range, LinkList linkList) {
        final List<Link> toLinkList = linkList.findToLink(queueApplication);
        if (toLinkList.isEmpty()) {
            return NodeHistogram.empty(queueApplication, range);
        }

        final NodeHistogram.Builder nodeBuilder = NodeHistogram.newBuilder(queueApplication, range);

        // create applicationHistogram
        final Histogram applicationHistogram = new Histogram(queueApplication.serviceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeBuilder.setApplicationHistogram(applicationHistogram);

        ApplicationTimeHistogram applicationTimeHistogram = buildApplicationTimeHistogram(queueApplication, range, toLinkList);
        nodeBuilder.setApplicationTimeHistogram(applicationTimeHistogram);

        return nodeBuilder.build();
    }

    private ApplicationTimeHistogram buildApplicationTimeHistogram(Application queueApplication, Range range, List<Link> toLinkList) {
        // create applicationTimeHistogram
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap linkCallDataMapToUse = link.getInLink();
            // queues, unlike terminal nodes, could have targetLinkCallDataMap instead of sourceLinkCallDataMap
            // depending on whether the link has been generated with the queue node as the out link, or the in link.
            if (linkCallDataMapToUse.getLinkDataList().isEmpty()) {
                linkCallDataMapToUse = link.getOutLink();
            }
            linkCallDataMap.addLinkDataMap(linkCallDataMapToUse);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(queueApplication, range);
        return builder.build(linkCallDataMap.getLinkDataList());
    }

    @Override
    public NodeHistogram createEmptyNodeHistogram(Application application, Range range) {
        return NodeHistogram.empty(application, range);
    }
}
