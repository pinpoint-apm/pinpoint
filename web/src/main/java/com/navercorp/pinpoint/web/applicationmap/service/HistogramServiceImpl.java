/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelector;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorType;
import com.navercorp.pinpoint.web.applicationmap.map.processor.DestinationApplicationFilter;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.map.processor.SourceApplicationFilter;
import com.navercorp.pinpoint.web.applicationmap.map.processor.WasOnlyProcessor;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@Service
public class HistogramServiceImpl implements HistogramService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkSelectorFactory linkSelectorFactory;

    public HistogramServiceImpl(
            LinkSelectorFactory linkSelectorFactory
    ) {
        this.linkSelectorFactory = Objects.requireNonNull(linkSelectorFactory, "linkSelectorFactory");
    }

    @Override
    public LinkDataDuplexMap selectLinkDataDuplexMap(MapServiceOption option) {

        logger.debug("Selecting LinkDataDuplexMap for {}", option.getSourceApplication());

        StopWatch watch = new StopWatch("HistogramServiceImpl.selectLinkDataDuplexMap");
        watch.start("selectLinkDataDuplexMap");

        final SearchOption searchOption = option.getSearchOption();
        LinkSelectorType linkSelectorType = searchOption.getLinkSelectorType();
        int outSearchDepth = searchOption.getOutSearchDepth();
        int inSearchDepth = searchOption.getInSearchDepth();
        boolean timeAggregate = option.isSimpleResponseHistogram();

        LinkDataMapProcessor outLinkProcessor = LinkDataMapProcessor.NO_OP;
        if (searchOption.isWasOnly()) {
            outLinkProcessor = new WasOnlyProcessor();
        }
        LinkDataMapProcessor inLinkProcessor = LinkDataMapProcessor.NO_OP;
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(linkSelectorType, outLinkProcessor, inLinkProcessor);

        TimeWindow timeWindow = option.getTimeWindow();
        LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(
                Collections.singletonList(
                        option.getSourceApplication()
                ), timeWindow, outSearchDepth, inSearchDepth, timeAggregate);
        watch.stop();
        logger.debug("LinkDataDuplexMap selected in {} ms. node={}, outDepth={}, inDepth={}, count={}",
                watch.getTotalTimeMillis(), option.getSourceApplication(), outSearchDepth, inSearchDepth, linkDataDuplexMap.size());
        return linkDataDuplexMap;
    }


    @Override
    public LinkHistogramSummary selectLinkHistogramData(Application fromApplication, Application toApplication, TimeWindow timeWindow) {
        Objects.requireNonNull(fromApplication, "fromApplication");
        Objects.requireNonNull(toApplication, "toApplication");
        Objects.requireNonNull(timeWindow, "range");

        LinkDataDuplexMap linkDataDuplexMap;
        LinkDirection linkDirection = LinkDirection.OUT_LINK;

        LinkDataMapProcessor sourceApplicationFilter = new SourceApplicationFilter(toApplication);
        LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(fromApplication);
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.BIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
        LinkDataDuplexMap linkDataDuplexMap1 = linkSelector.select(Collections.singletonList(toApplication), timeWindow, 1, 1);

        LinkSelector linkSelector2 = linkSelectorFactory.createLinkSelector(LinkSelectorType.BIDIRECTIONAL, LinkDataMapProcessor.NO_OP, sourceApplicationFilter);
        LinkDataDuplexMap linkDataDuplexMap2 = linkSelector2.select(Collections.singletonList(fromApplication), timeWindow, 1, 1);

        linkDataDuplexMap1.addLinkDataDuplexMap(linkDataDuplexMap2);
        linkDataDuplexMap = linkDataDuplexMap1;

        NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
        Range range = timeWindow.getWindowRange();
        LinkList linkList = LinkListFactory.createLinkList(nodeList, linkDataDuplexMap, range);
        LinkKey linkKey = new LinkKey(fromApplication, toApplication);
        Link link = linkList.getLink(linkKey);
        if (link == null) {
            return createEmptyLinkHistogramSummary(linkDirection, fromApplication, toApplication, range);
        }
        return new LinkHistogramSummary(link);
    }

    private LinkHistogramSummary createEmptyLinkHistogramSummary(LinkDirection direction, Application fromApplication, Application toApplication, Range range) {
        Node fromNode = new Node(fromApplication);
        Node toNode = new Node(toApplication);
        Link emptyLink = new Link(direction, fromNode, toNode, range);
        return new LinkHistogramSummary(emptyLink);
    }

    @Override
    public List<Application> getFromApplications(LinkDataDuplexMap linkDataDuplexMap) {
        Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");

        // Get the 'from' applications from the target link data map
        // In the target link, 'from' -> 'to' where 'to' Application is the main application,
        // so we get the 'from' applications from the target link data map.
        return linkDataDuplexMap.getTargetFromApplication();
    }

    @Override
    public List<Application> getToApplications(LinkDataDuplexMap linkDataDuplexMap) {
        Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");

        // Get the 'to' applications from the source link data map
        // In the source link, 'from' -> 'to' where 'from' Application is the main application,
        // so we get the 'to' applications from the source link data map.
        return linkDataDuplexMap.getSourceToApplication();
    }

    @Override
    public boolean isToNode(
            LinkDataDuplexMap linkDataDuplexMap, Application nodeApplication
    ) {
        Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");
        Objects.requireNonNull(nodeApplication, "nodeApplication");

        LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
        for (LinkData linkData : targetLinkDataMap.getLinkDataList()) {
            if (linkData.getFromApplication().equals(nodeApplication)) {
                return true;
            }
        }
        return false;
    }


}
