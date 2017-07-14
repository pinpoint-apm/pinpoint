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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.link.LinkFactory.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.navercorp.pinpoint.web.view.ResponseTimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that describes a relationship between apps in application map
 *
 * @author netspider
 * @author emeroad
 */
public class DetailedLink implements Link {

    // specifies who created the link.
    // indicates whether it was automatically created by the source, or if it was manually created by the target.
    private final CreateType createType;

    private final Node fromNode;
    private final Node toNode;

    private final Range range;

    private final LinkStateResolver linkStateResolver = LinkStateResolver.DEFAULT_LINK_STATE_RESOLVER;

    private final LinkCallDataMap sourceLinkCallDataMap = new LinkCallDataMap();

    private final LinkCallDataMap targetLinkCallDataMap = new LinkCallDataMap();

    private Histogram linkHistogram;

    DetailedLink(CreateType createType, Node fromNode, Node toNode, Range range) {
        if (createType == null) {
            throw new NullPointerException("createType must not be null");
        }
        if (fromNode == null) {
            throw new NullPointerException("fromNode must not be null");
        }
        if (toNode == null) {
            throw new NullPointerException("toNode must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        this.createType = createType;

        this.fromNode = fromNode;
        this.toNode = toNode;

        this.range = range;
    }

    @Override
    public Application getFilterApplication() {
        // User link: need to look at WAS, not from
        // Since User is a virtual link, we cannot filter by User
        if (fromNode.getServiceType() == ServiceType.USER) {
            return toNode.getApplication();
        }
        // same goes for virtual queue nodes
        if (!fromNode.getServiceType().isWas() && fromNode.getServiceType().isQueue()) {
            return toNode.getApplication();
        }
        return fromNode.getApplication();
    }

    @Override
    public LinkKey getLinkKey() {
        return new LinkKey(fromNode.getApplication(), toNode.getApplication());
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.DETAILED;
    }

    @Override
    public Node getFrom() {
        return fromNode;
    }

    @Override
    public Node getTo() {
        return toNode;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public String getLinkName() {
        return fromNode.getNodeName() + LINK_DELIMITER + toNode.getNodeName();
    }

    @Override
    public LinkCallDataMap getSourceLinkCallDataMap() {
        return sourceLinkCallDataMap;
    }

    @Override
    public LinkCallDataMap getTargetLinkCallDataMap() {
        return targetLinkCallDataMap;
    }

    @Override
    public CreateType getCreateType() {
        return createType;
    }

    @JsonIgnore
    @Override
    public AgentHistogramList getSourceList() {
        return sourceLinkCallDataMap.getSourceList();
    }

    @Override
    public AgentHistogramList getTargetList() {
        return sourceLinkCallDataMap.getTargetList();
    }

    @Override
    public Histogram getHistogram() {
        if (linkHistogram == null) {
            linkHistogram = createHistogram0();
        }
        return linkHistogram;
    }

    private Histogram createHistogram0() {
        // need serviceType of target (callee)
        // ie. Tomcat -> Arcus: we need arcus type
        final LinkCallDataMap findMap = getLinkCallDataMap();
        AgentHistogramList targetList = findMap.getTargetList();
        return targetList.mergeHistogram(toNode.getServiceType());
    }

    private LinkCallDataMap getLinkCallDataMap() {
        switch (createType) {
            case Source:
                return sourceLinkCallDataMap;
            case Target:
                return targetLinkCallDataMap;
            default:
                throw new IllegalArgumentException("invalid CreateType:" + createType);
        }
    }

    @Override
    public Histogram getTargetHistogram() {
        // need serviceType of target (callee)
        // ie. Tomcat -> Arcus: we need Arcus type
        AgentHistogramList targetList = targetLinkCallDataMap.getTargetList();
        return targetList.mergeHistogram(toNode.getServiceType());
    }

    @Override
    public List<ResponseTimeViewModel> getLinkApplicationTimeSeriesHistogram() {
        if (createType == CreateType.Source)  {
            return getSourceApplicationTimeSeriesHistogram();
        } else {
            return getTargetApplicationTimeSeriesHistogram();
        }
    }

    @Override
    public List<ResponseTimeViewModel> getSourceApplicationTimeSeriesHistogram() {
        ApplicationTimeHistogram histogramData = getSourceApplicationTimeSeriesHistogramData();
        return histogramData.createViewModel();
    }

    private ApplicationTimeHistogram getSourceApplicationTimeSeriesHistogramData() {
        // we need Target (to)'s time since time in link is RPC-based
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.build(sourceLinkCallDataMap.getLinkDataList());
    }

    @Override
    public List<ResponseTimeViewModel> getTargetApplicationTimeSeriesHistogram() {
        ApplicationTimeHistogram targetApplicationTimeHistogramData = getTargetApplicationTimeSeriesHistogramData();
        return targetApplicationTimeHistogramData.createViewModel();
    }

    @Override
    public ApplicationTimeHistogram getTargetApplicationTimeSeriesHistogramData() {
        // we need Target (to)'s time since time in link is RPC-based
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.build(targetLinkCallDataMap.getLinkDataList());
    }

    @Override
    public void addSource(LinkCallDataMap sourceLinkCallDataMap) {
        this.sourceLinkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
    }

    @Override
    public void addTarget(LinkCallDataMap targetLinkCallDataMap) {
        this.targetLinkCallDataMap.addLinkDataMap(targetLinkCallDataMap);
    }

    @Override
    public AgentResponseTimeViewModelList getSourceAgentTimeSeriesHistogram() {
        // we need Target (to)'s time since time in link is RPC-based
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(toNode.getApplication(), range);
        AgentTimeHistogram applicationTimeSeriesHistogram = builder.buildSource(sourceLinkCallDataMap);
        AgentResponseTimeViewModelList agentResponseTimeViewModelList = new AgentResponseTimeViewModelList(applicationTimeSeriesHistogram.createViewModel());
        return agentResponseTimeViewModelList;
    }

    @Override
    public AgentTimeHistogram getTargetAgentTimeHistogram() {
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(toNode.getApplication(), range);
        AgentTimeHistogram agentTimeHistogram = builder.buildSource(targetLinkCallDataMap);
        return agentTimeHistogram;
    }

    @Override
    public Collection<Application> getSourceLinkTargetAgentList() {
        Set<Application> agentList = new HashSet<>();
        Collection<LinkCallData> linkDataList = sourceLinkCallDataMap.getLinkDataList();
        for (LinkCallData linkCallData : linkDataList) {
            agentList.add(new Application(linkCallData.getTarget(), linkCallData.getTargetServiceType()));
        }
        return agentList;
    }

    @Override
    public String getLinkState() {
        return linkStateResolver.resolve(this);
    }

    @Override
    public Boolean getLinkAlert() {
        return linkStateResolver.isAlert(this);
    }

    @Override
    public boolean isWasToWasLink() {
        return this.fromNode.getApplication().getServiceType().isWas() && this.toNode.getApplication().getServiceType().isWas();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetailedLink that = (DetailedLink) o;

        if (!fromNode.equals(that.fromNode)) return false;
        if (!toNode.equals(that.toNode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromNode.hashCode();
        result = 31 * result + toNode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultLink{" +
                "from=" + fromNode +
                " -> to=" + toNode +
                '}';
    }
}
