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

import com.navercorp.pinpoint.web.applicationmap.link.LinkFactory.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.navercorp.pinpoint.web.view.ResponseTimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple wrapper class for {@Link} that does not expose the underlying agent level histograms.
 *
 * @author HyunGil Jeong
 */
public class BasicLink implements Link {

    private final Link delegate;

    BasicLink(Link delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public Application getFilterApplication() {
        return delegate.getFilterApplication();
    }

    @Override
    public LinkKey getLinkKey() {
        return delegate.getLinkKey();
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.BASIC;
    }

    @Override
    public Node getFrom() {
        return delegate.getFrom();
    }

    @Override
    public Node getTo() {
        return delegate.getTo();
    }

    @Override
    public Range getRange() {
        return delegate.getRange();
    }

    @Override
    public String getLinkName() {
        return delegate.getLinkName();
    }

    @Override
    public LinkCallDataMap getSourceLinkCallDataMap() {
        return delegate.getSourceLinkCallDataMap();
    }

    @Override
    public LinkCallDataMap getTargetLinkCallDataMap() {
        return delegate.getTargetLinkCallDataMap();
    }

    @Override
    public CreateType getCreateType() {
        return delegate.getCreateType();
    }

    @Override
    public AgentHistogramList getSourceList() {
        return new AgentHistogramList();
    }

    @Override
    public AgentHistogramList getTargetList() {
        return new AgentHistogramList();
    }

    @Override
    public Histogram getHistogram() {
        return delegate.getHistogram();
    }

    @Override
    public Histogram getTargetHistogram() {
        return delegate.getTargetHistogram();
    }

    @Override
    public List<ResponseTimeViewModel> getLinkApplicationTimeSeriesHistogram() {
        return delegate.getLinkApplicationTimeSeriesHistogram();
    }

    @Override
    public List<ResponseTimeViewModel> getSourceApplicationTimeSeriesHistogram() {
        return delegate.getSourceApplicationTimeSeriesHistogram();
    }

    @Override
    public List<ResponseTimeViewModel> getTargetApplicationTimeSeriesHistogram() {
        return delegate.getTargetApplicationTimeSeriesHistogram();
    }

    @Override
    public ApplicationTimeHistogram getTargetApplicationTimeSeriesHistogramData() {
        return delegate.getTargetApplicationTimeSeriesHistogramData();
    }

    @Override
    public void addSource(LinkCallDataMap sourceLinkCallDataMap) {
        delegate.addSource(sourceLinkCallDataMap);
    }

    @Override
    public void addTarget(LinkCallDataMap targetLinkCallDataMap) {
        delegate.addTarget(targetLinkCallDataMap);
    }

    @Override
    public AgentResponseTimeViewModelList getSourceAgentTimeSeriesHistogram() {
        return new AgentResponseTimeViewModelList(Collections.emptyList());
    }

    @Override
    public AgentTimeHistogram getTargetAgentTimeHistogram() {
        return new AgentTimeHistogram(delegate.getTo().getApplication(), delegate.getRange());
    }

    @Override
    public Collection<Application> getSourceLinkTargetAgentList() {
        return delegate.getSourceLinkTargetAgentList();
    }

    @Override
    public String getLinkState() {
        return delegate.getLinkState();
    }

    @Override
    public Boolean getLinkAlert() {
        return delegate.getLinkAlert();
    }

    @Override
    public boolean isWasToWasLink() {
        return delegate.isWasToWasLink();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicLink that = (BasicLink) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleLink{");
        sb.append("from=").append(delegate.getFrom());
        sb.append(" -> to=").append(delegate.getTo());
        sb.append('}');
        return sb.toString();
    }
}
