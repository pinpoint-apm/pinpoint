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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.view.LinkSerializer;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A class that describes a relationship between apps in application map
 *
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@JsonSerialize(using = LinkSerializer.class)
public class Link {

    // specifies who created the link.
    // indicates whether it was automatically created by the source, or if it was manually created by the target.
    private final LinkDirection direction;

    private final Node fromNode;
    private final Node toNode;

    private final Range range;

    private final LinkStateResolver linkStateResolver = LinkStateResolver.DEFAULT_LINK_STATE_RESOLVER;

    private final LinkCallDataMap inLink = new LinkCallDataMap();

    private final LinkCallDataMap outLink = new LinkCallDataMap();

    private Histogram linkHistogram;
    private TimeHistogramFormat timeHistogramFormat = TimeHistogramFormat.V1;

    public Link(LinkDirection direction, Node fromNode, Node toNode, Range range) {
        this.direction = Objects.requireNonNull(direction, "direction");
        this.fromNode = Objects.requireNonNull(fromNode, "fromNode");
        this.toNode = Objects.requireNonNull(toNode, "toNode");
        this.range = Objects.requireNonNull(range, "range");
    }

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

    public LinkKey getLinkKey() {
        return new LinkKey(fromNode.getApplication(), toNode.getApplication());
    }

    public Node getFrom() {
        return fromNode;
    }

    public Node getTo() {
        return toNode;
    }

    public Range getRange() {
        return range;
    }

    public LinkName getLinkName() {
        return LinkName.of(fromNode.getApplication(), toNode.getApplication());
    }

    public TimeHistogramFormat getTimeHistogramFormat() {
        return timeHistogramFormat;
    }

    public void setTimeHistogramFormat(TimeHistogramFormat timeHistogramFormat) {
        this.timeHistogramFormat = timeHistogramFormat;
    }

    public LinkCallDataMap getInLink() {
        return inLink;
    }

    public LinkCallDataMap getOutLink() {
        return outLink;
    }

    public LinkDirection getDirection() {
        return direction;
    }

    @JsonIgnore
    public AgentHistogramList getSourceList() {
        return inLink.getInLinkList();
    }

    public AgentHistogramList getTargetList() {
        return inLink.getOutLinkList();
    }

    public Histogram getHistogram() {
        if (linkHistogram == null) {
            linkHistogram = createHistogram0();
        }
        return linkHistogram;
    }

    private Histogram createHistogram0() {
        // need serviceType of out link
        // ie. Tomcat -> Arcus: we need arcus type
        final LinkCallDataMap findMap = getLinkCallDataMap();
        AgentHistogramList outLinkList = findMap.getOutLinkList();
        return outLinkList.mergeHistogram(toNode.getServiceType());
    }

    private LinkCallDataMap getLinkCallDataMap() {
        return switch (direction) {
            case IN_LINK -> inLink;
            case OUT_LINK -> outLink;
        };
    }

    public Histogram getTargetHistogram() {
        // need serviceType of out link
        // ie. Tomcat -> Arcus: we need Arcus type
        AgentHistogramList outLinkList = outLink.getOutLinkList();
        return outLinkList.mergeHistogram(toNode.getServiceType());
    }

    public List<TimeViewModel> getLinkApplicationTimeSeriesHistogram() {
        if (direction == LinkDirection.IN_LINK) {
            return getSourceApplicationTimeSeriesHistogram();
        } else {
            return getTargetApplicationTimeSeriesHistogram();
        }
    }

    public List<TimeViewModel> getSourceApplicationTimeSeriesHistogram() {
        ApplicationTimeHistogram histogramData = getSourceApplicationTimeSeriesHistogramData();
        return histogramData.createViewModel(this.timeHistogramFormat);
    }

    private ApplicationTimeHistogram getSourceApplicationTimeSeriesHistogramData() {
        // we need Target (to)'s time since time in link is RPC-based
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.build(inLink.getLinkDataList());
    }

    public List<TimeViewModel> getTargetApplicationTimeSeriesHistogram() {
        ApplicationTimeHistogram targetApplicationTimeHistogramData = getTargetApplicationTimeSeriesHistogramData();
        return targetApplicationTimeHistogramData.createViewModel(this.timeHistogramFormat);
    }

    public ApplicationTimeHistogram getTargetApplicationTimeSeriesHistogramData() {
        // we need Target (to)'s time since time in link is RPC-based
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.build(outLink.getLinkDataList());
    }

    public ApplicationTimeHistogram getLinkApplicationTimeHistogram() {
        if (direction == LinkDirection.IN_LINK) {
            return getSourceApplicationTimeSeriesHistogramData();
        } else {
            return getTargetApplicationTimeSeriesHistogramData();
        }
    }

    public void addInLink(LinkCallDataMap inLinkCallDataMap) {
        this.inLink.addLinkDataMap(inLinkCallDataMap);
    }

    public void addOutLink(LinkCallDataMap outLinkCallDataMap) {
        this.outLink.addLinkDataMap(outLinkCallDataMap);
    }

    public JsonFields<AgentNameView, List<TimeViewModel>> getSourceAgentTimeSeriesHistogram() {
        // we need Target (to)'s time since time in link is RPC-based
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(toNode.getApplication(), range);
        AgentTimeHistogram applicationTimeSeriesHistogram = builder.buildSource(inLink);

        return applicationTimeSeriesHistogram.createViewModel(timeHistogramFormat);
    }

    public AgentTimeHistogram getTargetAgentTimeHistogram() {
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.buildSource(outLink);
    }

    public Collection<Application> getSourceLinkTargetAgentList() {
        Set<Application> agentList = new HashSet<>();
        Collection<LinkCallData> linkDataList = inLink.getLinkDataList();
        for (LinkCallData linkCallData : linkDataList) {
            agentList.add(linkCallData.getTarget());
        }
        return agentList;
    }

    public String getLinkState() {
        return linkStateResolver.resolve(this);
    }

    public Boolean getLinkAlert() {
        return linkStateResolver.isAlert(this);
    }

    public boolean isWasToWasLink() {
        return this.fromNode.getApplication().serviceType().isWas() && this.toNode.getApplication().serviceType().isWas();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link that = (Link) o;

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
