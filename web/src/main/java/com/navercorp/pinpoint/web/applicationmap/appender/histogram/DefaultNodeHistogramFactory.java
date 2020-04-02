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
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultNodeHistogramFactory implements NodeHistogramFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        final NodeHistogram nodeHistogram = new NodeHistogram(terminalApplication, range);

        // create applicationHistogram
        final List<Link> toLinkList = linkList.findToLink(terminalApplication);
        final Histogram applicationHistogram = new Histogram(terminalApplication.getServiceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeHistogram.setApplicationHistogram(applicationHistogram);

        // create applicationTimeHistogram
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
            linkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(terminalApplication, range);
        ApplicationTimeHistogram applicationTimeHistogram = builder.build(linkCallDataMap.getLinkDataList());
        nodeHistogram.setApplicationTimeHistogram(applicationTimeHistogram);

        // for Terminal nodes, create AgentLevel histogram
        if (terminalApplication.getServiceType().isTerminal() || terminalApplication.getServiceType().isAlias()) {
            LinkCallDataMap mergeSource = new LinkCallDataMap();
            final Map<String, Histogram> agentHistogramMap = new HashMap<>();
            for (Link link : toLinkList) {
                LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
                mergeSource.addLinkDataMap(sourceLinkCallDataMap);
                AgentHistogramList targetList = sourceLinkCallDataMap.getTargetList();
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
        nodeHistogram.setApplicationHistogram(sourceLink.getHistogram());

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

        // create applicationHistogram
        final Histogram applicationHistogram = new Histogram(queueApplication.getServiceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeHistogram.setApplicationHistogram(applicationHistogram);

        // create applicationTimeHistogram
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
