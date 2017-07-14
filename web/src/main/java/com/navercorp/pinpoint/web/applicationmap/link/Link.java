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
import com.navercorp.pinpoint.web.applicationmap.histogram.*;
import com.navercorp.pinpoint.web.applicationmap.rawdata.*;
import com.navercorp.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.navercorp.pinpoint.web.view.LinkSerializer;
import com.navercorp.pinpoint.web.view.ResponseTimeViewModel;
import com.navercorp.pinpoint.web.vo.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collection;
import java.util.List;

/**
 * An interface for describing a relationship between apps in application map
 *
 * @author netspider
 * @author emeroad
 */
@JsonSerialize(using = LinkSerializer.class)
public interface Link {

    String LINK_DELIMITER = "~";

    Application getFilterApplication();

    LinkKey getLinkKey();

    LinkType getLinkType();

    Node getFrom();

    Node getTo();

    Range getRange();

    String getLinkName();

    LinkCallDataMap getSourceLinkCallDataMap();

    LinkCallDataMap getTargetLinkCallDataMap();
    
    CreateType getCreateType();

    @JsonIgnore
    AgentHistogramList getSourceList();

    AgentHistogramList getTargetList();

    Histogram getHistogram();

    Histogram getTargetHistogram();

    List<ResponseTimeViewModel> getLinkApplicationTimeSeriesHistogram();

    List<ResponseTimeViewModel> getSourceApplicationTimeSeriesHistogram();

    List<ResponseTimeViewModel> getTargetApplicationTimeSeriesHistogram();

    ApplicationTimeHistogram getTargetApplicationTimeSeriesHistogramData();

    void addSource(LinkCallDataMap sourceLinkCallDataMap);

    void addTarget(LinkCallDataMap targetLinkCallDataMap);

    AgentResponseTimeViewModelList getSourceAgentTimeSeriesHistogram();

    AgentTimeHistogram getTargetAgentTimeHistogram();

    Collection<Application> getSourceLinkTargetAgentList();

    String getLinkState();

    Boolean getLinkAlert();

    boolean isWasToWasLink();
}
