/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.service.SearchDepth;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class UnidirectionalLinkSelector implements LinkSelector {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationsMapCreator applicationsMapCreator;

    private final VirtualLinkHandler virtualLinkHandler;

    private final ServerMapDataFilter serverMapDataFilter;

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    UnidirectionalLinkSelector(
            ApplicationsMapCreator applicationsMapCreator,
            VirtualLinkHandler virtualLinkHandler,
            ServerMapDataFilter serverMapDataFilter) {
        this.applicationsMapCreator = Objects.requireNonNull(applicationsMapCreator, "applicationsMapCreator");
        this.virtualLinkHandler = Objects.requireNonNull(virtualLinkHandler, "virtualLinkHandler");
        this.serverMapDataFilter = serverMapDataFilter;
    }

    @Override
    public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int outSearchDepth, int inSearchDepth) {
        return select(sourceApplications, range, outSearchDepth, inSearchDepth, false);
    }

    @Override
    public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int outSearchDepth, int inSearchDepth, boolean timeAggregated) {
        logger.debug("Creating link data map for {}", sourceApplications);
        final SearchDepth outDepth = new SearchDepth(outSearchDepth);
        final SearchDepth inDepth = new SearchDepth(inSearchDepth);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        List<Application> applications = filterApplications(sourceApplications);

        List<Application> outboundApplications = Collections.unmodifiableList(applications);
        LinkSelectContext outboundLinkSelectContext = new LinkSelectContext(range, outDepth, new SearchDepth(0), linkVisitChecker, timeAggregated);
        List<Application> inboundApplications = Collections.unmodifiableList(applications);
        LinkSelectContext inboundLinkSelectContext = new LinkSelectContext(range, new SearchDepth(0), inDepth, linkVisitChecker, timeAggregated);

        while (!outboundApplications.isEmpty() || !inboundApplications.isEmpty()) {

            logger.info("{} depth search start. outDepth:{}, inDepth:{}, size:{}, nodes:{}", LinkDirection.OUT_LINK, outboundLinkSelectContext.getOutDepth(), outboundLinkSelectContext.getInDepth(), outboundApplications.size(), outboundApplications);
            LinkDataDuplexMap outboundMap = applicationsMapCreator.createLinkDataDuplexMap(outboundApplications, outboundLinkSelectContext);
            logger.info("{} depth search end. outDepth:{}, inDepth:{}", LinkDirection.OUT_LINK, outboundLinkSelectContext.getOutDepth(), outboundLinkSelectContext.getInDepth());

            logger.info("{} depth search start. outDepth:{}, inDepth:{}, size:{}, nodes:{}", LinkDirection.IN_LINK, inboundLinkSelectContext.getOutDepth(), inboundLinkSelectContext.getInDepth(), inboundApplications.size(), inboundApplications);
            LinkDataDuplexMap inboundMap = applicationsMapCreator.createLinkDataDuplexMap(inboundApplications, inboundLinkSelectContext);
            logger.info("{} depth search end. outDepth:{}, inDepth:{}", LinkDirection.IN_LINK, inboundLinkSelectContext.getOutDepth(), inboundLinkSelectContext.getInDepth());

            linkDataDuplexMap.addLinkDataDuplexMap(outboundMap);
            linkDataDuplexMap.addLinkDataDuplexMap(inboundMap);

            outboundApplications = filterApplications(outboundLinkSelectContext.getNextApplications());
            inboundApplications = filterApplications(inboundLinkSelectContext.getNextApplications());

            outboundLinkSelectContext = outboundLinkSelectContext.advance();
            inboundLinkSelectContext = inboundLinkSelectContext.advance();
        }
        return virtualLinkHandler.processVirtualLinks(linkDataDuplexMap, linkVisitChecker, range);
    }

    private List<Application> filterApplications(List<Application> applications) {
        if (serverMapDataFilter == null) {
            return applications;
        }
        List<Application> filteredApplications = new ArrayList<>();
        for (Application application : applications) {
            if (!serverMapDataFilter.filter(application)) {
                filteredApplications.add(application);
            }
        }
        return filteredApplications;
    }
}
